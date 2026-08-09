package com.delivo.ai.envelope;

import com.delivo.ai.agent.NlPolisherAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class NlPolisher {

    @Autowired
    private NlPolisherAiService aiService;

    
    public String polish(String language, String goal, String context) {
        String instruction = buildInstruction(language, goal, context);
        try {
            String out = aiService.polish(instruction);
            return out == null ? goal : out.trim();
        } catch (Exception e) {
            log.warn("NlPolisher LLM call failed, falling back to goal text. err={}", e.toString());
            return goal;
        }
    }

    private String buildInstruction(String language, String goal, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Language: ").append(language == null ? "en" : language).append("\n");
        sb.append("Goal: ").append(goal == null ? "" : goal).append("\n");
        if (context != null && !context.isBlank()) {
            sb.append("Context: ").append(context).append("\n");
        }
        return sb.toString();
    }
}
