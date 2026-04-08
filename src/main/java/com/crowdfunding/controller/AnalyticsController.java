package com.crowdfunding.controller;

import com.crowdfunding.model.Administrator;
import com.crowdfunding.model.Donor;
import com.crowdfunding.repository.PayrollRepository;
import com.crowdfunding.repository.TransactionRepository;
import com.crowdfunding.repository.VisitRepository;
import com.crowdfunding.service.AdministratorService;
import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.FundraiserService;
import com.crowdfunding.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final FundraiserService fundraiserService;
    private final TransactionService transactionService;
    private final DonorService donorService;
    private final AdministratorService administratorService;
    private final TransactionRepository transactionRepository;
    private final VisitRepository visitRepository;
    private final PayrollRepository payrollRepository;

    public AnalyticsController(FundraiserService fundraiserService,
                                TransactionService transactionService,
                                DonorService donorService,
                                AdministratorService administratorService,
                                TransactionRepository transactionRepository,
                                VisitRepository visitRepository,
                                PayrollRepository payrollRepository) {
        this.fundraiserService = fundraiserService;
        this.transactionService = transactionService;
        this.donorService = donorService;
        this.administratorService = administratorService;
        this.transactionRepository = transactionRepository;
        this.visitRepository = visitRepository;
        this.payrollRepository = payrollRepository;
    }

    @GetMapping
    public String analyticsIndex(Model model) {
        model.addAttribute("activeFundraisers", fundraiserService.getActiveFundraisers());
        model.addAttribute("recentTransactions", transactionService.getRecentTransactions());

        // --- Donor Engagement ---
        List<Donor> donors = donorService.getAllDonors();
        model.addAttribute("donors", donors);

        Map<Long, Long> donorTransactionCount = new HashMap<>();
        Map<Long, Long> donorVisitCount = new HashMap<>();
        Map<Long, Long> donorFundraisersVisited = new HashMap<>();

        for (Donor d : donors) {
            long txCount = transactionRepository.findByDonorDonorId(d.getDonorId()).size();
            var visits = visitRepository.findByDonorDonorId(d.getDonorId());
            long uniqueFundraisers = visits.stream()
                    .map(v -> v.getFundraiser().getFundraiserNo()).distinct().count();
            donorTransactionCount.put(d.getDonorId(), txCount);
            donorVisitCount.put(d.getDonorId(), (long) visits.size());
            donorFundraisersVisited.put(d.getDonorId(), uniqueFundraisers);
        }
        model.addAttribute("donorTransactionCount", donorTransactionCount);
        model.addAttribute("donorVisitCount", donorVisitCount);
        model.addAttribute("donorFundraisersVisited", donorFundraisersVisited);

        // --- Administrator Dashboard ---
        List<Administrator> administrators = administratorService.getAllAdministrators();
        model.addAttribute("administrators", administrators);

        Map<Long, Long> adminTransactionCount = new HashMap<>();
        Map<Long, Long> adminPayoutCount = new HashMap<>();
        Map<Long, Long> adminActiveFundraisers = new HashMap<>();

        for (Administrator a : administrators) {
            long txCount = payrollRepository.findByAdministratorAdminId(a.getAdminId()).size();
            long payouts = payrollRepository.findByAdministratorAdminId(a.getAdminId()).size();
            long activeFr = a.getFundraisers().stream()
                    .filter(f -> "Active".equals(f.getStatus())).count();
            adminTransactionCount.put(a.getAdminId(), txCount);
            adminPayoutCount.put(a.getAdminId(), payouts);
            adminActiveFundraisers.put(a.getAdminId(), activeFr);
        }
        model.addAttribute("adminTransactionCount", adminTransactionCount);
        model.addAttribute("adminPayoutCount", adminPayoutCount);
        model.addAttribute("adminActiveFundraisers", adminActiveFundraisers);

        return "analytics/index";
    }
}
