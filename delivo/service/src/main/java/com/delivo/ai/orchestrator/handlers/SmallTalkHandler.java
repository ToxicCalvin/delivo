package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import org.springframework.stereotype.Component;


@Component
public class SmallTalkHandler implements IntentHandler {

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.SMALL_TALK;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        return HandlerResult.builder()
                .intentType(IntentType.SMALL_TALK)
                .needsNlPolish(true)
                .polishGoal("Reply to the user's small talk warmly, then steer the conversation back to ordering food.")
                .polishContext("The user said: \"" + ctx.getIntent().getRawText() + "\"")
                .build();
    }
}
