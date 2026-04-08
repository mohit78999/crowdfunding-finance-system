package com.crowdfunding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a person who donates money to fundraisers on the platform.
 * Tracks total cumulative donations made by this donor.
 *
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Entity
@Table(name = "donor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donor_id")
    private Long donorId;

    // Donor's display name
    @NotBlank(message = "Donor name is required")
    @Column(name = "dname", nullable = false, length = 100)
    private String dname;

    // Unique email — used to identify donors across transactions
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Column(name = "demail", unique = true, nullable = false, length = 100)
    private String demail;

    // Contact phone number (optional, but good to have for communications)
    @Column(name = "dphone", length = 20)
    private String dphone;

    // Total amount this donor has contributed across ALL their transactions
    // This is the gross amount (before platform fee deduction)
    @Column(name = "total_donated", columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal totalDonated = BigDecimal.ZERO;

    // When this donor profile was created in the system
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.totalDonated == null) {
            this.totalDonated = BigDecimal.ZERO;
        }
    }
}
