package com.delivo.controller.user;

import com.delivo.ai.orchestrator.AiOrchestrator;
import com.delivo.context.BaseContext;
import com.delivo.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin REST entry point for the chat assistant.
 */
@RestController("userAiChatController")
@RequestMapping("/user/ai")
@Api(tags = "AI Chat Interface")
@Slf4j
public class AiChatController {

    @Autowired
    private AiOrchestrator orchestrator;

    @PostMapping("/chat")
    @ApiOperation("Talk to AI Assistant")
    public Result<String> chat(@RequestBody String message) {
        log.info("Received AI chat message: {}", message);
        try {
            String sanitized = sanitizeMessage(message);
            if (sanitized == null || sanitized.trim().isEmpty()) {
                return Result.error("Message cannot be empty.");
            }
            Long userId = resolveUserId();
            String envelope = orchestrator.handle(userId, sanitized);
            return Result.success(envelope);
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return Result.error("AI service is currently unavailable. " + e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ApiOperation("Clear AI Chat Memory")
    public Result<String> clearMemory() {
        Long userId = resolveUserId();
        orchestrator.clearMemory(userId);
        log.info("Cleared AI chat memory for user: {}", userId);
        return Result.success("Memory cleared");
    }

    private Long resolveUserId() {
        Long userId = BaseContext.getCurrentId();
        return (userId != null) ? userId : 0L;
    }

    private String sanitizeMessage(String message) {
        if (message == null)
            return null;
        message = message.trim();
        if (message.startsWith("\"") && message.endsWith("\"") && message.length() >= 2) {
            message = message.substring(1, message.length() - 1);
        }
        message = message.replace("\\\"", "\"").replace("\\n", "\n");
        return message.trim();
    }
}
