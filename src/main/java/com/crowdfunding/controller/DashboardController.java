package com.crowdfunding.controller;

import com.crowdfunding.repository.AdministratorRepository;
import com.crowdfunding.repository.DonorRepository;
import com.crowdfunding.repository.FundraiserRepository;
import com.crowdfunding.repository.TransactionRepository;
import com.crowdfunding.repository.VisitRepository;
import com.crowdfunding.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles the main dashboard page at the root URL.
 *
 * GRASP: Controller — GRASP Controller principle: the UI delegates all requests
 *        to this controller, which collects statistics from repositories and
 *        passes them to the Thymeleaf view. The controller does NOT compute
 *        anything itself — it delegates to services/repositories.
 *
 * Design Pattern: MVC   — this is the C in MVC; populates the Model for the View.
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean.
 */
@Controller
public class DashboardController {

    // Using repositories directly here for simple count queries —
    // no complex business logic, just aggregation for display
    private final AdministratorRepository administratorRepository;
    private final DonorRepository donorRepository;
    private final FundraiserRepository fundraiserRepository;
    private final TransactionRepository transactionRepository;
    private final VisitRepository visitRepository;
    private final TransactionService transactionService;

    public DashboardController(AdministratorRepository administratorRepository,
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

    /**
     * Render the main dashboard with platform-wide statistics.
     * GET /
     *
     * Each stat is passed as a named model attribute — clean, readable, no DTO needed.
     */
    @GetMapping("/")
    public String dashboard(Model model) {

        // Platform-wide counts for the stat cards
        model.addAttribute("totalAdmins",        administratorRepository.count());
        model.addAttribute("totalVisits",         visitRepository.count());
        model.addAttribute("totalDonors",         donorRepository.count());
        model.addAttribute("totalFundraisers",    fundraiserRepository.count());
        model.addAttribute("activeFundraisers",   fundraiserRepository.countByStatus("Active"));
        model.addAttribute("totalTransactions",   transactionRepository.count());

        // Financial totals for the banner and fee tracking
        model.addAttribute("totalRaised",         fundraiserRepository.sumTotalRaised());
        model.addAttribute("totalGoalAmount",     fundraiserRepository.sumTotalGoal());
        model.addAttribute("totalPlatformFees",   transactionService.getTotalPlatformFees());

        // Recent activity feed — last 10 transactions shown in the table
        model.addAttribute("recentTransactions",  transactionService.getRecentTransactions());

        // Top fundraisers for the dashboard panel
        model.addAttribute("topFundraisers", fundraiserRepository.findAll());

        return "dashboard";
    }
}
