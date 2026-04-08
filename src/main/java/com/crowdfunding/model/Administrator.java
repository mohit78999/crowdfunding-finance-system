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
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a platform administrator who owns and manages fundraisers.
 * Administrators earn 99% of each donation (the net_amount after platform fee).
 *
 * GRASP: Information Expert - Administrator knows its own earnings data
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean (via @Repository/services that use it)
 */
@Entity
@Table(name = "administrator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    // Administrator's full name - required
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Email must be unique across all administrators
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    // Running total of earnings from all fundraisers managed by this admin
    // Starts at 0.00 and grows as donations come in
    @Column(name = "total_earnings", columnDefinition = "DECIMAL(12,2) DEFAULT 0.00")
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    // Set automatically on first persist - never updated after that
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // An administrator can manage multiple fundraisers
    @OneToMany(mappedBy = "administrator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Fundraiser> fundraisers = new ArrayList<>();

    // Automatically stamp the creation time before the entity is first saved
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Ensure totalEarnings is never null in the database
        if (this.totalEarnings == null) {
            this.totalEarnings = BigDecimal.ZERO;
        }
    }
}
