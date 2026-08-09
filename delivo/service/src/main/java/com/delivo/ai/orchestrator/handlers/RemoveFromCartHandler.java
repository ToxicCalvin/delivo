package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.ai.tool.DishMatcher;
import com.delivo.context.BaseContext;
import com.delivo.dto.ShoppingCartDTO;
import com.delivo.entity.Dish;
import com.delivo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RemoveFromCartHandler implements IntentHandler {

    @Autowired
    private DishMatcher dishMatcher;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.REMOVE_FROM_CART && state != AiState.PENDING_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        String dishName = ctx.getIntent().param("dishName", "");
        Dish dish = dishMatcher.findBestMatch(dishName);
        Map<String, Object> params = new HashMap<>();

        if (dish == null) {
            params.put("dishName", dishName);
            return HandlerResult.text(IntentType.REMOVE_FROM_CART, "cart.added.notFound", params);
        }

        BaseContext.setCurrentId(ctx.getUserId());
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setDishId(dish.getId());
        shoppingCartService.subShoppingCart(dto);

        params.put("dishName", dish.getName());
        return HandlerResult.text(IntentType.REMOVE_FROM_CART, "cart.removed.success", params);
    }
}
