package com.delivo.ai.envelope;

import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.entity.Dish;
import com.delivo.entity.ShoppingCart;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EnvelopeComposer {

    @Autowired
    private MessageTextTemplates templates;

    @Autowired
    private NlPolisher nlPolisher;

    @Autowired
    private QuickReplyCatalog quickReplyCatalog;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Composes the final JSON response envelope that will be sent to the frontend.
     * 
     * @param result   The result object returned from the intent handler
     * @param language The language code for text rendering
     * @return A JSON string representing the final structured response
     */
    public String compose(HandlerResult result, String language) {
        if (result == null) {
            return buildJsonEnvelope("", "carousel", Collections.emptyList(), Collections.emptyList(), null);
        }

        String text = renderText(result, language);
        text = sanitizeText(text);

        String uiType = result.getUiType() != null ? result.getUiType() : "carousel";
        List<Map<String, Object>> cards = result.getCards() != null ? result.getCards() : new ArrayList<>();
        List<String> quickReplies = result.getQuickReplies();
        if (quickReplies == null || quickReplies.isEmpty()) {
            quickReplies = quickReplyCatalog.defaultsFor(result.getIntentType(), language);
        }
        Object action = result.getAction();

        return buildJsonEnvelope(text, uiType, cards, quickReplies, action);
    }

    private String renderText(HandlerResult result, String language) {

        if (result.getRawText() != null && !result.getRawText().isBlank()) {
            return result.getRawText();
        }

        if (result.isNeedsNlPolish()) {
            return nlPolisher.polish(language, result.getPolishGoal(), result.getPolishContext());
        }

        if (result.getTextKey() != null) {
            return templates.render(result.getTextKey(), language, result.getTextParams());
        }

        return "";
    }

    public Map<String, Object> buildDishCard(Dish dish) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", String.valueOf(dish.getId()));
        card.put("name", dish.getName());
        card.put("price", dish.getPrice());
        card.put("image", dish.getImage() != null ? dish.getImage() : "");
        card.put("description", dish.getDescription() != null ? dish.getDescription() : "");
        return card;
    }

    public Map<String, Object> buildCartCard(ShoppingCart item) {
        Map<String, Object> card = new LinkedHashMap<>();
        String id = item.getDishId() != null ? String.valueOf(item.getDishId())
                : (item.getSetmealId() != null ? String.valueOf(item.getSetmealId()) : "");
        card.put("id", id);
        card.put("name", item.getName() != null ? item.getName() : "");
        card.put("price", item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
        card.put("image", item.getImage() != null ? item.getImage() : "");
        card.put("description", "");
        return card;
    }

    public Map<String, Object> buildPendingPaymentAction(String orderNumber, BigDecimal amount) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "pending_payment");
        action.put("summary", "Payment required to finish order");
        if (orderNumber != null)
            action.put("orderNumber", orderNumber);
        if (amount != null)
            action.put("orderAmount", amount.setScale(2, RoundingMode.HALF_UP));
        return action;
    }

    public Map<String, Object> buildCartAddAction(String summary) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("type", "cart_add");
        action.put("summary", summary == null ? "" : summary);
        return action;
    }

    public String buildJsonEnvelope(String text, String uiType, List<Map<String, Object>> cards,
            List<String> quickReplies, Object action) {
        try {
            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("text", text == null ? "" : text);
            envelope.put("uiType", uiType == null ? "carousel" : uiType);
            envelope.put("cards", cards == null ? Collections.emptyList() : cards);
            envelope.put("quickReplies", quickReplies == null ? Collections.emptyList() : quickReplies);
            envelope.put("action", action);
            return objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            log.error("Failed to serialize JSON envelope", e);
            return text == null ? "" : text;
        }
    }

    private String sanitizeText(String raw) {
        if (raw == null)
            return "";
        String s = raw;

        s = s.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        s = s.replaceAll("__(.+?)__", "$1");

        s = s.replaceAll("(?s)^```(?:json|text)?\\s*", "").replaceAll("```\\s*$", "");
        return s.trim();
    }
}
