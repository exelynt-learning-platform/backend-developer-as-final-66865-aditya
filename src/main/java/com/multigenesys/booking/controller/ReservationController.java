package com.multigenesys.booking.controller;

import com.multigenesys.booking.dto.request.ReservationRequest;
import com.multigenesys.booking.dto.request.ReservationStatusUpdateRequest;
import com.multigenesys.booking.dto.response.ReservationResponse;
import com.multigenesys.booking.entity.ReservationStatus;
import com.multigenesys.booking.exception.BadRequestException;
import com.multigenesys.booking.security.UserPrincipal;
import com.multigenesys.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation booking, filtering, pagination, and status management endpoints")
@SecurityRequirement(name = "BearerAuth")
public class ReservationController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "startTime", "endTime", "totalPrice", "status", "createdAt", "updatedAt"
    );

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Create a new reservation",
            description = "Creates a reservation for a bookable resource. The user identity is securely resolved from the JWT token."
    )
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ReservationResponse response = reservationService.createReservation(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get filtered and paginated reservations",
            description = "Retrieves reservations with dynamic filtering by status, minPrice, and maxPrice. Admins can view all reservations, while regular users see only their own."
    )
    public ResponseEntity<Page<ReservationResponse>> getReservations(
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Invalid sortBy field: '" + sortBy + "'. Allowed fields are: " + ALLOWED_SORT_FIELDS);
        }

        if (!sortDirection.equalsIgnoreCase("ASC") && !sortDirection.equalsIgnoreCase("DESC")) {
            throw new BadRequestException("Invalid sortDirection: '" + sortDirection + "'. Allowed values are: 'ASC', 'DESC'");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReservationResponse> responses = reservationService.getReservations(
                status, minPrice, maxPrice, pageable, currentUser
        );

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Get reservation by ID",
            description = "Retrieves reservation details. Users can only view their own reservations; Admins can view any."
    )
    public ResponseEntity<ReservationResponse> getReservationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ReservationResponse response = reservationService.getReservationById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Update reservation status (PATCH)",
            description = "Updates the status of a reservation. Users can only cancel (CANCELLED) their own reservations; Admins can set any status."
    )
    public ResponseEntity<ReservationResponse> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "Cancel or delete a reservation",
            description = "Allows users to cancel their own reservation, or admins to delete/cancel any reservation."
    )
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        reservationService.deleteReservation(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
