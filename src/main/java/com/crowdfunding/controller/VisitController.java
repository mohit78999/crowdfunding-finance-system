package com.crowdfunding.controller;

import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.FundraiserService;
import com.crowdfunding.service.VisitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles HTTP requests for visit tracking and engagement analytics.
 *
 * GRASP: Controller — GRASP Controller principle: this controller routes visit recording
 *        requests to VisitService (which is highly cohesive). The controller itself
 *        does no interest-level calculation — it just passes parameters and redirects.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/visits")
public class VisitController {

    private final VisitService visitService;
    private final DonorService donorService;
    private final FundraiserService fundraiserService;

    public VisitController(VisitService visitService,
                             DonorService donorService,
                             FundraiserService fundraiserService) {
        this.visitService = visitService;
        this.donorService = donorService;
        this.fundraiserService = fundraiserService;
    }

    /**
     * Display all recorded visits.
     * GET /visits
     */
    @GetMapping
    public String listVisits(Model model) {
        model.addAttribute("visits", visitService.getAllVisits());
        return "visits/list";
    }

    /**
     * Show the manual visit recording form.
     * GET /visits/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("donors", donorService.getAllDonors());
        model.addAttribute("fundraisers", fundraiserService.getAllFundraisers());
        model.addAttribute("visitTypes", new String[]{"View", "Transaction"});
        return "visits/form";
    }

    /**
     * Record a new visit and auto-calculate the interest level.
     * POST /visits/add
     *
     * VisitService handles the interest level calculation — this controller just passes inputs.
     */
    @PostMapping("/add")
    public String recordVisit(@RequestParam Long donorId,
                               @RequestParam Long fundraiserNo,
                               @RequestParam(required = false, defaultValue = "0") Integer duration,
                               @RequestParam(required = false, defaultValue = "View") String visitType,
                               RedirectAttributes redirectAttributes) {
        try {
            visitService.recordVisit(donorId, fundraiserNo, duration, visitType);
            redirectAttributes.addFlashAttribute("successMessage", "Visit recorded successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/visits/add";
        }
        return "redirect:/visits";
    }

    /** Show the edit form for an existing visit. GET /visits/edit/{id} */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("visit",
                visitService.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Visit not found with ID: " + id)));
        return "visits/edit";
    }

    /**
     * Auto-record a "View" visit from the fundraiser detail page.
     * POST /visits/record-view (called via JavaScript fetch)
     */
    @PostMapping("/record-view")
    @ResponseBody
    public String recordViewVisit(@RequestParam Long fundraiserNo,
                                   @RequestParam(required = false, defaultValue = "0") Integer duration) {
        try {
            visitService.recordAnonymousVisit(fundraiserNo, duration);
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }

    /** Submit updated visit data. POST /visits/edit/{id} */
    @PostMapping("/edit/{id}")
    public String updateVisit(@PathVariable Long id,
                               @RequestParam Integer duration,
                               @RequestParam String interestLevel,
                               RedirectAttributes redirectAttributes) {
        try {
            visitService.updateVisit(id, duration, interestLevel);
            redirectAttributes.addFlashAttribute("successMessage", "Visit updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/visits";
    }

}
