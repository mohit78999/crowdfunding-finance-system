package com.crowdfunding.controller;

import com.crowdfunding.model.Administrator;
import com.crowdfunding.service.AdministratorService;
import com.crowdfunding.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles all HTTP requests related to Administrator management.
 *
 * GRASP: Controller — GRASP Controller principle: this class is the single point of
 *        contact between the UI (Thymeleaf forms) and the AdministratorService.
 *        It never contains business logic — it just delegates and routes.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/administrators")
public class AdministratorController {

    private final AdministratorService administratorService;
    private final PayrollService payrollService;

    public AdministratorController(AdministratorService administratorService, PayrollService payrollService) {
        this.administratorService = administratorService;
        this.payrollService = payrollService;
    }

    /**
     * List all administrators.
     * GET /administrators
     */
    @GetMapping
    public String listAdministrators(Model model) {
        model.addAttribute("administrators", administratorService.getAllAdministrators());
        return "administrators/list";
    }

    /**
     * Show the blank form to add a new administrator.
     * GET /administrators/add
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        // Provide an empty Administrator object so Thymeleaf can bind form fields
        model.addAttribute("administrator", new Administrator());
        return "administrators/add";
    }

    /**
     * Process the new administrator form submission.
     * POST /administrators/add
     */
    @PostMapping("/add")
    public String addAdministrator(@Valid @ModelAttribute("administrator") Administrator administrator,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        // Return to form if validation errors exist
        if (result.hasErrors()) {
            return "administrators/add";
        }

        // Check for duplicate email before saving
        if (administratorService.emailExists(administrator.getEmail())) {
            result.rejectValue("email", "email.duplicate", "An administrator with this email already exists");
            return "administrators/add";
        }

        administratorService.save(administrator);
        redirectAttributes.addFlashAttribute("successMessage", "Administrator added successfully!");
        return "redirect:/administrators";
    }

    /**
     * Show the edit form pre-filled with an existing administrator's data.
     * GET /administrators/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Administrator admin = administratorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found with ID: " + id));
        model.addAttribute("administrator", admin);
        return "administrators/edit";
    }

    /**
     * Process the edit form submission.
     * POST /administrators/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String updateAdministrator(@PathVariable Long id,
                                       @Valid @ModelAttribute("administrator") Administrator administrator,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "administrators/edit";
        }

        // Make sure the ID is set (Thymeleaf hidden field may not bind it)
        administrator.setAdminId(id);
        administratorService.save(administrator);
        redirectAttributes.addFlashAttribute("successMessage", "Administrator updated successfully!");
        return "redirect:/administrators";
    }

    /**
     * Delete an administrator by ID.
     * GET /administrators/delete/{id}  (using GET for simplicity; POST would be more RESTful)
     */
    @GetMapping("/delete/{id}")
    public String deleteAdministrator(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        administratorService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Administrator deleted successfully!");
        return "redirect:/administrators";
    }

    /**
     * View details of a single administrator.
     * GET /administrators/{id}
     */
    @GetMapping("/{id}")
    public String viewAdministrator(@PathVariable Long id, Model model) {
        Administrator admin = administratorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found with ID: " + id));
        model.addAttribute("administrator", admin);
        model.addAttribute("payrollList", payrollService.getPayrollByAdmin(id));
        return "administrators/detail";
    }
}
