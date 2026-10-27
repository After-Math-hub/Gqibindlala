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
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(AppUserRepository.class);
        departmentRepository = org.mockito.Mockito.mock(DepartmentRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, departmentRepository, passwordEncoder,
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

        var response = authService.register(new RegisterRequest(
            "Taylor Customer", " CUSTOMER@example.com ", "071 123 4567", "a-long-passphrase"));

        assertEquals(AppRole.CUSTOMER, response.user().role());
        assertEquals("customer@example.com", response.user().loginIdentifier());
        assertNotNull(response.token());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
            user.getRole() == AppRole.CUSTOMER
                && !user.getPasswordHash().equals("a-long-passphrase")
                && passwordEncoder.matches("a-long-passphrase", user.getPasswordHash())));
    }

    @Test
    void departmentStaffRequiresDepartmentAssignment() {
        assertThrows(ResponseStatusException.class, () -> authService.createStaff(new CreateStaffRequest(
            "Chair desk", "chair@example.com", "0710000000", "another-long-password",
            AppRole.DEPARTMENT, null)));
    }

    @Test
    void paymentStaffMustBeAssignedToFinance() {
        Department chairs = new Department();
        chairs.setId(5L);
        chairs.setSlug("chair");
        when(departmentRepository.findById(5L)).thenReturn(java.util.Optional.of(chairs));
        assertThrows(ResponseStatusException.class, () -> authService.createStaff(new CreateStaffRequest(
            "Payments desk", "payments@example.com", "0710000000", "another-long-password",
            AppRole.PAYMENTS, 5L)));
    }

    @Test
    void staffLoginUsesGeneratedDepartmentIdentifierWithoutRequiringMailbox() {
        Department chairs = new Department();
        chairs.setId(7L);
        chairs.setName("Chairs");
        chairs.setSlug("chair");
        chairs.setLoginDomain("chair-gqi.co.za");
        when(departmentRepository.findById(7L)).thenReturn(java.util.Optional.of(chairs));
        when(userRepository.existsByLoginIdentifier("jsmith@chair-gqi.co.za")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        var response = authService.createStaff(new CreateStaffRequest(
            "Jamie Smith", null, "0710000000", "another-long-password", AppRole.DEPARTMENT, 7L));

        assertEquals("jsmith@chair-gqi.co.za", response.loginIdentifier());
        assertEquals(null, response.personalEmail());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
            user.getRole() == AppRole.DEPARTMENT
                && user.getPersonalEmail() == null
                && user.getDepartment().getId().equals(7L)));
    }
}