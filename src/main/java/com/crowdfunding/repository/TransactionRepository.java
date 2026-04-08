package com.crowdfunding.repository;

import com.crowdfunding.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data access layer for Transaction entities.
 *
 * Design Pattern: DAO - abstracts data access layer
 * Design Pattern: Singleton - Spring IoC manages this as a singleton bean
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All donations made by a specific donor (donor history page)
    List<Transaction> findByDonorDonorId(Long donorId);

    // All donations received by a specific fundraiser (fundraiser detail page)
    List<Transaction> findByFundraiserFundraiserNo(Long fundraiserNo);

    // Sum of gross amounts donated to a specific fundraiser
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fundraiser.fundraiserNo = :fundraiserNo")
    BigDecimal sumAmountByFundraiserFundraiserNo(@Param("fundraiserNo") Long fundraiserNo);

    // Total platform fees collected across all transactions — revenue metric for dashboard
    @Query("SELECT COALESCE(SUM(t.platformFee), 0) FROM Transaction t")
    BigDecimal sumTotalPlatformFees();

    // Total gross donations across all transactions — overall platform activity metric
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    BigDecimal sumTotalAmount();

    // Recent transactions ordered newest first — for dashboard activity feed
    List<Transaction> findTop10ByOrderByTransactionDateDesc();
}
