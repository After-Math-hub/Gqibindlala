package com.gqibindlala.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gqibindlala.booking.dto.RegisterRequest;
import com.gqibindlala.booking.dto.CreateStaffRequest;
import com.gqibindlala.booking.dto.LoginRequest;
import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.Department;
import com.gqibindlala.booking.repository.AppUserRepository;
import com.gqibindlala.booking.repository.DepartmentRepository;
import com.gqibindlala.booking.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {
    private AppUserRepository userRepository;
    private DepartmentRepository departmentRepository;
    private EmailVerificationService emailVerificationService;
    private StaffActivation staffActivationService;
    private AppUser pendingVerificationUser;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(AppUserRepository.class);
        departmentRepository = org.mockito.Mockito.mock(DepartmentRepository.class);
        emailVerificationService = new EmailVerificationService() {
            @Override
            public void sendVerification(AppUser user) {
                pendingVerificationUser = user;
            }

            @Override
            public void verify(String token) {}
        };
        staffActivationService = org.mockito.Mockito.mock(StaffActivation.class);
        when(staffActivationService.createPendingAccount(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(12L);
            user.setActive(false);
            user.setStaffActivated(false);
            return user;
        });
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, departmentRepository, emailVerificationService, staffActivationService, passwordEncoder,
            new JwtService("a-secret-key-with-at-least-thirty-two-bytes", 86_400_000));
    }

    @Test
    void publicRegistrationCreatesOnlyCustomerAndStoresPasswordHash() {
        when(userRepository.existsByLoginIdentifier("customer@example.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(8L);
            return saved;
        });

        authService.register(new RegisterRequest(
            "Taylor Customer", " CUSTOMER@example.com ", "071 123 4567", "a-long-passphrase"));

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
            user.getRole() == AppRole.CUSTOMER
                && !user.isEmailVerified()
                && "customer@example.com".equals(user.getLoginIdentifier())
                && !user.getPasswordHash().equals("a-long-passphrase")
                && passwordEncoder.matches("a-long-passphrase", user.getPasswordHash())));
        assertNotNull(pendingVerificationUser);
        assertEquals("customer@example.com", pendingVerificationUser.getPersonalEmail());
    }

    @Test
    void loginNormalizesIdentifierAndAcceptsMatchingBcryptPassword() {
        AppUser admin = loginUser("admin@gqibindlala.co.za", AppRole.ADMIN, true, true, "correct-password");
        when(userRepository.findByLoginIdentifier("admin@gqibindlala.co.za"))
            .thenReturn(java.util.Optional.of(admin));

        var response = authService.login(new LoginRequest(" ADMIN@GQIBINDLALA.CO.ZA ", "correct-password"));

        assertEquals("admin@gqibindlala.co.za", response.user().loginIdentifier());
    }

    @Test
    void loginRejectsPasswordThatDoesNotMatchTheStoredBcryptHash() {
        AppUser admin = loginUser("admin@gqibindlala.co.za", AppRole.ADMIN, true, true, "original-password");
        when(userRepository.findByLoginIdentifier("admin@gqibindlala.co.za"))
            .thenReturn(java.util.Optional.of(admin));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("admin@gqibindlala.co.za", "different-password")));

        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid email or password", exception.getReason());
    }

    @Test
    void loginRejectsInactiveAdminEvenWhenPasswordMatches() {
        AppUser admin = loginUser("admin@gqibindlala.co.za", AppRole.ADMIN, false, true, "correct-password");
        when(userRepository.findByLoginIdentifier("admin@gqibindlala.co.za"))
            .thenReturn(java.util.Optional.of(admin));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("admin@gqibindlala.co.za", "correct-password")));

        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginRejectsStaffAccountThatHasNotCompletedActivation() {
        AppUser staff = loginUser("staff@example.com", AppRole.DEPARTMENT, true, false, "correct-password");
        when(userRepository.findByLoginIdentifier("staff@example.com"))
            .thenReturn(java.util.Optional.of(staff));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> authService.login(new LoginRequest("staff@example.com", "correct-password")));

        assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void departmentStaffRequiresDepartmentAssignment() {
        assertThrows(ResponseStatusException.class, () -> authService.createStaff(new CreateStaffRequest(
            "Chair desk", "chair@example.com", "0710000000", AppRole.DEPARTMENT, null)));
    }

    @Test
    void paymentStaffMustBeAssignedToFinance() {
        Department chairs = new Department();
        chairs.setId(5L);
        chairs.setSlug("chair");
        when(departmentRepository.findById(5L)).thenReturn(java.util.Optional.of(chairs));
        assertThrows(ResponseStatusException.class, () -> authService.createStaff(new CreateStaffRequest(
            "Payments desk", "payments@example.com", "0710000000", AppRole.PAYMENTS, 5L)));
    }

    private AppUser loginUser(String identifier, AppRole role, boolean active, boolean staffActivated, String password) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setFullName("Test Account");
        user.setLoginIdentifier(identifier);
        user.setPhone("Not provided");
        user.setRole(role);
        user.setActive(active);
        user.setStaffActivated(staffActivated);
        user.setPasswordHash(passwordEncoder.encode(password));
        return user;
    }

    @Test
    void staffLoginUsesGeneratedDepartmentIdentifierAndActivationEmail() {
        Department chairs = new Department();
        chairs.setId(7L);
        chairs.setName("Chairs");
        chairs.setSlug("chair");
        chairs.setLoginDomain("chair-gqi.co.za");
        when(departmentRepository.findById(7L)).thenReturn(java.util.Optional.of(chairs));
        when(userRepository.existsByLoginIdentifier("jsmith@chair-gqi.co.za")).thenReturn(false);
        var response = authService.createStaff(new CreateStaffRequest(
            "Jamie Smith", "jamie@example.com", "0710000000", AppRole.DEPARTMENT, 7L));

        assertEquals("jsmith@chair-gqi.co.za", response.loginIdentifier());
        assertEquals("jamie@example.com", response.personalEmail());
        assertEquals(false, response.active());
        assertEquals(false, response.staffActivated());
        verify(staffActivationService).createPendingAccount(org.mockito.ArgumentMatchers.argThat(user ->
            user.getRole() == AppRole.DEPARTMENT
                && "jamie@example.com".equals(user.getPersonalEmail())
                && user.getDepartment().getId().equals(7L)));
    }
}