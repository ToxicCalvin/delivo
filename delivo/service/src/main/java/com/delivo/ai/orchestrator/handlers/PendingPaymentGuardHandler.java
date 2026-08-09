package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import org.springframework.stereotype.Component;


@Component
public class PendingPaymentGuardHandler implements IntentHandler {

    @Override
    public boolean supports(AiState state, IntentType type) {
        if (state != AiState.PENDING_PAYMENT) return false;
        switch (type) {
            case ADD_TO_CART:
            case REMOVE_FROM_CART:
            case CLEAR_CART:
            case CHECKOUT:
            case PROVIDE_ADDRESS:
            case CREATE_ADDRESS:
            case RECOMMEND_DISHES:
            case REORDER:
                return true;
            default:
                return false;
        }
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        return HandlerResult.builder()
                .intentType(ctx.getIntent().getType())
                .textKey("payment.required.first")
                .halts(true)
                .build();
    }
}
