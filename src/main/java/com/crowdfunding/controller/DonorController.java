package com.crowdfunding.controller;

import com.crowdfunding.model.Donor;
import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles all HTTP requests related to Donor management.
 *
 * GRASP: Controller — GRASP Controller principle: UI delegates donor-related requests
 *        to this controller, which calls DonorService and never contains business logic itself.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private final TransactionService transactionService;

    public DonorController(DonorService donorService, TransactionService transactionService) {
        this.donorService = donorService;
        this.transactionService = transactionService;
    }

    /**
     * List all registered donors.
     * GET /donors
     */
    @GetMapping
    public String listDonors(Model model) {
        model.addAttribute("donors", donorService.getAllDonors());
        return "donors/list";
    }

    /**
     * Show the form to register a new donor.
     * GET /donors/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("donor", new Donor());
        return "donors/add";
    }

    /**
     * Submit new donor registration.
     * POST /donors/add
     */
    @PostMapping("/add")
    public String addDonor(@Valid @ModelAttribute("donor") Donor donor,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "donors/add";
        }

        // Prevent duplicate donor accounts by email
        if (donorService.emailExists(donor.getDemail())) {
            result.rejectValue("demail", "email.duplicate", "A donor with this email already exists");
            return "donors/add";
        }

        donorService.save(donor);
        redirectAttributes.addFlashAttribute("successMessage", "Donor registered successfully!");
        return "redirect:/donors";
    }

    /**
     * Show pre-filled edit form for an existing donor.
     * GET /donors/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Donor donor = donorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + id));
        model.addAttribute("donor", donor);
        return "donors/edit";
    }

    /**
     * Submit updated donor details.
     * POST /donors/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String updateDonor(@PathVariable Long id,
                               @Valid @ModelAttribute("donor") Donor donor,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "donors/edit";
        }

        donor.setDonorId(id);
        donorService.save(donor);
        redirectAttributes.addFlashAttribute("successMessage", "Donor updated successfully!");
        return "redirect:/donors";
    }

    /**
     * Delete a donor by ID.
     * GET /donors/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteDonor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        donorService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Donor deleted successfully!");
        return "redirect:/donors";
    }

    /**
     * View a donor's profile and donation summary.
     * GET /donors/{id}
     */
    @GetMapping("/{id}")
    public String viewDonor(@PathVariable Long id, Model model) {
        Donor donor = donorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + id));
        model.addAttribute("donor", donor);
        // Load this donor's full donation history for the detail page
        model.addAttribute("transactions", transactionService.getTransactionsByDonor(id));
        return "donors/detail";
    }
}
