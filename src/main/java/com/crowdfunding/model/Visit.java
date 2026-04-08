package com.crowdfunding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tracks each time a Donor views or interacts with a Fundraiser page.
 * Interest level is computed by VisitService based on cumulative visit count.
 *
 * GRASP: High Cohesion - VisitService exclusively handles Visit recording and interest level logic
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 * Design Pattern: MVC      - this is the Model (M) in the MVC pattern
 */
@Entity
@Table(name = "visit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    // Which donor is visiting
    @NotNull(message = "Donor is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    // Which fundraiser is being viewed
    @NotNull(message = "Fundraiser is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fundraiser_no", nullable = false)
    private Fundraiser fundraiser;

    // Timestamp of this visit
    @Column(name = "visit_date", updatable = false)
    private LocalDateTime visitDate;

    // How many minutes the donor spent on the page (can help infer interest)
    @Column(name = "duration")
    private Integer duration;

    // Computed engagement level based on cumulative visit count for this donor-fundraiser pair
    // Values: "Low" (1-2 visits), "Medium" (3-4), "High" (5-9), "Very High" (10+)
    @Column(name = "interest_level", length = 20)
    private String interestLevel = "Low";

    // Whether the donor just viewed the page or actually completed a transaction
    // Values: "View", "Transaction"
    @Column(name = "visit_type", length = 20)
    private String visitType = "View";

    @PrePersist
    protected void onCreate() {
        this.visitDate = LocalDateTime.now();
        if (this.interestLevel == null) this.interestLevel = "Low";
        if (this.visitType == null) this.visitType = "View";
    }
}
