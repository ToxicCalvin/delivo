package com.delivo.ai.tool;

import com.delivo.context.BaseContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class AiContext {

    private AiContext() {}

    
    public static Long resolveAndSetUserId(Long memoryId) {
        Long userId = memoryId;
        if (userId == null || userId <= 0) {
            userId = BaseContext.getCurrentId();
        }
        if (userId == null || userId <= 0) {
            log.warn("No authenticated user found, falling back to demo user 1");
            userId = 1L;
        }
        BaseContext.setCurrentId(userId);
        return userId;
    }
}
