package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.envelope.EnvelopeComposer;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.ai.tool.DishMatcher;
import com.delivo.context.BaseContext;
import com.delivo.dto.ShoppingCartDTO;
import com.delivo.entity.Dish;
import com.delivo.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class AddToCartHandler implements IntentHandler {

    @Autowired
    private DishMatcher dishMatcher;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private EnvelopeComposer envelopeComposer;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.ADD_TO_CART
                && state != AiState.PENDING_PAYMENT;  
    }

    @Override
    @SuppressWarnings("unchecked")
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        Object itemsObj = ctx.getIntent().paramsOrEmpty().get("items");
        List<Map<String, Object>> items = (itemsObj instanceof List) ? (List<Map<String, Object>>) itemsObj
                                                                     : Collections.emptyList();

        List<String> addedNames = new ArrayList<>();
        List<String> missedNames = new ArrayList<>();
        int totalAdded = 0;

        for (Map<String, Object> item : items) {
            String dishName = item.get("dishName") != null ? item.get("dishName").toString() : "";
            String flavor = item.get("flavor") != null ? item.get("flavor").toString() : "";
            int qty = parseQuantity(item.get("quantity"));

            Dish dish = dishMatcher.findBestMatch(dishName);
            if (dish == null) {
                missedNames.add(dishName);
                continue;
            }

            ShoppingCartDTO dto = new ShoppingCartDTO();
            dto.setDishId(dish.getId());
            dto.setDishFlavor(flavor);
            for (int i = 0; i < qty; i++) {
                shoppingCartService.addShoppingCart(dto);
            }
            addedNames.add(dish.getName() + " x" + qty);
            totalAdded += qty;
        }

        Map<String, Object> textParams = new HashMap<>();
        Map<String, Object> action = null;
        String textKey;

        if (totalAdded == 0 && !missedNames.isEmpty()) {
            textParams.put("dishName", String.join(", ", missedNames));
            textKey = "cart.added.notFound";
        } else if (addedNames.size() == 1) {
            textParams.put("dishName", addedNames.get(0));
            textKey = "cart.added.success";
            action = envelopeComposer.buildCartAddAction("Added " + addedNames.get(0));
        } else {
            textParams.put("count", totalAdded);
            textKey = "cart.added.multi";
            action = envelopeComposer.buildCartAddAction("Added " + totalAdded + " items");
        }

        return HandlerResult.builder()
                .intentType(IntentType.ADD_TO_CART)
                .textKey(textKey)
                .textParams(textParams)
                .action(action)
                .build();
    }

    private int parseQuantity(Object q) {
        if (q == null) return 1;
        if (q instanceof Number) return Math.max(1, ((Number) q).intValue());
        try {
            return Math.max(1, Integer.parseInt(q.toString()));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
