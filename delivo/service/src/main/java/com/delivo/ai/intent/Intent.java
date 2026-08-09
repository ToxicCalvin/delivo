package com.delivo.ai.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;


@Getter
@Builder
@AllArgsConstructor
public class Intent {

    private final IntentType type;

    
    private final Map<String, Object> params;

    
    private final String rawText;

    
    private final double confidence;

    
    private final String language;

    
    public Map<String, Object> paramsOrEmpty() {
        return params == null ? Collections.emptyMap() : params;
    }

    
    @SuppressWarnings("unchecked")
    public <T> T param(String key, T defaultValue) {
        Map<String, Object> p = paramsOrEmpty();
        Object v = p.get(key);
        if (v == null) return defaultValue;
        try {
            return (T) v;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
