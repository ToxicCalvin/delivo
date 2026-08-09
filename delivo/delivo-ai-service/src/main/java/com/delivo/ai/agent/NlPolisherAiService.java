package com.delivo.ai.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface NlPolisherAiService {

    @SystemMessage(fromResource = "prompts/nl-polish.txt")
    String polish(@UserMessage String instruction);
}
