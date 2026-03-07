package com.mercato.Service;

import com.mercato.Entity.Product;
import com.mercato.Entity.fulfillment.Order;
import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.Entity.fulfillment.OrderLineAction;
import com.mercato.Entity.fulfillment.OrderReservation;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Repository.OrderReservationRepository;
import com.mercato.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderReservationServiceImpl implements OrderReservationService {

    private final OrderReservationRepository orderReservationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void reserveForOrder(Order order) {
        order.getOrderLines().forEach(orderLine -> {
            Product product = productRepository.findByProductId(orderLine.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "productId", orderLine.getProductId()
                    ));

            OrderReservation reservation = OrderReservation.builder()
                    .order(order)
                    .orderLine(orderLine)
                    .product(product)
                    .reservedQty(orderLine.getOrderedQty())
                    .build();

            orderReservationRepository.save(reservation);
        });
    }

    @Override
    @Transactional
    public void settleQty(OrderLine orderLine, int qty, OrderLineAction action) {
        OrderReservation reservation = orderReservationRepository
                .findByOrderLine_Id(orderLine.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OrderReservation", "orderLineId", String.valueOf(orderLine.getId())
                ));

        Product product = productRepository.findByProductIdForUpdate(
                        reservation.getProduct().getProductId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", reservation.getProduct().getProductId()
                ));

        switch (action) {
            case SHIP -> {
                product.adjustInventory(-qty);
                product.decreaseReservedQty(qty);
            }
            case CANCEL -> product.decreaseReservedQty(qty);
            default -> throw new IllegalStateException(
                    "Cannot settle qty for action: " + action
            );
        }

        productRepository.save(product);

        if (orderLine.isTerminal()) {
            orderReservationRepository.delete(reservation);
        } else {
            reservation.setReservedQty(reservation.getReservedQty() - qty);
            orderReservationRepository.save(reservation);
        }
    }
}
