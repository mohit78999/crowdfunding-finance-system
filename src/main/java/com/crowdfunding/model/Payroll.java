package com.crowdfunding.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Auto-generated record that tracks what an Administrator earned from a specific Transaction.
 * Created automatically by TransactionService whenever a donation is processed.
 *
 * Each transaction produces exactly one Payroll record.
 *
 * Design Pattern: MVC      - this is the Model (M) in the MVC pattern
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Entity
@Table(name = "payroll")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_id")
    private Long payrollId;

    // The admin who will receive this payout
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false)
    private Administrator administrator;

    // Which fundraiser this payout relates to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fundraiser_no", nullable = false)
    private Fundraiser fundraiser;

    // The transaction that triggered this payroll entry (one-to-one in practice)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    // 99% of the donation amount — what the admin actually receives
    @Column(name = "admin_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal adminEarnings;

    // 1% that the platform retained from this transaction
    @Column(name = "platform_fee_deducted", nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFeeDeducted;

    // When this payroll record was generated
    @Column(name = "payout_date")
    private LocalDateTime payoutDate;

    // "Automatic" for system-generated records; could be "Manual" for adjustments
    @Column(name = "payout_type", length = 50)
    private String payoutType = "Automatic";
}
