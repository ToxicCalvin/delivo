package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.envelope.EnvelopeComposer;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.ai.retrieval.DishSearchQuery;
import com.delivo.ai.retrieval.DishSearchResult;
import com.delivo.ai.retrieval.DishSearchService;
import com.delivo.entity.Dish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RecommendDishesHandler implements IntentHandler {

    @Autowired
    private DishSearchService dishSearchService;

    @Autowired
    private EnvelopeComposer envelopeComposer;

    
    private static final List<Long> NON_MAIN_CATEGORIES = Arrays.asList(205L, 206L, 207L); 
    private static final long COMBO_SIDE_CATEGORY = 205L;
    private static final long COMBO_DRINK_CATEGORY = 207L;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.RECOMMEND_DISHES && state != AiState.PENDING_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        Map<String, Object> p = ctx.getIntent().paramsOrEmpty();
        boolean mealPlanning = Boolean.TRUE.equals(p.get("mealPlanning"));

        if (mealPlanning) {
            return handleCombo(ctx);
        }

        DishSearchQuery query = buildQuery(p, null);
        DishSearchResult result = dishSearchService.search(query);

        if (result.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.RECOMMEND_DISHES)
                    .needsNlPolish(true)
                    .polishGoal("Tell the user we couldn't find dishes matching their request and invite them to try a different keyword.")
                    .polishContext("Search params: " + p)
                    .build();
        }

        List<Map<String, Object>> cards = result.getDishes().stream()
                .map(envelopeComposer::buildDishCard)
                .collect(Collectors.toList());

        String dishNamesForLog = result.getDishes().stream().map(Dish::getName).collect(Collectors.joining(", "));
        String contextLine = "Found " + cards.size() + " dishes: " + dishNamesForLog;

        return HandlerResult.builder()
                .intentType(IntentType.RECOMMEND_DISHES)
                .uiType("carousel")
                .cards(cards)
                .needsNlPolish(true)
                .polishGoal("Briefly introduce the recommended dishes shown in the cards. Keep it warm and short.")
                .polishContext(contextLine)
                .build();
    }

    
    private static final double MAIN_BUDGET_RATIO = 0.60;
    private static final double SIDE_BUDGET_RATIO = 0.25;
    private static final double DRINK_BUDGET_RATIO = 0.15;

    
    private HandlerResult handleCombo(HandlerContext ctx) {
        Map<String, Object> p = ctx.getIntent().paramsOrEmpty();
        List<Map<String, Object>> cards = new ArrayList<>();
        List<String> picked = new ArrayList<>();

        Double totalBudget = asDouble(p.get("maxPrice"));

        
        DishSearchQuery mainQuery = DishSearchQuery.builder()
                .keyword(asString(p.get("keyword")))
                .maxPrice(totalBudget != null ? totalBudget * MAIN_BUDGET_RATIO : null)
                .excludeCategoryIds(NON_MAIN_CATEGORIES)
                .build();
        DishSearchResult mainResult = dishSearchService.search(mainQuery);
        if (!mainResult.isEmpty()) {
            Dish first = mainResult.getDishes().get(0);
            cards.add(envelopeComposer.buildDishCard(first));
            picked.add(first.getName());
        }

        
        double[] sideAndDrinkRatios = {SIDE_BUDGET_RATIO, DRINK_BUDGET_RATIO};
        long[] sideAndDrinkCategories = {COMBO_SIDE_CATEGORY, COMBO_DRINK_CATEGORY};
        for (int i = 0; i < sideAndDrinkCategories.length; i++) {
            DishSearchQuery query = DishSearchQuery.builder()
                    .keyword(asString(p.get("keyword")))
                    .maxPrice(totalBudget != null ? totalBudget * sideAndDrinkRatios[i] : null)
                    .categoryId(sideAndDrinkCategories[i])
                    .build();
            DishSearchResult result = dishSearchService.search(query);
            if (!result.isEmpty()) {
                Dish first = result.getDishes().get(0);
                cards.add(envelopeComposer.buildDishCard(first));
                picked.add(first.getName());
            }
        }

        if (cards.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.RECOMMEND_DISHES)
                    .needsNlPolish(true)
                    .polishGoal("Tell the user we couldn't put together a combo right now and ask them to try another preference.")
                    .polishContext("")
                    .build();
        }

        return HandlerResult.builder()
                .intentType(IntentType.RECOMMEND_DISHES)
                .uiType("combo_list")
                .cards(cards)
                .needsNlPolish(true)
                .polishGoal("Introduce a combo (main + side + drink). Mention only that it's a balanced combo, not the dish names.")
                .polishContext("Picked: " + String.join(", ", picked))
                .build();
    }

    private DishSearchQuery buildQuery(Map<String, Object> p, Long categoryOverride) {
        return DishSearchQuery.builder()
                .keyword(asString(p.get("keyword")))
                .maxPrice(asDouble(p.get("maxPrice")))
                .categoryId(categoryOverride != null ? categoryOverride : asLong(p.get("categoryId")))
                .build();
    }

    private String asString(Object v) { return v == null ? null : v.toString(); }
    private Double asDouble(Object v) { return (v instanceof Number) ? ((Number) v).doubleValue() : null; }
    private Long asLong(Object v) { return (v instanceof Number) ? ((Number) v).longValue() : null; }
}
