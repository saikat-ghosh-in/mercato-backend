package com.mercato.Service;


import com.mercato.Entity.fulfillment.TransitionTrigger;
import com.mercato.Payloads.Request.OrderLineUpdateRequestDTO;
import com.mercato.Payloads.Response.OrderLineResponseDTO;
import jakarta.validation.Valid;

public interface OrderLineUpdateService {
    OrderLineResponseDTO updateOrderLine(@Valid OrderLineUpdateRequestDTO request, TransitionTrigger trigger);
}
