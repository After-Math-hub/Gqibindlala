package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.Equipment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
	@Override
	@org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
	Optional<Equipment> findById(Long id);

	@Override
	@org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
	java.util.List<Equipment> findAll();

	@org.springframework.data.jpa.repository.EntityGraph(attributePaths = "department")
	java.util.List<Equipment> findByDepartmentActiveTrue();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT equipment FROM Equipment equipment JOIN FETCH equipment.department WHERE equipment.id = :id")
	Optional<Equipment> findByIdForUpdate(@Param("id") Long id);
}
