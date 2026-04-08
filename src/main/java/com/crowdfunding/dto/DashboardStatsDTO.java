package com.crowdfunding.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object that bundles all the key metrics shown on the dashboard.
 *
 * Using a DTO keeps the controller clean — instead of adding a dozen individual
 * model attributes, we add one object that Thymeleaf can read with dot notation.
 *
 * Design Pattern: MVC — this DTO is the "Model" that travels from Controller to View
 */
public class DashboardStatsDTO {

    // How many administrators are registered on the platform
    private long totalAdministrators;

    // How many donors have signed up
    private long totalDonors;

    // Total number of fundraisers ever created
    private long totalFundraisers;

    // How many fundraisers are currently in "Active" status
    private long activeFundraisers;

    // Total number of donation transactions processed
    private long totalTransactions;

    // Sum of all net_amounts credited to fundraisers
    private BigDecimal totalRaised;

    // Sum of all platform_fees collected (1% of each donation)
    private BigDecimal totalPlatformFees;

    // No-arg constructor required for frameworks
    public DashboardStatsDTO() {}

    // All-args constructor for quick instantiation in the controller
    public DashboardStatsDTO(long totalAdministrators, long totalDonors, long totalFundraisers,
                              long activeFundraisers, long totalTransactions,
                              BigDecimal totalRaised, BigDecimal totalPlatformFees) {
        this.totalAdministrators = totalAdministrators;
        this.totalDonors = totalDonors;
        this.totalFundraisers = totalFundraisers;
        this.activeFundraisers = activeFundraisers;
        this.totalTransactions = totalTransactions;
        this.totalRaised = totalRaised != null ? totalRaised : BigDecimal.ZERO;
        this.totalPlatformFees = totalPlatformFees != null ? totalPlatformFees : BigDecimal.ZERO;
    }

    // --- Getters and Setters ---

    public long getTotalAdministrators() {
        return totalAdministrators;
    }

    public void setTotalAdministrators(long totalAdministrators) {
        this.totalAdministrators = totalAdministrators;
    }

    public long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public long getTotalFundraisers() {
        return totalFundraisers;
    }

    public void setTotalFundraisers(long totalFundraisers) {
        this.totalFundraisers = totalFundraisers;
    }

    public long getActiveFundraisers() {
        return activeFundraisers;
    }

    public void setActiveFundraisers(long activeFundraisers) {
        this.activeFundraisers = activeFundraisers;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalRaised() {
        return totalRaised;
    }

    public void setTotalRaised(BigDecimal totalRaised) {
        this.totalRaised = totalRaised;
    }

    public BigDecimal getTotalPlatformFees() {
        return totalPlatformFees;
    }

    public void setTotalPlatformFees(BigDecimal totalPlatformFees) {
        this.totalPlatformFees = totalPlatformFees;
    }
}
