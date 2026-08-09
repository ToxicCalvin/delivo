package com.delivo.ai.orchestrator;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;


public interface IntentHandler {

    
    boolean supports(AiState state, IntentType type);

    
    HandlerResult handle(HandlerContext ctx);
}
