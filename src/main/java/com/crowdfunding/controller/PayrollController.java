package com.crowdfunding.controller;

import com.crowdfunding.service.PayrollService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles HTTP requests for viewing payroll records.
 * Payroll is auto-created by the system — no manual creation forms needed here.
 *
 * GRASP: Controller — GRASP Controller principle: delegates all payroll data retrieval
 *        to PayrollService; no logic lives in this class.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    /**
     * Display all payroll records — the complete earnings log.
     * GET /payroll
     */
    @GetMapping
    public String listPayroll(Model model) {
        model.addAttribute("payrollList", payrollService.getAllPayroll());
        return "payroll/list";
    }

    /**
     * View a single payroll record.
     * GET /payroll/{id}
     */
    @GetMapping("/{id}")
    public String viewPayroll(@PathVariable Long id, Model model) {
        model.addAttribute("payroll",
                payrollService.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Payroll record not found with ID: " + id)));
        return "payroll/detail";
    }
}
