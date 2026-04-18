package com.crowdfunding.controller;

import com.crowdfunding.service.DashboardFacade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles the main dashboard page at the root URL.
 *
 * GRASP: Controller — GRASP Controller principle: the UI delegates all requests
 *        to this controller, which collects statistics from the DashboardFacade
 *        and passes them to the Thymeleaf view. The controller itself has zero
 *        business or data-access logic.
 *
 * Design Pattern: MVC     — this is the C in MVC; populates the Model for the View.
 * Design Pattern: Facade  — DashboardFacade hides the complexity of querying multiple
 *                           repositories; the controller talks to one facade only.
 * Design Pattern: Singleton — Spring IoC manages this as a singleton bean.
 */
@Controller
public class DashboardController {

    // Design Pattern: Facade — single entry point for all dashboard data needs.
    // Instead of injecting 5 repositories here, the facade handles all of it.
    private final DashboardFacade dashboardFacade;

    public DashboardController(DashboardFacade dashboardFacade) {
        this.dashboardFacade = dashboardFacade;
    }

    /**
     * Render the main dashboard with platform-wide statistics.
     * GET /
     *
     * All data is fetched through DashboardFacade — this controller does not
     * know which repositories or services are involved behind the scenes.
     */
    @GetMapping("/")
    public String dashboard(Model model) {

        // Facade pattern — all data fetched through a single simplified interface
        model.addAttribute("totalAdmins",         dashboardFacade.getTotalAdmins());
        model.addAttribute("totalVisits",          dashboardFacade.getTotalVisits());
        model.addAttribute("totalDonors",          dashboardFacade.getTotalDonors());
        model.addAttribute("totalFundraisers",     dashboardFacade.getTotalFundraisers());
        model.addAttribute("activeFundraisers",    dashboardFacade.getActiveFundraisers());
        model.addAttribute("totalTransactions",    dashboardFacade.getTotalTransactions());
        model.addAttribute("totalRaised",          dashboardFacade.getTotalRaised());
        model.addAttribute("totalGoalAmount",      dashboardFacade.getTotalGoalAmount());
        model.addAttribute("totalPlatformFees",    dashboardFacade.getTotalPlatformFees());
        model.addAttribute("recentTransactions",   dashboardFacade.getRecentTransactions());
        model.addAttribute("topFundraisers",       dashboardFacade.getAllFundraisers());

        return "dashboard";
    }
}
