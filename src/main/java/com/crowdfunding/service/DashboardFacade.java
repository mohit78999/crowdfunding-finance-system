package com.crowdfunding.service;

import com.crowdfunding.model.Fundraiser;
import com.crowdfunding.model.Transaction;
import com.crowdfunding.repository.AdministratorRepository;
import com.crowdfunding.repository.DonorRepository;
import com.crowdfunding.repository.FundraiserRepository;
import com.crowdfunding.repository.TransactionRepository;
import com.crowdfunding.repository.VisitRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Design Pattern: Facade — provides a single simplified interface to the
 * dashboard's data needs, hiding the complexity of querying 5 different
 * repositories and services behind simple method calls.
 *
 * DashboardController calls this facade instead of talking to multiple
 * repositories directly — reducing coupling and keeping the controller clean.
 *
 * Design Pattern: Singleton — Spring IoC manages this as a singleton bean (@Service).
 */
@Service
public class DashboardFacade {

    // Facade internally coordinates multiple subsystems (repositories + service)
    private final AdministratorRepository administratorRepository;
    private final DonorRepository donorRepository;
    private final FundraiserRepository fundraiserRepository;
    private final TransactionRepository transactionRepository;
    private final VisitRepository visitRepository;
    private final TransactionService transactionService;

    public DashboardFacade(AdministratorRepository administratorRepository,
                           DonorRepository donorRepository,
                           FundraiserRepository fundraiserRepository,
                           TransactionRepository transactionRepository,
                           VisitRepository visitRepository,
                           TransactionService transactionService) {
        this.administratorRepository = administratorRepository;
        this.donorRepository = donorRepository;
        this.fundraiserRepository = fundraiserRepository;
        this.transactionRepository = transactionRepository;
        this.visitRepository = visitRepository;
        this.transactionService = transactionService;
    }

    /** Total number of administrators registered on the platform. */
    public long getTotalAdmins() {
        return administratorRepository.count();
    }

    /** Total number of donors registered. */
    public long getTotalDonors() {
        return donorRepository.count();
    }

    /** Total visits recorded across all fundraisers. */
    public long getTotalVisits() {
        return visitRepository.count();
    }

    /** Total number of fundraisers ever created. */
    public long getTotalFundraisers() {
        return fundraiserRepository.count();
    }

    /** How many fundraisers are currently Active. */
    public long getActiveFundraisers() {
        return fundraiserRepository.countByStatus("Active");
    }

    /** Total number of donation transactions processed. */
    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    /** Sum of all net amounts raised across all fundraisers. */
    public BigDecimal getTotalRaised() {
        return fundraiserRepository.sumTotalRaised();
    }

    /** Sum of all fundraiser goal amounts. */
    public BigDecimal getTotalGoalAmount() {
        return fundraiserRepository.sumTotalGoal();
    }

    /** Total platform fees collected (1% of every donation). */
    public BigDecimal getTotalPlatformFees() {
        return transactionService.getTotalPlatformFees();
    }

    /** Most recent 10 transactions for the activity feed. */
    public List<Transaction> getRecentTransactions() {
        return transactionService.getRecentTransactions();
    }

    /** All fundraisers — used for the top fundraisers table. */
    public List<Fundraiser> getAllFundraisers() {
        return fundraiserRepository.findAll();
    }
}
