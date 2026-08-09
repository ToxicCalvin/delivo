package com.delivo.ai.orchestrator;

import com.delivo.ai.config.AiStateManager;
import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.config.RedisChatMemoryStore;
import com.delivo.ai.envelope.EnvelopeComposer;
import com.delivo.ai.intent.Intent;
import com.delivo.ai.intent.IntentExtractor;
import com.delivo.ai.intent.IntentType;
import com.delivo.context.BaseContext;
import com.delivo.entity.ShoppingCart;
import com.delivo.service.ShoppingCartService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;


@Component
@Slf4j
public class AiOrchestrator {

    private static final int MAX_RECENT_TURNS = 6; 

    @Autowired
    private IntentExtractor intentExtractor;

    @Autowired
    private IntentRouter intentRouter;

    @Autowired
    private EnvelopeComposer envelopeComposer;

    @Autowired
    private AiStateManager stateManager;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private RedisChatMemoryStore memoryStore;

    @PostConstruct
    public void init() {
        this.memoryStore = new RedisChatMemoryStore(
                stringRedisTemplate, "delivo:chat:memory:", Duration.ofHours(24));
    }

    
    /**
     * The main entry point for the AI chat processing.
     * Receives the user message, extracts the intent using the LLM, and routes the intent
     * to the specific handler based on the current state. The handler executes the relevant
     * business logic (e.g., ordering, refunding) and generates the response for the user.
     * @param userId The ID of the user
     * @param userMessage The original text input from the user
     * @return The final JSON encapsulated response to be returned to the frontend
     */
    public String handle(Long userId, String userMessage) {
        BaseContext.setCurrentId(userId);

        String language = detectLanguage(userMessage);
        AiState state = stateManager.getState(userId);
        List<ShoppingCart> cart = safeCart();
        String cartStatus = (cart == null || cart.isEmpty()) ? "EMPTY" : "NOT EMPTY (" + cart.size() + " items)";
        String recentContext = readRecentContext(userId);

        
        List<Intent> intents = intentExtractor.extract(userMessage, recentContext, cartStatus, state.name());
        log.info("AiOrchestrator: userId={}, state={}, language={}, intents={}",
                userId, state, language, intents.stream().map(i -> i.getType().name()).toList());

        Deque<Intent> queue = new ArrayDeque<>(intents);
        HandlerResult lastResult = null;
        int safetyBudget = 8; 

        while (!queue.isEmpty() && safetyBudget-- > 0) {
            Intent intent = queue.poll();

            HandlerContext ctx = HandlerContext.builder()
                    .userId(userId)
                    .language(intent.getLanguage() != null ? intent.getLanguage() : language)
                    .state(state)
                    .intent(intent)
                    .recentContext(recentContext)
                    .cartSnapshot(cart)
                    .build();

            
            IntentHandler handler = intentRouter.route(state, intent.getType());
            HandlerResult result;
            try {
                
                result = handler.handle(ctx);
            } catch (Exception e) {
                log.error("Handler {} threw on intent {}", handler.getClass().getSimpleName(), intent.getType(), e);
                result = HandlerResult.builder()
                        .intentType(intent.getType())
                        .needsNlPolish(true)
                        .polishGoal("Apologise briefly for an internal error and invite the user to try again.")
                        .polishContext("Error in handler: " + e.getMessage())
                        .halts(true)
                        .build();
            }

            
            if (result != null && result.getNewState() != null) {
                
                stateManager.setState(userId, result.getNewState());
                state = result.getNewState();
            }

            if (result != null && result.getFollowUpIntents() != null) {
                for (Intent f : result.getFollowUpIntents()) queue.addFirst(f);
            }

            lastResult = preferUserVisible(lastResult, result);

            if (result != null && result.isHalts()) break;
        }

        String language2 = lastResult != null && lastResult.getCards() != null && !lastResult.getCards().isEmpty()
                ? language : language;
        
        String envelope = envelopeComposer.compose(lastResult, language2);

        
        appendTurn(userId, userMessage, lastResult);

        return envelope;
    }

    
    private HandlerResult preferUserVisible(HandlerResult prev, HandlerResult next) {
        if (next == null) return prev;
        boolean hasContent = next.getTextKey() != null
                || (next.getRawText() != null && !next.getRawText().isBlank())
                || next.isNeedsNlPolish()
                || (next.getCards() != null && !next.getCards().isEmpty())
                || next.getAction() != null;
        return hasContent ? next : prev;
    }

    
    public void clearMemory(Long userId) {
        memoryStore.deleteMessages(userId);
    }

    

    private List<ShoppingCart> safeCart() {
        try {
            List<ShoppingCart> cart = shoppingCartService.showShoppingCart();
            return cart == null ? Collections.emptyList() : cart;
        } catch (Exception e) {
            log.warn("AiOrchestrator: failed to read cart, treating as empty. err={}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String readRecentContext(Long userId) {
        try {
            List<ChatMessage> messages = memoryStore.getMessages(userId);
            if (messages == null || messages.isEmpty()) return "";

            int from = Math.max(0, messages.size() - MAX_RECENT_TURNS);
            StringBuilder sb = new StringBuilder();
            for (int i = from; i < messages.size(); i++) {
                ChatMessage m = messages.get(i);
                if (m instanceof UserMessage um) {
                    sb.append("user: ").append(um.singleText()).append("\n");
                } else if (m instanceof AiMessage am) {
                    sb.append("assistant: ").append(am.text() == null ? "" : am.text()).append("\n");
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("AiOrchestrator: failed to read recent context. err={}", e.getMessage());
            return "";
        }
    }

    /**
     * Appends the user's message and the AI's response for the current turn
     * into the Redis conversation memory.
     * @param userId The ID of the user
     * @param userMessage The text sent by the user
     * @param result The final result object after AI processing
     */
    private void appendTurn(Long userId, String userMessage, HandlerResult result) {
        try {
            List<ChatMessage> existing = memoryStore.getMessages(userId);
            List<ChatMessage> updated = new ArrayList<>(existing == null ? Collections.emptyList() : existing);

            updated.add(UserMessage.from(userMessage == null ? "" : userMessage));

            String assistantText = "";
            if (result != null) {
                if (result.getRawText() != null && !result.getRawText().isBlank()) {
                    assistantText = result.getRawText();
                } else if (result.getTextKey() != null) {
                    assistantText = "[" + result.getTextKey() + "]";
                } else if (result.isNeedsNlPolish() && result.getPolishGoal() != null) {
                    assistantText = "[polish] " + result.getPolishGoal();
                }
            }
            updated.add(AiMessage.from(assistantText));

            
            int max = MAX_RECENT_TURNS * 2;
            if (updated.size() > max) {
                updated = new ArrayList<>(updated.subList(updated.size() - max, updated.size()));
            }
            memoryStore.updateMessages(userId, updated);
        } catch (Exception e) {
            log.warn("AiOrchestrator: failed to persist chat memory. err={}", e.getMessage());
        }
    }

    
    public static String detectLanguage(String message) {
        if (message == null || message.isBlank()) return "en";
        for (char c : message.toCharArray()) {
            Character.UnicodeScript script = Character.UnicodeScript.of(c);
            if (script == Character.UnicodeScript.HAN) return "zh";
            
            
            
            if (script == Character.UnicodeScript.HANGUL) return "ko";
        }
        return "en";
    }

    
    public static boolean isHaltingType(IntentType type) {
        return type == IntentType.CLARIFY || type == IntentType.UNKNOWN;
    }
}
