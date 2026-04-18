package com.crowdfunding.service;

import com.crowdfunding.model.Donor;
import com.crowdfunding.model.Fundraiser;
import com.crowdfunding.model.Visit;
import com.crowdfunding.repository.DonorRepository;
import com.crowdfunding.repository.FundraiserRepository;
import com.crowdfunding.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

/**
 * Service responsible exclusively for visit tracking and donor engagement analysis.
 *
 * GRASP: High Cohesion — this service does ONE thing: manage visits.
 *        It records visits, computes interest levels, and queries visit data.
 *        It does NOT touch transactions, payroll, or admin logic.
 *
 * GRASP: Low Coupling — depends on VisitRepository, DonorRepository, FundraiserRepository
 *        interfaces, not concrete implementations. Spring injects the proxy at runtime.
 *
 * Design Pattern: Singleton — Spring IoC manages this as a singleton bean (@Service).
 * Design Pattern: DAO       — VisitRepository is the DAO for Visit persistence.
 */
@Service
public class VisitService {

    // DAO Pattern — repository interfaces abstract all database access
    private final VisitRepository visitRepository;
    private final DonorRepository donorRepository;
    private final FundraiserRepository fundraiserRepository;

    public VisitService(VisitRepository visitRepository,
                        DonorRepository donorRepository,
                        FundraiserRepository fundraiserRepository) {
        this.visitRepository = visitRepository;
        this.donorRepository = donorRepository;
        this.fundraiserRepository = fundraiserRepository;
    }

    /**
     * Record a new visit by a donor to a fundraiser page.
     * Interest level is automatically computed based on cumulative visit count.
     *
     * @param donorId       the visiting donor
     * @param fundraiserNo  the fundraiser being viewed
     * @param duration      time spent on the page in minutes
     * @param visitType     "View" for page view, "Transaction" if they donated
     * @return the saved Visit record
     */
    @Transactional
    public Visit recordVisit(Long donorId, Long fundraiserNo, Integer duration, String visitType) {
        // DAO Pattern — fetch entities through repository (DAO) layer
        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + donorId));

        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserNo)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found with ID: " + fundraiserNo));

        // Count how many times this donor has visited this fundraiser BEFORE this visit
        long previousVisitCount = visitRepository.countByDonorDonorIdAndFundraiserFundraiserNo(donorId, fundraiserNo);

        // Determine interest level based on total visit count (including this one)
        String interestLevel = determineInterestLevel(previousVisitCount + 1);

        Visit visit = new Visit();
        visit.setDonor(donor);
        visit.setFundraiser(fundraiser);
        visit.setDuration(duration != null ? duration : 0);
        visit.setVisitType(visitType != null ? visitType : "View");
        visit.setInterestLevel(interestLevel);
        // visitDate is auto-set by @PrePersist in Visit entity

        // DAO Pattern — persist through repository
        return visitRepository.save(visit);
    }

    /**
     * Determine interest level based on how many times the donor has visited.
     * Rules: 1-2 visits = Low, 3-4 = Medium, 5-9 = High, 10+ = Very High
     *
     * @param visitCount total visit count INCLUDING the current visit
     * @return interest level string
     */
    public String determineInterestLevel(long visitCount) {
        if (visitCount >= 10) {
            return "Very High"; // 10+ visits: maximally engaged
        } else if (visitCount >= 5) {
            return "High";      // 5-9 visits: highly interested
        } else if (visitCount >= 3) {
            return "Medium";    // 3-4 visits: returning, genuine interest
        } else {
            return "Low";       // 1-2 visits: just discovered this fundraiser
        }
    }

    /**
     * Record an anonymous page view — auto-creates a "Guest" donor if none exists.
     * Called when anyone opens a fundraiser detail page.
     */
    @Transactional
    public Visit recordAnonymousVisit(Long fundraiserNo, Integer duration) {
        Fundraiser fundraiser = fundraiserRepository.findById(fundraiserNo)
                .orElseThrow(() -> new IllegalArgumentException("Fundraiser not found"));

        // Find or create the Guest donor
        Donor guest = donorRepository.findByDemail("guest@system.local").orElseGet(() -> {
            Donor d = new Donor();
            d.setDname("Guest Visitor");
            d.setDemail("guest@system.local");
            d.setTotalDonated(java.math.BigDecimal.ZERO);
            return donorRepository.save(d);
        });

        long visitCount = visitRepository.countByDonorDonorIdAndFundraiserFundraiserNo(guest.getDonorId(), fundraiserNo);
        String interestLevel = determineInterestLevel(visitCount + 1);

        Visit visit = new Visit();
        visit.setDonor(guest);
        visit.setFundraiser(fundraiser);
        visit.setDuration(duration != null ? duration : 0);
        visit.setVisitType("View");
        visit.setInterestLevel(interestLevel);
        return visitRepository.save(visit);
    }

    /** Get all visit records across the platform. */
    public List<Visit> getAllVisits() {
        // DAO Pattern — delegate to repository
        return visitRepository.findAll();
    }

    /** Find a specific visit by its ID. */
    public Optional<Visit> findById(Long id) {
        return visitRepository.findById(id);
    }

    /** All visits made by a specific donor. */
    public List<Visit> getVisitsByDonor(Long donorId) {
        return visitRepository.findByDonorDonorId(donorId);
    }

    /** All visits to a specific fundraiser. */
    public List<Visit> getVisitsByFundraiser(Long fundraiserNo) {
        return visitRepository.findByFundraiserFundraiserNo(fundraiserNo);
    }

    /** How many times has this donor visited this fundraiser? */
    public long getVisitCount(Long donorId, Long fundraiserNo) {
        return visitRepository.countByDonorDonorIdAndFundraiserFundraiserNo(donorId, fundraiserNo);
    }

    /** Update duration and interest level of an existing visit. */
    @Transactional
    public Visit updateVisit(Long visitId, Integer duration, String interestLevel) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Visit not found with ID: " + visitId));
        visit.setDuration(duration);
        visit.setInterestLevel(interestLevel);
        return visitRepository.save(visit);
    }
}
