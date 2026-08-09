package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.Orders;
import com.delivo.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class OrderStatusHandler implements IntentHandler {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.ORDER_STATUS;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());
        Orders latest = orderMapper.getLatestOrderByUserId(ctx.getUserId());
        if (latest == null) {
            return HandlerResult.builder()
                    .intentType(IntentType.ORDER_STATUS)
                    .textKey("order.notFound")
                    .build();
        }

        String statusText = mapStatus(latest.getStatus());
        String contextLine = "Latest order status: " + statusText
                + (latest.getEstimatedDeliveryTime() != null
                        ? ". Estimated delivery: " + latest.getEstimatedDeliveryTime()
                        : "");

        return HandlerResult.builder()
                .intentType(IntentType.ORDER_STATUS)
                .needsNlPolish(true)
                .polishGoal("Tell the user the current status of their latest order. Be warm and empathetic; one short sentence.")
                .polishContext(contextLine)
                .build();
    }

    static String mapStatus(Integer status) {
        if (status == null) return "Unknown";
        switch (status) {
            case 1: return "Pending Payment";
            case 2: return "Awaiting Acceptance";
            case 3: return "Accepted & Cooking";
            case 4: return "Delivering";
            case 5: return "Completed";
            case 6: return "Cancelled";
            case 7: return "Refunded";
            default: return "Unknown (" + status + ")";
        }
    }
}
