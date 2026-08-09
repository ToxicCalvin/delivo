package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.envelope.EnvelopeComposer;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.ShoppingCart;
import com.delivo.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class ViewCartHandler implements IntentHandler {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private EnvelopeComposer envelopeComposer;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.VIEW_CART;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());
        List<ShoppingCart> cart = shoppingCartService.showShoppingCart();

        if (cart == null || cart.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.VIEW_CART)
                    .textKey("cart.empty")
                    .build();
        }

        List<Map<String, Object>> cards = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        StringBuilder summary = new StringBuilder();
        boolean zh = "zh".equals(ctx.getLanguage());

        for (int i = 0; i < cart.size(); i++) {
            ShoppingCart item = cart.get(i);
            cards.add(envelopeComposer.buildCartCard(item));

            BigDecimal unit = item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO;
            int qty = item.getNumber() != null ? item.getNumber() : 0;
            total = total.add(unit.multiply(new BigDecimal(qty)));

            if (i > 0) summary.append(zh ? "，" : ", ");
            String unitStr = unit.setScale(2, RoundingMode.HALF_UP).toPlainString();
            summary.append(item.getName() != null ? item.getName() : "")
                    .append(zh ? "（¥" : " (¥").append(unitStr).append(zh ? "）x" : ") x").append(qty);
        }

        String totalStr = total.setScale(2, RoundingMode.HALF_UP).toPlainString();
        Map<String, Object> params = new HashMap<>();
        params.put("items", summary.toString());
        params.put("total", totalStr);

        
        String text;
        switch (ctx.getLanguage() == null ? "en" : ctx.getLanguage()) {
            case "zh": text = "你的购物车有：" + summary + "。合计：¥" + totalStr + "。要去结账吗？"; break;
            case "hu": text = "A kosarad: " + summary + ". Összesen: ¥" + totalStr + ". Tovább a fizetéshez?"; break;
            case "ko": text = "장바구니: " + summary + ". 합계: ¥" + totalStr + ". 결제하시겠어요?"; break;
            default:   text = "Your cart contains: " + summary + ". Total: ¥" + totalStr + ". Proceed to checkout?";
        }

        return HandlerResult.builder()
                .intentType(IntentType.VIEW_CART)
                .rawText(text)
                .uiType("carousel")
                .cards(cards)
                .build();
    }
}
