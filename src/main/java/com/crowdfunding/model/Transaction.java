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
import java.time.LocalDateTime;

/**
 * Records each individual donation made by a Donor to a Fundraiser.
 *
 * Fee structure (applied by TransactionService - the Information Expert):
 *   - platform_fee = amount * 1%   → kept by the platform
 *   - net_amount   = amount * 99%  → goes to fundraiser/admin
 *
 * GRASP: Information Expert - TransactionService (not this entity) owns the fee calculation logic
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    // The donor who made this donation
    @NotNull(message = "Donor is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    // The fundraiser that received this donation
    @NotNull(message = "Fundraiser is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fundraiser_no", nullable = false)
    private Fundraiser fundraiser;

    // Gross amount entered by the donor
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum donation is 1.00")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // 1% of amount — platform revenue
    @Column(name = "platform_fee", precision = 12, scale = 2)
    private BigDecimal platformFee;

    // 99% of amount — what the fundraiser actually receives
    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    // How the donor paid: e.g., "Credit Card", "UPI", "Bank Transfer", "PayPal"
    @NotBlank(message = "Payment mode is required")
    @Column(name = "payment_mode", nullable = false, length = 50)
    private String paymentMode;

    // Exact timestamp of when this transaction was processed
    @Column(name = "transaction_date", updatable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }

    /**
     * Design Pattern: Builder — provides a readable, step-by-step way to construct
     * a Transaction object with multiple fields, avoiding long constructor calls.
     *
     * Used by TransactionService.processDonation() to build the Transaction before saving.
     * This keeps object construction clean and explicit — each field is named at the call site.
     */
    public static class Builder {

        private Donor donor;
        private Fundraiser fundraiser;
        private BigDecimal amount;
        private BigDecimal platformFee;
        private BigDecimal netAmount;
        private String paymentMode;

        // Each setter returns 'this' so calls can be chained fluently
        public Builder donor(Donor donor)               { this.donor = donor;               return this; }
        public Builder fundraiser(Fundraiser fundraiser){ this.fundraiser = fundraiser;       return this; }
        public Builder amount(BigDecimal amount)        { this.amount = amount;               return this; }
        public Builder platformFee(BigDecimal fee)      { this.platformFee = fee;             return this; }
        public Builder netAmount(BigDecimal net)        { this.netAmount = net;               return this; }
        public Builder paymentMode(String mode)         { this.paymentMode = mode;            return this; }

        /**
         * Assembles and returns the fully configured Transaction object.
         * @PrePersist on Transaction auto-sets transactionDate when saved.
         */
        public Transaction build() {
            Transaction t = new Transaction();
            t.setDonor(donor);
            t.setFundraiser(fundraiser);
            t.setAmount(amount);
            t.setPlatformFee(platformFee);
            t.setNetAmount(netAmount);
            t.setPaymentMode(paymentMode);
            return t;
        }
    }
}
