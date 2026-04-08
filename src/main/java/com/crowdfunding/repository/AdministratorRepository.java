package com.crowdfunding.repository;

import com.crowdfunding.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Data access layer for Administrator entities.
 * Spring Data JPA auto-generates the SQL implementation at runtime.
 *
 * Design Pattern: DAO - abstracts data access layer,
 * so services never write raw SQL or deal with EntityManager directly
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {

    // Find an admin by email — useful for duplicate-check before saving
    Optional<Administrator> findByEmail(String email);

    // Check existence by email without loading the full entity
    boolean existsByEmail(String email);

    // Calculate total earnings across ALL administrators — used for dashboard stats
    @Query("SELECT COALESCE(SUM(a.totalEarnings), 0) FROM Administrator a")
    BigDecimal sumTotalEarnings();
}
