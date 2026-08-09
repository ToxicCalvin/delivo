package com.delivo.ai.config;

import com.delivo.entity.Orders;
import com.delivo.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class AiStateManager {

    public enum AiState {
        RECOMMEND_DISHES,
        GUIDING_NEW_ADDRESS,
        PENDING_PAYMENT,
        ORDER_COMPLETED
    }

    @Autowired
    private OrderMapper orderMapper;

    private final Map<Long, AiState> userStateMap = new ConcurrentHashMap<>();

    
    /**
     * Retrieves the user's current conversation flow state.
     * To prevent state changes that occurred outside the app (e.g., an order being cancelled
     * in the backend, or an automatic timeout), it reconciles with the database to check the
     * latest order status every time it fetches the state.
     * @param userId The ID of the user
     * @return The current conversation flow state of the user
     */
    public AiState getState(Long userId) {
        AiState cached = userStateMap.getOrDefault(userId, AiState.RECOMMEND_DISHES);

        
        if (cached == AiState.GUIDING_NEW_ADDRESS) {
            return cached;
        }

        AiState derived = deriveFromLatestOrder(userId);
        if (derived != cached) {
            log.debug("User {} state reconcile: cached={} -> derived={}", userId, cached, derived);
            userStateMap.put(userId, derived);
        }
        return derived;
    }

    public void setState(Long userId, AiState state) {
        log.info("User {} state transition -> {}", userId, state);
        userStateMap.put(userId, state);
    }

    private AiState deriveFromLatestOrder(Long userId) {
        Orders latest;
        try {
            latest = orderMapper.getLatestOrderByUserId(userId);
        } catch (Exception e) {
            log.warn("AiStateManager: failed to read latest order for user {}, defaulting to RECOMMEND_DISHES. err={}",
                    userId, e.getMessage());
            return AiState.RECOMMEND_DISHES;
        }
        if (latest == null) {
            return AiState.RECOMMEND_DISHES;
        }

        if (Orders.PENDING_PAYMENT.equals(latest.getStatus())
                && Orders.UN_PAID.equals(latest.getPayStatus())) {
            return AiState.PENDING_PAYMENT;
        }
        return AiState.RECOMMEND_DISHES;
    }
}
