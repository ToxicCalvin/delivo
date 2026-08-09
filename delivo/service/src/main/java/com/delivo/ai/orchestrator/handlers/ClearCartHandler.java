package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClearCartHandler implements IntentHandler {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CLEAR_CART && state != AiState.PENDING_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());
        shoppingCartService.cleanShoppingCart();
        return HandlerResult.text(IntentType.CLEAR_CART, "cart.cleared", null);
    }
}
