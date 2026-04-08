package com.crowdfunding.repository;

import com.crowdfunding.model.Fundraiser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data access layer for Fundraiser entities.
 *
 * GRASP: Low Coupling - FundraiserService depends on this interface, not a concrete implementation.
 * Spring injects the generated proxy at runtime, meaning we can swap persistence strategies
 * without changing any service code.
 *
 * Design Pattern: DAO - abstracts data access layer
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface FundraiserRepository extends JpaRepository<Fundraiser, Long> {

    // Filter fundraisers by their lifecycle status (Planned, Active, Goal Reached, Closed)
    List<Fundraiser> findByStatus(String status);

    // Get all fundraisers managed by a specific administrator
    List<Fundraiser> findByAdministratorAdminId(Long adminId);

    // Total amount raised across ALL fundraisers — key dashboard metric
    @Query("SELECT COALESCE(SUM(f.raisedAmount), 0) FROM Fundraiser f")
    BigDecimal sumTotalRaised();

    // Total goal amount across ALL fundraisers
    @Query("SELECT COALESCE(SUM(f.goalAmount), 0) FROM Fundraiser f")
    BigDecimal sumTotalGoal();

    // Count how many fundraisers are currently accepting donations
    long countByStatus(String status);
}
