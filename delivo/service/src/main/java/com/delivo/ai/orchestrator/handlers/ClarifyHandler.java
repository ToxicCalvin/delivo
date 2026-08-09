package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import org.springframework.stereotype.Component;


@Component("clarifyHandler")
public class ClarifyHandler implements IntentHandler {

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CLARIFY || type == IntentType.UNKNOWN;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        return HandlerResult.builder()
                .intentType(IntentType.CLARIFY)
                .needsNlPolish(true)
                .polishGoal("Politely ask the user to clarify what they want — keep it short and inviting.")
                .polishContext("The user said: \"" + ctx.getIntent().getRawText() + "\"")
                .halts(true)
                .build();
    }
}
