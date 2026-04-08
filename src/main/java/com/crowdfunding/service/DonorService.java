package com.crowdfunding.service;

import com.crowdfunding.model.Donor;
import com.crowdfunding.repository.DonorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Donor CRUD and donation-total tracking.
 *
 * GRASP: Low Coupling - depends on DonorRepository interface, not a concrete class
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Service
public class DonorService {

    // Constructor injection keeps dependencies explicit and testable
    private final DonorRepository donorRepository;

    public DonorService(DonorRepository donorRepository) {
        this.donorRepository = donorRepository;
    }

    /** Get all donors registered on the platform. */
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    /** Find a single donor by their ID. Returns empty Optional if not found. */
    public Optional<Donor> findById(Long id) {
        return donorRepository.findById(id);
    }

    /** Create or update a donor record. */
    @Transactional
    public Donor save(Donor donor) {
        if (donor.getTotalDonated() == null) {
            donor.setTotalDonated(BigDecimal.ZERO);
        }
        return donorRepository.save(donor);
    }

    /** Delete a donor by their ID. */
    @Transactional
    public void deleteById(Long id) {
        donorRepository.deleteById(id);
    }

    /**
     * Increment a donor's running total after a successful donation.
     * Called by TransactionService — the actual amount the donor paid (gross).
     *
     * @param donorId the donor who made the donation
     * @param amount  the gross donation amount (full amount, before fee)
     */
    @Transactional
    public void updateTotalDonated(Long donorId, BigDecimal amount) {
        donorRepository.findById(donorId).ifPresent(donor -> {
            BigDecimal current = donor.getTotalDonated() != null ? donor.getTotalDonated() : BigDecimal.ZERO;
            donor.setTotalDonated(current.add(amount));
            donorRepository.save(donor);
        });
    }

    /** Email uniqueness check for form validation. */
    public boolean emailExists(String email) {
        return donorRepository.existsByDemail(email);
    }
}
