package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.Orders;
import com.delivo.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
public class ConfirmPaymentHandler implements IntentHandler {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CONFIRM_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        String orderNumber = ctx.getIntent().param("orderNumber", "");
        Orders order;
        if (orderNumber != null && !orderNumber.isBlank()) {
            order = orderMapper.getByNumberAndUserId(orderNumber, ctx.getUserId());
        } else {
            order = orderMapper.getLatestOrderByUserId(ctx.getUserId());
        }

        if (order == null) {
            return HandlerResult.builder()
                    .intentType(IntentType.CONFIRM_PAYMENT)
                    .textKey("order.notFound")
                    .halts(true)
                    .build();
        }

        Map<String, Object> textParams = new HashMap<>();
        textParams.put("orderNumber", order.getNumber() == null ? "" : order.getNumber());

        if (order.getPayStatus() != null && Orders.PAID.equals(order.getPayStatus())) {
            return HandlerResult.builder()
                    .intentType(IntentType.CONFIRM_PAYMENT)
                    .textKey("payment.confirmed")
                    .textParams(textParams)
                    .newState(AiState.ORDER_COMPLETED)
                    .build();
        }

        return HandlerResult.builder()
                .intentType(IntentType.CONFIRM_PAYMENT)
                .textKey("payment.notReceived")
                .textParams(textParams)
                .halts(true)
                .build();
    }
}
