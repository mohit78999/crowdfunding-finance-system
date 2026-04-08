package com.crowdfunding.controller;

import com.crowdfunding.model.Transaction;
import com.crowdfunding.service.DonorService;
import com.crowdfunding.service.FundraiserService;
import com.crowdfunding.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * Handles all HTTP requests for the donation/transaction flow.
 *
 * GRASP: Controller — GRASP Controller principle: this is the UI-facing entry point
 *        for all donation actions. It validates inputs, calls TransactionService
 *        (the Information Expert), and redirects the user appropriately.
 *        The controller itself has zero financial calculation logic.
 *
 * Design Pattern: MVC   — the Controller in Spring MVC
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final DonorService donorService;
    private final FundraiserService fundraiserService;

    public TransactionController(TransactionService transactionService,
                                   DonorService donorService,
                                   FundraiserService fundraiserService) {
        this.transactionService = transactionService;
        this.donorService = donorService;
        this.fundraiserService = fundraiserService;
    }

    /**
     * Display all transactions.
     * GET /transactions
     */
    @GetMapping
    public String listTransactions(Model model) {
        model.addAttribute("transactions", transactionService.getAllTransactions());
        return "transactions/list";
    }

    /**
     * Show the donation form.
     * GET /transactions/add
     */
    @GetMapping("/add")
    public String showDonationForm(Model model) {
        // Form needs donor and fundraiser dropdowns
        model.addAttribute("donors", donorService.getAllDonors());
        // Only show active fundraisers that are accepting donations
        model.addAttribute("fundraisers", fundraiserService.getActiveFundraisers());
        model.addAttribute("paymentModes", new String[]{"Credit Card", "Debit Card", "UPI", "Bank Transfer", "PayPal", "Cash"});
        return "transactions/form";
    }

    /**
     * Process the donation form — this triggers the full donation flow via TransactionService.
     * POST /transactions/add
     *
     * All the business logic (fee calculation, payroll creation, balance updates)
     * is handled by TransactionService.processDonation() — the GRASP Information Expert.
     */
    @PostMapping("/add")
    public String processDonation(@RequestParam Long donorId,
                                   @RequestParam Long fundraiserNo,
                                   @RequestParam BigDecimal amount,
                                   @RequestParam String paymentMode,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Delegate entirely to the Information Expert — controller does NOT calculate fees
            Transaction transaction = transactionService.processDonation(donorId, fundraiserNo, amount, paymentMode);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Donation of $" + amount + " processed successfully! Transaction ID: " + transaction.getTransactionId());
        } catch (IllegalArgumentException e) {
            // Validation errors from the service layer
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/transactions/add";
        } catch (Exception e) {
            // Unexpected errors — log and show user-friendly message
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction failed. Please try again.");
            return "redirect:/transactions/add";
        }

        return "redirect:/transactions";
    }

    /**
     * View details of a single transaction (amounts, fee breakdown, payroll link).
     * GET /transactions/{id}
     */
    @GetMapping("/{id}")
    public String viewTransaction(@PathVariable Long id, Model model) {
        Transaction transaction = transactionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found with ID: " + id));
        model.addAttribute("transaction", transaction);
        return "transactions/detail";
    }
}
