package com.crowdfunding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a crowdfunding campaign created and managed by an Administrator.
 * Tracks financial progress (goal vs raised vs remaining) and campaign lifecycle status.
 *
 * Status lifecycle: Planned → Active → Goal Reached / Closed
 *
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Entity
@Table(name = "fundraiser")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fundraiser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fundraiser_no")
    private Long fundraiserNo;

    // The administrator responsible for this fundraiser (owns the campaign)
    @NotNull(message = "Administrator is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false)
    private Administrator administrator;

    // Bank account details where funds will be disbursed
    @Column(name = "bank_details", length = 255)
    private String bankDetails;

    // Short descriptive title shown in listings
    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // Full description explaining the cause/project
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Target amount the fundraiser aims to collect
    @NotNull(message = "Goal amount is required")
    @DecimalMin(value = "1.00", message = "Goal amount must be at least 1.00")
    @Column(name = "goal_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal goalAmount;

    // How much has actually been collected so far (net amounts from transactions)
    @Column(name = "raised_amount", columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    // Convenience field: goal_amount - raised_amount (kept in sync by TransactionService)
    @Column(name = "remaining_amount", columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    // Campaign end date — after this date the fundraiser should be closed
    @Column(name = "deadline")
    private LocalDate deadline;

    // Current status of the fundraiser campaign
    // Allowed values: 'Planned', 'Active', 'Goal Reached', 'Closed'
    @Column(name = "status", length = 50)
    private String status = "Planned";

    // Person/entity that owns or benefits from this fundraiser
    @Column(name = "fundraiser_owner_name", length = 150)
    private String fundraiserOwnerName;

    // When the fundraiser was first created on the platform
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        // Set remaining = goal on creation since nothing has been raised yet
        if (this.raisedAmount == null) this.raisedAmount = BigDecimal.ZERO;
        if (this.remainingAmount == null) this.remainingAmount = this.goalAmount;
        if (this.status == null) this.status = "Planned";
    }
}
