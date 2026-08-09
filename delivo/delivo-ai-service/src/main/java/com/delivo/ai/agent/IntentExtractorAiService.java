package com.delivo.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface IntentExtractorAiService {

    @SystemMessage(fromResource = "prompts/intent-extractor.txt")
    String extract(@UserMessage String userMessage,
                   @V("context") String recentContext,
                   @V("cartStatus") String cartStatus,
                   @V("currentState") String currentState);
}
