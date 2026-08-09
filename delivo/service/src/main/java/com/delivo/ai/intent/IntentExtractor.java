package com.delivo.ai.intent;

import com.delivo.ai.agent.IntentExtractorAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class IntentExtractor {

    
    private static final double CLARIFY_THRESHOLD = 0.4;

    @Autowired
    private IntentExtractorAiService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Calls the underlying AI model to extract specific business intents (e.g., add to cart,
     * check order status) from the user's input utterance.
     * @param userMessage The original text input from the user
     * @param recentContext The recent conversation context
     * @param cartStatus A brief description of the current shopping cart status
     * @param currentState The user's current business flow state
     * @return A list of extracted intent objects
     */
    public List<Intent> extract(String userMessage,
                                String recentContext,
                                String cartStatus,
                                String currentState) {
        String raw;
        try {
            raw = aiService.extract(
                    userMessage == null ? "" : userMessage,
                    recentContext == null ? "" : recentContext,
                    cartStatus == null ? "EMPTY" : cartStatus,
                    currentState == null ? "RECOMMEND_DISHES" : currentState);
            log.debug("IntentExtractor LLM output: {}", raw);
        } catch (Exception e) {
            log.error("IntentExtractor LLM call failed", e);
            return Collections.singletonList(unknown(userMessage));
        }
        return parse(raw, userMessage);
    }

    List<Intent> parse(String raw, String userMessage) {
        if (raw == null || raw.isBlank()) {
            return Collections.singletonList(unknown(userMessage));
        }

        String json = stripFencesAndExtractObject(raw);
        try {
            JsonNode root = objectMapper.readTree(json);
            String language = root.path("language").asText("en");
            JsonNode intentsNode = root.path("intents");

            List<Intent> result = new ArrayList<>();
            if (intentsNode.isArray()) {
                Iterator<JsonNode> it = intentsNode.elements();
                while (it.hasNext()) {
                    JsonNode item = it.next();
                    Intent intent = parseOne(item, userMessage, language);
                    if (intent != null) result.add(intent);
                }
            }

            if (result.isEmpty()) {
                return Collections.singletonList(clarify(userMessage, language));
            }
            return result;
        } catch (Exception e) {
            log.warn("IntentExtractor: failed to parse JSON, falling back to UNKNOWN. err={}, raw={}",
                    e.getMessage(), raw);
            return Collections.singletonList(unknown(userMessage));
        }
    }

    private Intent parseOne(JsonNode node, String userMessage, String language) {
        String typeStr = node.path("type").asText("UNKNOWN");
        IntentType type;
        try {
            type = IntentType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            log.warn("IntentExtractor: unknown intent type '{}', defaulting to UNKNOWN", typeStr);
            type = IntentType.UNKNOWN;
        }

        double confidence = node.path("confidence").asDouble(1.0);

        Map<String, Object> params;
        JsonNode paramsNode = node.path("params");
        if (paramsNode.isObject()) {
            try {
                params = objectMapper.convertValue(paramsNode, Map.class);
            } catch (Exception e) {
                log.warn("IntentExtractor: failed to parse params for {}, defaulting to empty", type);
                params = new HashMap<>();
            }
        } else {
            params = new HashMap<>();
        }

        
        if (confidence < CLARIFY_THRESHOLD && type != IntentType.CLARIFY) {
            log.info("IntentExtractor: confidence {} < {} for {}, demoting to CLARIFY",
                    confidence, CLARIFY_THRESHOLD, type);
            return clarify(userMessage, language);
        }

        return Intent.builder()
                .type(type)
                .params(params)
                .rawText(userMessage)
                .confidence(confidence)
                .language(language)
                .build();
    }

    private Intent unknown(String userMessage) {
        return Intent.builder()
                .type(IntentType.UNKNOWN)
                .params(new HashMap<>())
                .rawText(userMessage)
                .confidence(0.0)
                .language("en")
                .build();
    }

    private Intent clarify(String userMessage, String language) {
        return Intent.builder()
                .type(IntentType.CLARIFY)
                .params(new HashMap<>())
                .rawText(userMessage)
                .confidence(0.0)
                .language(language)
                .build();
    }

    
    private String stripFencesAndExtractObject(String input) {
        String s = input.trim();
        if (s.startsWith("```")) {
            s = s.replaceAll("(?s)^```(?:json|JSON)?\\s*", "").replaceAll("```\\s*$", "").trim();
        }
        int start = s.indexOf('{');
        if (start < 0) return s;

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (c == '\\' && inString) { escaped = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (!inString) {
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return s.substring(start, i + 1);
                }
            }
        }
        return s.substring(start);
    }
}
