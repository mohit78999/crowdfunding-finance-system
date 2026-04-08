package com.crowdfunding.service;

import com.crowdfunding.model.Administrator;
import com.crowdfunding.repository.AdministratorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Administrator CRUD and earnings management.
 *
 * GRASP: High Cohesion - this service ONLY handles administrator-related operations.
 *        It does not touch transactions, visits, or payroll logic.
 * GRASP: Low Coupling - depends on AdministratorRepository interface, not a concrete class.
 *
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Service
public class AdministratorService {

    // Constructor injection — preferred over @Autowired field injection
    // because it makes dependencies explicit and enables proper unit testing
    private final AdministratorRepository administratorRepository;

    public AdministratorService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    /** Retrieve every administrator registered on the platform. */
    public List<Administrator> getAllAdministrators() {
        return administratorRepository.findAll();
    }

    /** Look up a single administrator by their primary key. Returns empty if not found. */
    public Optional<Administrator> findById(Long id) {
        return administratorRepository.findById(id);
    }

    /** Persist a new administrator or update an existing one. */
    @Transactional
    public Administrator save(Administrator administrator) {
        // Ensure monetary field is never null before saving
        if (administrator.getTotalEarnings() == null) {
            administrator.setTotalEarnings(BigDecimal.ZERO);
        }
        return administratorRepository.save(administrator);
    }

    /** Remove an administrator by ID. */
    @Transactional
    public void deleteById(Long id) {
        administratorRepository.deleteById(id);
    }

    /**
     * Add to an administrator's running total earnings.
     * Called by TransactionService after every successful donation.
     *
     * @param adminId the administrator to credit
     * @param amount  the net amount (99% of donation) earned from this transaction
     */
    @Transactional
    public void updateEarnings(Long adminId, BigDecimal amount) {
        // Fetch the admin, add the new earnings, and save
        administratorRepository.findById(adminId).ifPresent(admin -> {
            BigDecimal current = admin.getTotalEarnings() != null ? admin.getTotalEarnings() : BigDecimal.ZERO;
            admin.setTotalEarnings(current.add(amount));
            administratorRepository.save(admin);
        });
    }

    /** Check if an email is already in use (for validation in the controller). */
    public boolean emailExists(String email) {
        return administratorRepository.existsByEmail(email);
    }
}
