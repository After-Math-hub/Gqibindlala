package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.model.AppRole;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
    Optional<AppUser> findByLoginIdentifier(String loginIdentifier);
    boolean existsByLoginIdentifier(String loginIdentifier);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
    List<AppUser> findByRoleInOrderByFullNameAsc(List<AppRole> roles);
}