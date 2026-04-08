package com.crowdfunding.repository;

import com.crowdfunding.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Data access layer for Donor entities.
 *
 * Design Pattern: DAO - abstracts data access layer
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    // Used to prevent duplicate donor registrations
    Optional<Donor> findByDemail(String demail);

    // Existence check without fetching the full entity
    boolean existsByDemail(String demail);

    // Aggregate total donations across all donors — shown on the dashboard
    @Query("SELECT COALESCE(SUM(d.totalDonated), 0) FROM Donor d")
    BigDecimal sumTotalDonated();
}
