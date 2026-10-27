package com.gqibindlala.booking.service;

import com.gqibindlala.booking.dto.AuthResponse;
import com.gqibindlala.booking.dto.CreateStaffRequest;
import com.gqibindlala.booking.dto.LoginRequest;
import com.gqibindlala.booking.dto.RegisterRequest;
import com.gqibindlala.booking.dto.UserResponse;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.DepartmentRepository;
import com.gqibindlala.booking.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository userRepository, DepartmentRepository departmentRepository,
                       EmailVerificationService emailVerificationService,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeIdentifier(request.email());
        if (userRepository.existsByLoginIdentifier(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setLoginIdentifier(email);
        user.setPersonalEmail(email);
        user.setPhone(request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(AppRole.CUSTOMER);
        user.setEmailVerified(false);
        AppUser savedUser = userRepository.save(user);
        emailVerificationService.sendVerification(savedUser);
    }

    @Transactional
    public void verifyEmail(String token) {
        emailVerificationService.verify(token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByLoginIdentifier(normalizeIdentifier(request.loginIdentifier()))
            .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (user.getRole() == AppRole.CUSTOMER && !user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify your email before signing in");
        }
        return createResponse(user);
    }

    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        if (request.role() != AppRole.DEPARTMENT && request.role() != AppRole.PAYMENTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff role must be DEPARTMENT or PAYMENTS");
        }
        if (request.departmentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a department for staff access");
        }
        Department department = departmentRepository.findById(request.departmentId())
            .filter(Department::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an active department"));
        if (request.role() == AppRole.PAYMENTS && !department.getSlug().equals("finance")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment reviewers must belong to Finance");
        }
        if (request.role() == AppRole.DEPARTMENT && department.getSlug().equals("finance")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finance access must use the Payments role");
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setPersonalEmail(normalizeOptionalEmail(request.personalEmail()));
        user.setLoginIdentifier(generateStaffIdentifier(user.getFullName(), department));
        user.setPhone(request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
        user.setRole(request.role());
        user.setDepartment(department);
        return UserResponse.from(userRepository.save(user));
    }

    private AuthResponse createResponse(AppUser user) {
        return new AuthResponse(jwtService.issueToken(user), UserResponse.from(user));
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeOptionalEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateStaffIdentifier(String fullName, Department department) {
        String[] parts = fullName.trim().split("\\s+");
        String first = normalizeNamePart(parts[0]);
        String surname = normalizeNamePart(parts[parts.length - 1]);
        if (first.isBlank() || surname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff name must contain letters or numbers");
        }
        String base = first.substring(0, 1) + surname;
        String domain = department.getLoginDomain().toLowerCase(Locale.ROOT);
        String identifier = base + "@" + domain;
        int suffix = 2;
        while (userRepository.existsByLoginIdentifier(identifier)) {
            identifier = base + suffix++ + "@" + domain;
        }
        return identifier;
    }

    private String normalizeNamePart(String part) {
        return java.text.Normalizer.normalize(part, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^A-Za-z0-9]", "")
            .toLowerCase(Locale.ROOT);
    }
}