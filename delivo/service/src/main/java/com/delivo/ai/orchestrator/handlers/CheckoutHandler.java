package com.delivo.ai.orchestrator.handlers;

import com.delivo.ai.config.AiStateManager.AiState;
import com.delivo.ai.envelope.AddressListFormatter;
import com.delivo.ai.envelope.MessageTextTemplates;
import com.delivo.ai.intent.Intent;
import com.delivo.ai.intent.IntentType;
import com.delivo.ai.orchestrator.HandlerContext;
import com.delivo.ai.orchestrator.HandlerResult;
import com.delivo.ai.orchestrator.IntentHandler;
import com.delivo.context.BaseContext;
import com.delivo.entity.AddressBook;
import com.delivo.entity.ShoppingCart;
import com.delivo.service.AddressBookService;
import com.delivo.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Component
@Slf4j
public class CheckoutHandler implements IntentHandler {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private AddressListFormatter addressListFormatter;

    @Autowired
    private MessageTextTemplates templates;

    @Override
    public boolean supports(AiState state, IntentType type) {
        return type == IntentType.CHECKOUT && state != AiState.PENDING_PAYMENT;
    }

    /**
     * Handles the checkout intent.
     * Validates if the cart is empty, checks if the user has an existing address,
     * and either asks the user to provide/select an address or automatically routes
     * to the SUBMIT_ORDER intent if an address hint perfectly matches.
     * @param ctx The current handler context containing user data and intent details
     * @return The processing result, which may halt for user input or chain to submit order
     */
    @Override
    public HandlerResult handle(HandlerContext ctx) {
        BaseContext.setCurrentId(ctx.getUserId());

        List<ShoppingCart> cart = shoppingCartService.showShoppingCart();
        if (cart == null || cart.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.CHECKOUT)
                    .textKey("cart.empty")
                    .halts(true)
                    .build();
        }

        AddressBook query = new AddressBook();
        query.setUserId(ctx.getUserId());
        List<AddressBook> addresses = addressBookService.list(query);

        
        if (addresses == null || addresses.isEmpty()) {
            return HandlerResult.builder()
                    .intentType(IntentType.CHECKOUT)
                    .textKey("address.none.askCreate")
                    .newState(AiState.GUIDING_NEW_ADDRESS)
                    .halts(true)
                    .build();
        }

        
        String hint = ctx.getIntent().param("addressHint", "");
        AddressBook matched = matchAddressByHint(addresses, hint);
        if (matched != null) {
            HandlerResult result = HandlerResult.builder()
                    .intentType(IntentType.CHECKOUT)
                    .build();
            
            Map<String, Object> params = new HashMap<>();
            params.put("addressId", matched.getId());
            result.addFollowUp(Intent.builder()
                    .type(IntentType.SUBMIT_ORDER)
                    .params(params)
                    .rawText(ctx.getIntent().getRawText())
                    .confidence(1.0)
                    .language(ctx.getLanguage())
                    .build());
            return result;
        }

        
        
        
        
        
        
        
        
        String formattedList = addressListFormatter.formatForText(addresses, ctx.getLanguage());
        Map<String, Object> textParams = new LinkedHashMap<>();
        textParams.put("addressList", formattedList);
        String text = templates.render("address.select", ctx.getLanguage(), textParams);

        List<String> quickReplies = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            quickReplies.add(addressListFormatter.quickReplyLabel(i + 1, ctx.getLanguage()));
        }
        quickReplies.add(addressListFormatter.newAddressQuickReply(ctx.getLanguage()));

        return HandlerResult.builder()
                .intentType(IntentType.CHECKOUT)
                .rawText(text)
                .uiType("address_select")
                .quickReplies(quickReplies)
                .halts(true)
                .build();
    }

    
    private AddressBook matchAddressByHint(List<AddressBook> addresses, String hint) {
        if (hint == null || hint.isBlank()) return null;
        String h = hint.toLowerCase(Locale.ROOT);
        AddressBook match = null;
        int hits = 0;
        for (AddressBook a : addresses) {
            String haystack = (safe(a.getConsignee()) + " " + safe(a.getLabel()) + " " + safe(a.getDetail()))
                    .toLowerCase(Locale.ROOT);
            if (haystack.contains(h)) {
                match = a;
                hits++;
            }
        }
        return hits == 1 ? match : null;
    }

    private String safe(String s) { return s == null ? "" : s; }
}
