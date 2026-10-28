package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.StaffActivationToken;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffActivationTokenRepository extends JpaRepository<StaffActivationToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StaffActivationToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
    List<StaffActivationToken> findAllByUser_IdAndUsedAtIsNull(Long userId);
}