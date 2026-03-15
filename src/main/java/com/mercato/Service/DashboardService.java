package com.mercato.Service;

import com.mercato.Payloads.Response.AdminDashboardStatsDTO;
import com.mercato.Payloads.Response.SellerDashboardStatsDTO;

public interface DashboardService {

    AdminDashboardStatsDTO getAdminDashboardStats();

    SellerDashboardStatsDTO getSellerDashboardStats();
}
