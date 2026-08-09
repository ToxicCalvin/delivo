package com.delivo.ai.orchestrator;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.Intent;
import com.delivo.entity.ShoppingCart;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class HandlerContext {

    
    private final Long userId;

    
    private final String language;

    
    private final AiState state;

    
    private final Intent intent;

    
    private final String recentContext;

    
    private final List<ShoppingCart> cartSnapshot;
}
