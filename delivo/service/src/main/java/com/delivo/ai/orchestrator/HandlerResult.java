package com.delivo.ai.orchestrator;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.Intent;
import com.delivo.ai.intent.IntentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@Builder
public class HandlerResult {

    
    private IntentType intentType;

    
    private AiState newState;

    

    
    private String textKey;

    
    private Map<String, Object> textParams;

    
    private String rawText;

    
    private boolean needsNlPolish;

    
    private String polishGoal;

    
    private String polishContext;

    

    
    private String uiType;

    
    private List<Map<String, Object>> cards;

    
    private List<String> quickReplies;

    
    private Map<String, Object> action;

    

    
    private boolean halts;

    
    private List<Intent> followUpIntents;

    

    
    public static HandlerResult empty(IntentType type) {
        return HandlerResult.builder().intentType(type).build();
    }

    
    public static HandlerResult text(IntentType type, String textKey, Map<String, Object> params) {
        return HandlerResult.builder()
                .intentType(type)
                .textKey(textKey)
                .textParams(params)
                .build();
    }

    
    public void addFollowUp(Intent intent) {
        if (followUpIntents == null) followUpIntents = new ArrayList<>();
        followUpIntents.add(intent);
    }
}
