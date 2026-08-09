package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.intent.Intent;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.AddressBook;
import com.delivo.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Component
@Slf4j
public class ProvideAddressHandler implements IntentHandler {

    @Autowired
    private AddressBookService addressBookService;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.PROVIDE_ADDRESS && state != AiState.PENDING_PAYMENT;
    }

    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        AddressBook query = new AddressBook();
        query.setUserId(ctx.getUserId());
        List<AddressBook> addresses = addressBookService.list(query);
        if (addresses == null || addresses.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.PROVIDE_ADDRESS)
                    .textKey("address.none.askCreate")
                    .newState(AiState.GUIDING_NEW_ADDRESS)
                    .halts(true)
                    .build();
        }

        Map<String, Object> p = ctx.getIntent().paramsOrEmpty();
        Long addressId = asLong(p.get("addressId"));
        AddressBook resolved = null;

        if (addressId != null) {
            
            if (addressId >= 1 && addressId <= addresses.size()) {
                resolved = addresses.get(addressId.intValue() - 1);
            } else {
                
                for (AddressBook a : addresses) {
                    if (addressId.equals(a.getId())) { resolved = a; break; }
                }
            }
        }

        if (resolved == null) {
            String hint = ctx.getIntent().param("addressHint", "");
            resolved = matchByHint(addresses, hint);
        }

        if (resolved == null) {
            return HandlerResult.builder()
                    .intentType(IntentType.PROVIDE_ADDRESS)
                    .textKey("address.notFound")
                    .halts(true)
                    .build();
        }

        
        HandlerResult result = HandlerResult.builder()
                .intentType(IntentType.PROVIDE_ADDRESS)
                .build();
        Map<String, Object> params = new HashMap<>();
        params.put("addressId", resolved.getId());
        result.addFollowUp(Intent.builder()
                .type(IntentType.SUBMIT_ORDER)
                .params(params)
                .rawText(ctx.getIntent().getRawText())
                .confidence(1.0)
                .language(ctx.getLanguage())
                .build());
        return result;
    }

    private AddressBook matchByHint(List<AddressBook> addresses, String hint) {
        if (hint == null || hint.isBlank()) return null;
        String h = hint.toLowerCase(Locale.ROOT);
        AddressBook match = null;
        int hits = 0;
        for (AddressBook a : addresses) {
            String hay = ((a.getConsignee() == null ? "" : a.getConsignee()) + " "
                    + (a.getLabel() == null ? "" : a.getLabel()) + " "
                    + (a.getDetail() == null ? "" : a.getDetail())).toLowerCase(Locale.ROOT);
            if (hay.contains(h)) { match = a; hits++; }
        }
        return hits == 1 ? match : null;
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
