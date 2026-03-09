package com.mercato.Service;

import com.mercato.Entity.EcommUser;
import com.mercato.Entity.fulfillment.OrderLine;
import com.mercato.ExceptionHandler.ResourceNotFoundException;
import com.mercato.Mapper.FulfillmentOrderMapper;
import com.mercato.Payloads.Response.FulfillmentOrderResponseDTO;
import com.mercato.Repository.OrderLineRepository;
import com.mercato.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final OrderLineRepository orderLineRepository;
    private final AuthUtil authUtil;

    @Override
    @Transactional(readOnly = true)
    public List<FulfillmentOrderResponseDTO> getAllFulfillmentOrders() {
        EcommUser seller = authUtil.getLoggedInUser();

        List<OrderLine> lines = orderLineRepository
                .findAllBySellerEmail(seller.getEmail());

        return groupByFulfillmentId(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public FulfillmentOrderResponseDTO getFulfillmentOrder(String fulfillmentId) {
        EcommUser seller = authUtil.getLoggedInUser();

        List<OrderLine> lines = orderLineRepository
                .findAllByFulfillmentIdAndSellerEmail(fulfillmentId, seller.getEmail());

        if (lines.isEmpty()) {
            throw new ResourceNotFoundException("FulfillmentOrder", "fulfillmentId", fulfillmentId);
        }

        return FulfillmentOrderMapper.toDto(fulfillmentId, lines);
    }


    private List<FulfillmentOrderResponseDTO> groupByFulfillmentId(List<OrderLine> lines) {
        Map<String, List<OrderLine>> grouped = lines.stream()
                .collect(Collectors.groupingBy(OrderLine::getFulfillmentId));

        return grouped.entrySet().stream()
                .map(entry ->
                        FulfillmentOrderMapper.toDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}