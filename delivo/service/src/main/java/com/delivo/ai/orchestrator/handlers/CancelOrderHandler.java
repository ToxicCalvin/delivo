package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.Orders;
import com.delivo.mapper.OrderMapper;
import com.delivo.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CancelOrderHandler implements IntentHandler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CANCEL_ORDER;
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
                    .intentType(IntentType.CANCEL_ORDER)
                    .textKey("order.notFound")
                    .halts(true)
                    .build();
        }

        try {
            orderService.userCancelById(order.getId());
        } catch (Exception e) {
            log.error("cancelOrder failed", e);
            Map<String, Object> failParams = new HashMap<>();
            failParams.put("reason", e.getMessage() == null ? "internal error" : e.getMessage());
            return HandlerResult.builder()
                    .intentType(IntentType.CANCEL_ORDER)
                    .textKey("order.submit.failed")
                    .textParams(failParams)
                    .halts(true)
                    .build();
        }

        Map<String, Object> textParams = new HashMap<>();
        textParams.put("orderNumber", order.getNumber() == null ? "" : order.getNumber());
        return HandlerResult.builder()
                .intentType(IntentType.CANCEL_ORDER)
                .textKey("order.cancelled")
                .textParams(textParams)
                .newState(AiState.RECOMMEND_DISHES)
                .build();
    }
}
