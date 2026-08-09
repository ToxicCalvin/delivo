package com.delivo.ai.orchestrator;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class IntentRouter {

    private final List<IntentHandler> handlers;
    private final IntentHandler defaultHandler;

    @Autowired
    public IntentRouter(List<IntentHandler> handlers,
            @org.springframework.beans.factory.annotation.Qualifier("clarifyHandler") IntentHandler defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }

    /**
     * Routes the extracted intent to the appropriate IntentHandler based on the
     * user's current conversation state and the intent type. If no matching handler
     * is found, it falls back to the default ClarifyHandler.
     * 
     * @param state The current conversation state of the user
     * @param type  The type of intent extracted from the user's message
     * @return The specific IntentHandler capable of processing the request
     */
    public IntentHandler route(AiState state, IntentType type) {
        Optional<IntentHandler> match = handlers.stream()
                .filter(h -> h.supports(state, type))
                .findFirst();
        if (match.isEmpty()) {
            log.info("IntentRouter: no handler for state={}, type={} → defaulting to clarify", state, type);
            return defaultHandler;
        }
        return match.get();
    }
}
