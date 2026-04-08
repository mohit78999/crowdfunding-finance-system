package com.crowdfunding.service;

import com.crowdfunding.model.Payroll;
import com.crowdfunding.repository.PayrollRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for querying Payroll records.
 * Payroll entries are created automatically by TransactionService —
 * this service is read-only in normal operation.
 *
 * GRASP: High Cohesion - only deals with payroll queries, nothing else
 * GRASP: Low Coupling - depends on PayrollRepository interface, not a concrete class
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;

    public PayrollService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    /** Get all payroll records across the platform. */
    public List<Payroll> getAllPayroll() {
        return payrollRepository.findAll();
    }

    /** Find a specific payroll entry by its ID. */
    public Optional<Payroll> findById(Long id) {
        return payrollRepository.findById(id);
    }

    /**
     * Get payroll history for a specific administrator.
     * Useful for an admin to see their earnings breakdown per transaction.
     */
    public List<Payroll> getPayrollByAdmin(Long adminId) {
        return payrollRepository.findByAdministratorAdminId(adminId);
    }

    /** Get all payroll entries related to a specific fundraiser. */
    public List<Payroll> getPayrollByFundraiser(Long fundraiserNo) {
        return payrollRepository.findByFundraiserFundraiserNo(fundraiserNo);
    }
}
