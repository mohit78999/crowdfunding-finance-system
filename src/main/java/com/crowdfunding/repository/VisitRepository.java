package com.crowdfunding.repository;

import com.crowdfunding.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for Visit entities.
 * The visit count query here is critical — VisitService uses it to determine interest level.
 *
 * Design Pattern: DAO - abstracts data access layer
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    // All visits by a specific donor — their browsing history
    List<Visit> findByDonorDonorId(Long donorId);

    // All visits to a specific fundraiser — who is interested in this campaign
    List<Visit> findByFundraiserFundraiserNo(Long fundraiserNo);

    // Key query: how many times has this donor visited THIS fundraiser?
    // VisitService uses this count to determine the interest level
    long countByDonorDonorIdAndFundraiserFundraiserNo(Long donorId, Long fundraiserNo);

    // All visits of a specific type (View vs Transaction) for a fundraiser
    List<Visit> findByFundraiserFundraiserNoAndVisitType(Long fundraiserNo, String visitType);
}
