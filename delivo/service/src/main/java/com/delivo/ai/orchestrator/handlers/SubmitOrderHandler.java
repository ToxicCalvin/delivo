package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.envelope.EnvelopeComposer;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.dto.OrdersSubmitDTO;
import com.delivo.entity.ShoppingCart;
import com.delivo.service.AddressBookService;
import com.delivo.service.OrderService;
import com.delivo.service.ShoppingCartService;
import com.delivo.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class SubmitOrderHandler implements IntentHandler {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private EnvelopeComposer envelopeComposer;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.SUBMIT_ORDER;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        Long addressId = asLong(ctx.getIntent().paramsOrEmpty().get("addressId"));
        if (addressId == null || addressBookService.getById(addressId) == null) {
            Map<String, Object> textParams = new HashMap<>();
            textParams.put("reason", "missing or invalid address");
            return HandlerResult.builder()
                    .intentType(IntentType.SUBMIT_ORDER)
                    .textKey("order.submit.failed")
                    .textParams(textParams)
                    .halts(true)
                    .build();
        }

        List<ShoppingCart> cart = shoppingCartService.showShoppingCart();
        if (cart == null || cart.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.SUBMIT_ORDER)
                    .textKey("cart.empty")
                    .halts(true)
                    .build();
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCart line : cart) {
            BigDecimal unit = line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO;
            int qty = line.getNumber() != null ? line.getNumber() : 0;
            total = total.add(unit.multiply(new BigDecimal(qty)));
        }

        OrdersSubmitDTO dto = new OrdersSubmitDTO();
        dto.setAddressBookId(addressId);
        dto.setPayMethod(1); 
        dto.setRemark("Ordered via AI Assistant");
        dto.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        dto.setDeliveryStatus(1);
        dto.setTablewareNumber(1);
        dto.setTablewareStatus(1);
        dto.setPackAmount(0);
        dto.setAmount(total);

        try {
            OrderSubmitVO vo = orderService.submitOrder(dto);
            Map<String, Object> textParams = new HashMap<>();
            textParams.put("amount", vo.getOrderAmount() == null ? "" : vo.getOrderAmount().toPlainString());
            return HandlerResult.builder()
                    .intentType(IntentType.SUBMIT_ORDER)
                    .textKey("order.submitted")
                    .textParams(textParams)
                    .newState(AiState.PENDING_PAYMENT)
                    .action(envelopeComposer.buildPendingPaymentAction(vo.getOrderNumber(), vo.getOrderAmount()))
                    .build();
        } catch (Exception e) {
            log.error("submitOrder failed", e);
            Map<String, Object> textParams = new HashMap<>();
            textParams.put("reason", e.getMessage() == null ? "internal error" : e.getMessage());
            return HandlerResult.builder()
                    .intentType(IntentType.SUBMIT_ORDER)
                    .textKey("order.submit.failed")
                    .textParams(textParams)
                    .halts(true)
                    .build();
        }
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
