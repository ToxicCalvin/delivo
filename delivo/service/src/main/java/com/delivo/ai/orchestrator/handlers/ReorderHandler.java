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
import com.delivo.entity.OrderDetail;
import com.delivo.entity.Orders;
import com.delivo.mapper.OrderDetailMapper;
import com.delivo.mapper.OrderMapper;
import com.delivo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class ReorderHandler implements IntentHandler {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private DishMatcher dishMatcher;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.REORDER && state != AiState.PENDING_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        Orders latest = orderMapper.getLatestOrderByUserId(ctx.getUserId());
        if (latest == null) {
            return HandlerResult.builder()
                    .intentType(IntentType.REORDER)
                    .textKey("reorder.none")
                    .build();
        }

        List<OrderDetail> details = orderDetailMapper.getByOrderId(latest.getId());
        if (details == null || details.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.REORDER)
                    .textKey("reorder.none")
                    .build();
        }

        int added = 0;
        for (OrderDetail det : details) {
            Dish dish = dishMatcher.findBestMatch(det.getName());
            if (dish == null) continue;

            int qty = det.getNumber() != null ? det.getNumber() : 1;
            ShoppingCartDTO dto = new ShoppingCartDTO();
            dto.setDishId(dish.getId());
            for (int i = 0; i < qty; i++) {
                shoppingCartService.addShoppingCart(dto);
                added++;
            }
        }

        Map<String, Object> textParams = new HashMap<>();
        textParams.put("count", added);
        return HandlerResult.builder()
                .intentType(IntentType.REORDER)
                .textKey("reorder.added")
                .textParams(textParams)
                .build();
    }
}
