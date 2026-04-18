package com.crowdfunding.service;

import com.crowdfunding.model.Donor;
import com.crowdfunding.model.Fundraiser;
import com.crowdfunding.model.Payroll;
import com.crowdfunding.model.Transaction;
import com.crowdfunding.repository.DonorRepository;
import com.crowdfunding.repository.FundraiserRepository;
import com.crowdfunding.repository.PayrollRepository;
import com.crowdfunding.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Central service for processing donations — the heart of the platform's financial logic.
 *
 * GRASP: Information Expert — TransactionService owns all knowledge needed to complete
 *        a donation: fee calculation (1%/99% split), entity updates, and payroll creation.
 *        No other class needs to know HOW a donation is processed.
 *
 * GRASP: Low Coupling — depends on repository interfaces, not concrete implementations.
 *
 * Design Pattern: Singleton — Spring IoC manages this as a single shared instance (@Service).
 * Design Pattern: Factory   — Spring IoC creates and injects all dependencies via constructor injection.
 * Design Pattern: DAO       — all persistence goes through repository (DAO) interfaces.
 */
@Service
public class TransactionService {

    // DAO Pattern — all database access through repository interfaces
    private final TransactionRepository transactionRepository;
    private final PayrollRepository payrollRepository;
    private final FundraiserRepository fundraiserRepository;
    private final DonorRepository donorRepository;
    private final FundraiserService fundraiserService;
    private final AdministratorService administratorService;
    private final DonorService donorService;
    private final VisitService visitService;

    public TransactionService(
            TransactionRepository transactionRepository,
            PayrollRepository payrollRepository,
            FundraiserRepository fundraiserRepository,
            DonorRepository donorRepository,
            FundraiserService fundraiserService,
            AdministratorService administratorService,
            DonorService donorService,
            VisitService visitService) {
        this.transactionRepository = transactionRepository;
        this.payrollRepository = payrollRepository;
        this.fundraiserRepository = fundraiserRepository;
        this.donorRepository = donorRepository;
        this.fundraiserService = fundraiserService;
        this.administratorService = administratorService;
        this.donorService = donorService;
        this.visitService = visitService;
    }

    /**
     * The core business operation: process a donation end-to-end.
     *
     * GRASP: Information Expert — this method encapsulates ALL knowledge needed
     *        to complete a donation: fee rules, entity updates, and payroll creation.
     *
     * @Transactional ensures that if ANY step fails, ALL database changes are rolled back.
     *
     * @param donorId       the donor making the donation
     * @param fundraiserNo  the target fundraiser
     * @param amount        the gross donation amount entered by the donor
     * @param paymentMode   payment method (e.g., "Credit Card", "UPI")
     * @return the persisted Transaction record
     */
    @Transactional
    public Transaction processDonation(Long donorId, Long fundraiserNo, BigDecimal amount, String paymentMode) {

        // --- Step 1: Load entities via DAO (repository) layer ---
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + donorId));

        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserNo)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found with ID: " + fundraiserNo));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Donation amount must be greater than zero");
        }

        // --- Validation: fundraiser must be Active ---
        if (!"Active".equals(fundraiser.getStatus())) {
            throw new IllegalArgumentException("Fundraiser is not active. Current status: " + fundraiser.getStatus());
        }

        // --- Validation: amount must not exceed remaining goal ---
        BigDecimal remaining = fundraiser.getRemainingAmount() != null ? fundraiser.getRemainingAmount() : fundraiser.getGoalAmount();
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Donation amount (₹" + amount + ") exceeds remaining goal (₹" + remaining + ")");
        }

        // --- Step 2: Calculate fees (GRASP Information Expert — owns the fee formula) ---
        // Platform retains 1% of every donation as operating revenue
        BigDecimal platformFee = amount.multiply(new BigDecimal("0.01"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        // Fundraiser admin receives the remaining 99%
        BigDecimal netAmount = amount.multiply(new BigDecimal("0.99"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        // --- Step 3: Build and persist the Transaction record ---
        // Design Pattern: Builder — uses Transaction.Builder to construct the object
        // step-by-step instead of a long constructor call, making each field explicit.
        Transaction transaction = new Transaction.Builder()
                .donor(donor)
                .fundraiser(fundraiser)
                .amount(amount)
                .platformFee(platformFee)
                .netAmount(netAmount)
                .paymentMode(paymentMode)
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        // --- Step 4: Create Payroll record ---
        // Design Pattern: Factory — Spring IoC (via @Autowired constructor injection) manages
        // object creation. TransactionService itself is injected by Spring, not created with 'new'.
        Payroll payroll = new Payroll();
        payroll.setAdministrator(fundraiser.getAdministrator());
        payroll.setFundraiser(fundraiser);
        payroll.setTransaction(savedTransaction);
        payroll.setAdminEarnings(netAmount);           // 99% goes to admin
        payroll.setPlatformFeeDeducted(platformFee);  // 1% kept by platform
        payroll.setPayoutDate(java.time.LocalDateTime.now());
        payroll.setPayoutType("Automatic");
        payrollRepository.save(payroll);

        // --- Step 5: Update fundraiser progress (raised amount, remaining) ---
        fundraiserService.updateProgress(fundraiserNo, netAmount);

        // --- Step 6: Update donor's running total donated ---
        donorService.updateTotalDonated(donorId, amount);

        // --- Step 7: Credit administrator earnings ---
        administratorService.updateEarnings(fundraiser.getAdministrator().getAdminId(), netAmount);

        // --- Step 8: Auto-record visit with type "Transaction" ---
        try {
            visitService.recordVisit(donorId, fundraiserNo, 0, "Transaction");
        } catch (Exception ignored) {
            // Visit recording is non-critical — don't roll back the donation
        }

        return savedTransaction;
    }

    /** Get all transactions. */
    public List<Transaction> getAllTransactions() {
        // DAO Pattern — delegate to repository
        return transactionRepository.findAll();
    }

    /** Find a specific transaction by its ID. */
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    /** All transactions made by a specific donor. */
    public List<Transaction> getTransactionsByDonor(Long donorId) {
        return transactionRepository.findByDonorDonorId(donorId);
    }

    /** All transactions for a specific fundraiser. */
    public List<Transaction> getTransactionsByFundraiser(Long fundraiserNo) {
        return transactionRepository.findByFundraiserFundraiserNo(fundraiserNo);
    }

    /** Most recent 10 transactions — for the dashboard activity feed. */
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc();
    }

    /** Platform-wide total fees collected. */
    public BigDecimal getTotalPlatformFees() {
        return transactionRepository.sumTotalPlatformFees();
    }

    /** Total gross donations across the platform. */
    public BigDecimal getTotalDonations() {
        return transactionRepository.sumTotalAmount();
    }
}
