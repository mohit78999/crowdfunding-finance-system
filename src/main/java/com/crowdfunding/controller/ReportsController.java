package com.crowdfunding.controller;

import com.crowdfunding.model.Visit;
import com.crowdfunding.repository.AdministratorRepository;
import com.crowdfunding.repository.DonorRepository;
import com.crowdfunding.repository.FundraiserRepository;
import com.crowdfunding.repository.TransactionRepository;
import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.FundraiserService;
import com.crowdfunding.service.TransactionService;
import com.crowdfunding.service.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the Reports & Analytics page.
 *
 * GRASP: Controller — delegates all data retrieval to services/repositories; no logic here.
 * Design Pattern: MVC — populates Model for the reports/index Thymeleaf view.
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean.
 */
@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final AdministratorRepository administratorRepository;
    private final DonorRepository donorRepository;
    private final FundraiserRepository fundraiserRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final FundraiserService fundraiserService;
    private final DonorService donorService;
    private final VisitService visitService;

    public ReportsController(AdministratorRepository administratorRepository,
                              DonorRepository donorRepository,
                              FundraiserRepository fundraiserRepository,
                              TransactionRepository transactionRepository,
                              TransactionService transactionService,
                              FundraiserService fundraiserService,
                              DonorService donorService,
                              VisitService visitService) {
        this.administratorRepository = administratorRepository;
        this.donorRepository = donorRepository;
        this.fundraiserRepository = fundraiserRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.fundraiserService = fundraiserService;
        this.donorService = donorService;
        this.visitService = visitService;
    }

    /** GET /reports */
    @GetMapping
    public String reportsIndex(Model model) {
        // Platform statistics
        model.addAttribute("totalAdmins", administratorRepository.count());
        model.addAttribute("totalDonors", donorRepository.count());
        model.addAttribute("totalFundraisers", fundraiserRepository.count());
        model.addAttribute("activeFundraisers", fundraiserRepository.countByStatus("Active"));
        model.addAttribute("totalTransactions", transactionRepository.count());

        BigDecimal totalRaised = fundraiserRepository.sumTotalRaised();
        BigDecimal totalPlatformFees = transactionService.getTotalPlatformFees();
        BigDecimal totalToFundraisers = totalRaised.subtract(totalPlatformFees);

        model.addAttribute("totalRaised", totalRaised);
        model.addAttribute("totalPlatformFees", totalPlatformFees);
        model.addAttribute("totalToFundraisers", totalToFundraisers);

        // Top donors sorted by total donated descending (top 10)
        model.addAttribute("topDonors", donorService.getAllDonors().stream()
                .sorted((a, b) -> b.getTotalDonated().compareTo(a.getTotalDonated()))
                .limit(10)
                .collect(Collectors.toList()));

        // All fundraisers for progress table
        model.addAttribute("fundraiserProgress", fundraiserService.getAllFundraisers());

        // High interest visits (Very High or High interest level)
        List<Visit> highInterestVisits = visitService.getAllVisits().stream()
                .filter(v -> "Very High".equals(v.getInterestLevel()) || "High".equals(v.getInterestLevel()))
                .collect(Collectors.toList());
        model.addAttribute("highInterestVisits", highInterestVisits);

        return "reports/index";
    }
}
