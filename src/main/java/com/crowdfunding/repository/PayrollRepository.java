package com.crowdfunding.repository;

import com.crowdfunding.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data access layer for Payroll entities.
 * Each payroll record corresponds to one processed transaction.
 *
 * Design Pattern: DAO - abstracts data access layer
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    // All payroll entries for a specific admin — their earnings history
    List<Payroll> findByAdministratorAdminId(Long adminId);

    // All payroll entries linked to a specific fundraiser
    List<Payroll> findByFundraiserFundraiserNo(Long fundraiserNo);

    // Total earnings for a specific admin — used when recalculating admin.totalEarnings
    @Query("SELECT COALESCE(SUM(p.adminEarnings), 0) FROM Payroll p WHERE p.administrator.adminId = :adminId")
    BigDecimal sumEarningsByAdminId(@Param("adminId") Long adminId);
}
