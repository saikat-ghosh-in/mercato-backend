package com.mercato.Controller;

import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;
import com.mercato.Payloads.Response.SellerDashboardStatsDTO;
import com.mercato.Service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping("/seller/fulfillment-orders")
    public ResponseEntity<List<FulfillmentOrderResponseDTO>> getAllFulfillmentOrders() {
        return ResponseEntity.ok(sellerService.getAllFulfillmentOrders());
    }

    @GetMapping("/seller/fulfillment-orders/{fulfillmentId}")
    public ResponseEntity<FulfillmentOrderResponseDTO> getFulfillmentOrder(@PathVariable String fulfillmentId) {
        return ResponseEntity.ok(sellerService.getFulfillmentOrder(fulfillmentId));
    }

    @GetMapping("/seller/dashboard/stats")
    public ResponseEntity<SellerDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(sellerService.getDashboardStats());
    }
}
