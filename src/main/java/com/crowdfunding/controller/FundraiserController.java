package com.crowdfunding.controller;

import com.crowdfunding.model.Administrator;
import com.crowdfunding.model.Fundraiser;
import com.crowdfunding.repository.TransactionRepository;
import com.crowdfunding.repository.VisitRepository;
import com.crowdfunding.service.AdministratorService;
import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.FundraiserService;
import com.crowdfunding.service.TransactionService;
import com.crowdfunding.service.VisitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Handles all HTTP requests related to Fundraiser management.
 *
 * GRASP: Controller — GRASP Controller principle: this controller orchestrates calls to
 *        FundraiserService and AdministratorService to serve fundraiser-related views.
 *        All campaign-change logic happens in the services, not here.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/fundraisers")
public class FundraiserController {

    private final FundraiserService fundraiserService;
    private final AdministratorService administratorService;
    private final TransactionService transactionService;
    private final VisitService visitService;
    private final VisitRepository visitRepository;
    private final TransactionRepository transactionRepository;
    private final DonorService donorService;

    public FundraiserController(FundraiserService fundraiserService,
                                AdministratorService administratorService,
                                TransactionService transactionService,
                                VisitService visitService,
                                VisitRepository visitRepository,
                                TransactionRepository transactionRepository,
                                DonorService donorService) {
        this.fundraiserService = fundraiserService;
        this.administratorService = administratorService;
        this.transactionService = transactionService;
        this.visitService = visitService;
        this.visitRepository = visitRepository;
        this.transactionRepository = transactionRepository;
        this.donorService = donorService;
    }

    /** List all fundraisers. GET /fundraisers */
    @GetMapping
    public String listFundraisers(Model model) {
        java.util.List<Fundraiser> fundraisers = fundraiserService.getAllFundraisers();
        model.addAttribute("fundraisers", fundraisers);

        // Build engagement maps: fundraiserNo -> visitCount / donorCount
        java.util.Map<Long, Long> visitCounts = new java.util.HashMap<>();
        java.util.Map<Long, Long> donorCounts = new java.util.HashMap<>();
        for (Fundraiser f : fundraisers) {
            visitCounts.put(f.getFundraiserNo(), (long) visitRepository.findByFundraiserFundraiserNo(f.getFundraiserNo()).size());
            donorCounts.put(f.getFundraiserNo(), (long) transactionRepository.findByFundraiserFundraiserNo(f.getFundraiserNo()).stream().map(t -> t.getDonor().getDonorId()).distinct().count());
        }
        model.addAttribute("visitCounts", visitCounts);
        model.addAttribute("donorCounts", donorCounts);
        return "fundraisers/list";
    }

    /** Show the create form. GET /fundraisers/add */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("fundraiser", new Fundraiser());
        model.addAttribute("administrators", administratorService.getAllAdministrators());
        model.addAttribute("statusOptions", new String[]{"Planned", "Active", "Goal Reached", "Closed"});
        return "fundraisers/add";
    }

    /**
     * Submit the new fundraiser form.
     * POST /fundraisers/add
     *
     * We use individual @RequestParam bindings because the administrator field
     * is a nested entity — Spring MVC cannot auto-bind it from a plain adminId string.
     * This controller manually builds the Fundraiser, then delegates to FundraiserService.
     */
    @PostMapping("/add")
    public String addFundraiser(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long adminId,
            @RequestParam BigDecimal goalAmount,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @RequestParam(required = false) String fundraiserOwnerName,
            @RequestParam(required = false) String bankDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Administrator admin = administratorService.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

            Fundraiser fundraiser = new Fundraiser();
            fundraiser.setTitle(title);
            fundraiser.setDescription(description);
            fundraiser.setAdministrator(admin);
            fundraiser.setGoalAmount(goalAmount);
            fundraiser.setStatus(status != null ? status : "Planned");
            fundraiser.setDeadline(deadline);
            fundraiser.setFundraiserOwnerName(fundraiserOwnerName);
            fundraiser.setBankDetails(bankDetails);

            fundraiserService.save(fundraiser);
            redirectAttributes.addFlashAttribute("successMessage", "Fundraiser created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/fundraisers/add";
        }

        return "redirect:/fundraisers";
    }

    /** Show the edit form for an existing fundraiser. GET /fundraisers/edit/{id} */

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Fundraiser fundraiser = fundraiserService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found with ID: " + id));
        model.addAttribute("fundraiser", fundraiser);
        model.addAttribute("administrators", administratorService.getAllAdministrators());
        model.addAttribute("statusOptions", new String[]{"Planned", "Active", "Goal Reached", "Closed"});
        return "fundraisers/edit";
    }

    /** Submit updated fundraiser data. POST /fundraisers/edit/{id} */
    @PostMapping("/edit/{id}")
    public String updateFundraiser(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Long adminId,
            @RequestParam BigDecimal goalAmount,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @RequestParam(required = false) String fundraiserOwnerName,
            @RequestParam(required = false) String bankDetails,
            RedirectAttributes redirectAttributes) {

        try {
            // Load existing fundraiser to preserve raised/remaining amounts
            Fundraiser fundraiser = fundraiserService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found"));

            Administrator admin = administratorService.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

            fundraiser.setTitle(title);
            fundraiser.setDescription(description);
            fundraiser.setAdministrator(admin);
            fundraiser.setGoalAmount(goalAmount);
            fundraiser.setStatus(status != null ? status : fundraiser.getStatus());
            fundraiser.setDeadline(deadline);
            fundraiser.setFundraiserOwnerName(fundraiserOwnerName);
            fundraiser.setBankDetails(bankDetails);

            fundraiserService.save(fundraiser);
            redirectAttributes.addFlashAttribute("successMessage", "Fundraiser updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/fundraisers/edit/" + id;
        }

        return "redirect:/fundraisers";
    }

    /** Delete a fundraiser. GET /fundraisers/delete/{id} */
    @GetMapping("/delete/{id}")
    public String deleteFundraiser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        fundraiserService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Fundraiser deleted successfully!");
        return "redirect:/fundraisers";
    }

    /** View detailed campaign info. GET /fundraisers/{id} */
    @GetMapping("/{id}")
    public String viewFundraiser(@PathVariable Long id, Model model) {
        Fundraiser fundraiser = fundraiserService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found with ID: " + id));
        model.addAttribute("fundraiser", fundraiser);

        var transactions = transactionService.getTransactionsByFundraiser(id);
        var visits = visitService.getVisitsByFundraiser(id);
        long uniqueVisitors = transactions.stream().map(t -> t.getDonor().getDonorId()).distinct().count();
        long daysRemaining = fundraiser.getDeadline() != null
                ? Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), fundraiser.getDeadline())) : 0;

        model.addAttribute("transactions", transactions);
        model.addAttribute("visits", visits);
        model.addAttribute("uniqueVisitors", uniqueVisitors);
        model.addAttribute("daysRemaining", daysRemaining);
        model.addAttribute("allDonors", donorService.getAllDonors());
        return "fundraisers/detail";
    }

    /** View audit trail (transactions + visits) for a fundraiser. GET /fundraisers/{id}/audit */
    @GetMapping("/{id}/audit")
    public String viewAudit(@PathVariable Long id, Model model) {
        Fundraiser fundraiser = fundraiserService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found with ID: " + id));
        model.addAttribute("fundraiser", fundraiser);
        model.addAttribute("transactions", transactionService.getTransactionsByFundraiser(id));
        model.addAttribute("visits", visitService.getVisitsByFundraiser(id));
        return "fundraisers/audit";
    }
}
