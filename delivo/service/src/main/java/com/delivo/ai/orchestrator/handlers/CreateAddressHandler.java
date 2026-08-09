package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.Intent;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.AddressBook;
import com.delivo.entity.ShoppingCart;
import com.delivo.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class CreateAddressHandler implements IntentHandler {

    @Autowired
    private AddressBookService addressBookService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CREATE_ADDRESS;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        Map<String, Object> p = ctx.getIntent().paramsOrEmpty();
        String consignee = asString(p.get("consignee"));
        String phone = asString(p.get("phone"));
        String detail = asString(p.get("detail"));

        List<String> missing = new ArrayList<>();
        if (consignee == null || consignee.isBlank()) missing.add("name");
        if (phone == null || phone.isBlank()) missing.add("phone");
        if (detail == null || detail.isBlank()) missing.add("address");
        if (!missing.isEmpty()) {
            Map<String, Object> textParams = new HashMap<>();
            textParams.put("missing", String.join(", ", missing));
            return HandlerResult.builder()
                    .intentType(IntentType.CREATE_ADDRESS)
                    .textKey("address.creating.missing")
                    .textParams(textParams)
                    .newState(AiState.GUIDING_NEW_ADDRESS)
                    .halts(true)
                    .build();
        }

        AddressBook ab = new AddressBook();
        ab.setUserId(ctx.getUserId());
        ab.setConsignee(consignee);
        ab.setPhone(phone);
        ab.setDetail(detail);
        ab.setSex("1");
        ab.setCityName(asString(p.get("city")) != null ? asString(p.get("city")) : "");
        ab.setDistrictName(asString(p.get("district")) != null ? asString(p.get("district")) : "");
        addressBookService.save(ab);

        HandlerResult result = HandlerResult.builder()
                .intentType(IntentType.CREATE_ADDRESS)
                .textKey("address.created")
                .newState(AiState.RECOMMEND_DISHES)
                .build();

        
        
        
        
        if (hasItems(ctx.getCartSnapshot())) {
            if (ab.getId() == null) {
                log.warn("CreateAddressHandler: saved address has null id, cannot chain SUBMIT_ORDER (userId={})",
                        ctx.getUserId());
            } else {
                Map<String, Object> submitParams = new HashMap<>();
                submitParams.put("addressId", ab.getId());
                result.addFollowUp(Intent.builder()
                        .type(IntentType.SUBMIT_ORDER)
                        .params(submitParams)
                        .rawText(ctx.getIntent().getRawText())
                        .confidence(1.0)
                        .language(ctx.getLanguage())
                        .build());
            }
        }
        return result;
    }

    private boolean hasItems(List<ShoppingCart> cart) {
        return cart != null && !cart.isEmpty();
    }

    private String asString(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
