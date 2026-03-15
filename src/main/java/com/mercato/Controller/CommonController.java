package com.mercato.Controller;

import com.mercato.Payloads.Response.AdminDashboardStatsDTO;
import com.mercato.Payloads.Response.SellerDashboardStatsDTO;
import com.mercato.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/dashboard/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardStats());
    }

    @GetMapping("/seller/dashboard/stats")
    public ResponseEntity<SellerDashboardStatsDTO> getSellerDashboardStats() {
        return ResponseEntity.ok(dashboardService.getSellerDashboardStats());
    }
}
