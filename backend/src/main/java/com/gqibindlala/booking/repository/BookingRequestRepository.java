package com.gqibindlala.booking.repository;

import com.gqibindlala.booking.model.BookingRequest;
import com.gqibindlala.booking.model.PaymentStatus;
import com.gqibindlala.booking.model.FinanceRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
	@Override
	@EntityGraph(attributePaths = {"items", "items.equipment", "items.equipment.department", "items.quotationIssuedBy", "customer"})
	Optional<BookingRequest> findById(Long id);

	@EntityGraph(attributePaths = {"items", "items.equipment", "items.equipment.department", "items.quotationIssuedBy", "customer"})
	List<BookingRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	@EntityGraph(attributePaths = {"items", "items.equipment", "items.equipment.department", "items.quotationIssuedBy", "customer"})
	List<BookingRequest> findByPaymentStatusOrderByCreatedAtAsc(PaymentStatus paymentStatus);

	@EntityGraph(attributePaths = {"items", "items.equipment", "items.equipment.department", "customer"})
	List<BookingRequest> findByFinanceRequestStatusInOrderByCreatedAtDesc(List<FinanceRequestStatus> statuses);

	@Override
	@EntityGraph(attributePaths = {"items", "items.equipment", "items.equipment.department", "items.quotationIssuedBy", "customer"})
	List<BookingRequest> findAll();
}
