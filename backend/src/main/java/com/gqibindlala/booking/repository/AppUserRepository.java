package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLoginIdentifier(String loginIdentifier);
    boolean existsByLoginIdentifier(String loginIdentifier);
}