package com.crowdfunding.service;

import com.crowdfunding.model.Fundraiser;
import com.crowdfunding.repository.FundraiserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Fundraiser CRUD and progress tracking.
 *
 * GRASP: Low Coupling - FundraiserService depends on FundraiserRepository (an interface),
 *        not on any concrete class. Spring injects the JPA implementation at runtime.
 *        This means we can swap the persistence layer without touching this service.
 *
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Service
public class FundraiserService {

    // GRASP: Low Coupling — we reference the interface, never the concrete Hibernate implementation
    private final FundraiserRepository fundraiserRepository;

    public FundraiserService(FundraiserRepository fundraiserRepository) {
        this.fundraiserRepository = fundraiserRepository;
    }

    /** Get all fundraisers regardless of status. */
    public List<Fundraiser> getAllFundraisers() {
        return fundraiserRepository.findAll();
    }

    /** Get only fundraisers currently accepting donations ("Active" status). */
    public List<Fundraiser> getActiveFundraisers() {
        return fundraiserRepository.findByStatus("Active");
    }

    /** Get fundraisers by a specific status string. */
    public List<Fundraiser> getFundraisersByStatus(String status) {
        return fundraiserRepository.findByStatus(status);
    }

    /** Find a single fundraiser by its primary key. */
    public Optional<Fundraiser> findById(Long id) {
        return fundraiserRepository.findById(id);
    }

    /** Persist a new fundraiser or update an existing one. */
    @Transactional
    public Fundraiser save(Fundraiser fundraiser) {
        if (fundraiser.getRaisedAmount() == null) {
            fundraiser.setRaisedAmount(BigDecimal.ZERO);
        }
        // Always recalculate remaining = goal - raised (fixes default-0 issue)
        if (fundraiser.getGoalAmount() != null) {
            BigDecimal raised = fundraiser.getRaisedAmount() != null ? fundraiser.getRaisedAmount() : BigDecimal.ZERO;
            fundraiser.setRemainingAmount(fundraiser.getGoalAmount().subtract(raised).max(BigDecimal.ZERO));
        }
        return fundraiserRepository.save(fundraiser);
    }

    /** Delete a fundraiser by ID. */
    @Transactional
    public void deleteById(Long id) {
        fundraiserRepository.deleteById(id);
    }

    /**
     * Update the fundraiser's financial progress after a donation.
     * Called by TransactionService (the Information Expert) after each successful transaction.
     *
     * Also checks if the goal has been reached and updates status accordingly.
     *
     * @param fundraiserNo the fundraiser that received the donation
     * @param netAmount    the 99% net amount credited to the fundraiser
     */
    @Transactional
    public void updateProgress(Long fundraiserNo, BigDecimal netAmount) {
        fundraiserRepository.findById(fundraiserNo).ifPresent(fundraiser -> {
            // Add the net donation to raised total
            BigDecimal newRaised = fundraiser.getRaisedAmount().add(netAmount);
            fundraiser.setRaisedAmount(newRaised);

            // Recalculate how much is still needed
            BigDecimal remaining = fundraiser.getGoalAmount().subtract(newRaised);
            // Remaining cannot go below zero (in case of over-funding)
            fundraiser.setRemainingAmount(remaining.max(BigDecimal.ZERO));

            // Automatically close the fundraiser when the goal is met
            if (newRaised.compareTo(fundraiser.getGoalAmount()) >= 0) {
                fundraiser.setStatus("Goal Reached");
            }

            fundraiserRepository.save(fundraiser);
        });
    }

    /** Get all fundraisers managed by a specific administrator. */
    public List<Fundraiser> getFundraisersByAdmin(Long adminId) {
        return fundraiserRepository.findByAdministratorAdminId(adminId);
    }
}
