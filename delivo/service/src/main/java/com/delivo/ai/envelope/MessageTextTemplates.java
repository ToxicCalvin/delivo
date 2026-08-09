package com.delivo.ai.envelope;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class MessageTextTemplates {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)\\}");

    private final Map<String, Map<String, String>> templates = new HashMap<>();

    public MessageTextTemplates() {
        
        put("cart.empty",
                "zh", "你的购物车还是空的，先加点菜再结账吧！",
                "en", "Your cart is empty. Please add some dishes before checking out!",
                "hu", "A kosarad üres. Adj hozzá ételeket a fizetés előtt!",
                "ko", "장바구니가 비어 있어요. 메뉴를 먼저 담아주세요!");

        put("cart.added.success",
                "zh", "已为你加入 {dishName}，要继续点还是去结账？",
                "en", "Added {dishName}. Keep browsing or check out?",
                "hu", "{dishName} hozzáadva. Folytatod a böngészést vagy fizetsz?",
                "ko", "{dishName} 을(를) 담았어요. 계속 보시겠어요?");

        put("cart.added.multi",
                "zh", "已加入 {count} 件商品到购物车，要继续还是去结账？",
                "en", "Added {count} items to your cart. Keep browsing or check out?",
                "hu", "{count} tétel hozzáadva a kosaradhoz.",
                "ko", "{count} 개를 장바구니에 담았어요.");

        put("cart.added.notFound",
                "zh", "找不到 {dishName}，可以换个名字再试试吗？",
                "en", "I couldn't find {dishName}. Could you try a different name?",
                "hu", "Nem találom: {dishName}. Próbálj meg egy másik nevet!",
                "ko", "{dishName} 을(를) 찾을 수 없어요.");

        put("cart.removed.success",
                "zh", "已从购物车中移除一份 {dishName}。",
                "en", "Removed one {dishName} from your cart.",
                "hu", "Egy {dishName} eltávolítva a kosárból.",
                "ko", "{dishName} 1개를 제거했어요.");

        put("cart.cleared",
                "zh", "购物车已清空。",
                "en", "Your cart has been cleared.",
                "hu", "A kosarad kiürült.",
                "ko", "장바구니를 비웠어요.");

        
        put("address.select",
                "zh", "请选择配送地址：\n{addressList}",
                "en", "Please pick a delivery address:\n{addressList}",
                "hu", "Válassz szállítási címet:\n{addressList}",
                "ko", "배송 주소를 선택해 주세요：\n{addressList}");

        put("address.none.askCreate",
                "zh", "你还没有保存的地址，请告诉我收件人姓名、电话和详细地址。",
                "en", "You don't have any saved addresses yet. Please share recipient name, phone, and full address.",
                "hu", "Még nincs mentett címed. Kérlek, add meg a címzett nevét, telefonszámát és a pontos címet.",
                "ko", "저장된 주소가 없어요. 받는 분 이름, 전화, 주소를 알려주세요.");

        put("address.creating.missing",
                "zh", "还差一些信息：{missing}。",
                "en", "I still need: {missing}.",
                "hu", "Még szükségem van: {missing}.",
                "ko", "추가 정보가 필요해요: {missing}.");

        put("address.created",
                "zh", "地址已保存。要现在用它结账吗？",
                "en", "Address saved. Use it for checkout now?",
                "hu", "Cím elmentve. Használjuk a fizetéshez?",
                "ko", "주소를 저장했어요. 지금 결제에 사용할까요?");

        put("address.notFound",
                "zh", "找不到这个地址，请重新选择。",
                "en", "I couldn't find that address. Please pick again.",
                "hu", "Nem találom ezt a címet. Kérlek, válassz újra.",
                "ko", "그 주소를 찾을 수 없어요.");

        
        put("order.submitted",
                "zh", "订单已提交，总金额 ¥{amount}，请完成支付～",
                "en", "Order submitted. Total: ¥{amount}. Please complete the payment.",
                "hu", "Rendelés elküldve. Összesen: ¥{amount}. Kérlek, fejezd be a fizetést.",
                "ko", "주문이 접수되었어요. 총 ¥{amount}. 결제를 완료해주세요.");

        put("order.submit.failed",
                "zh", "下单失败：{reason}",
                "en", "Order failed: {reason}",
                "hu", "A rendelés sikertelen: {reason}",
                "ko", "주문 실패: {reason}");

        put("payment.confirmed",
                "zh", "支付成功！订单 #{orderNumber} 正在准备中。",
                "en", "Payment confirmed! Order #{orderNumber} is being prepared.",
                "hu", "Fizetés megerősítve! A #{orderNumber} rendelés készül.",
                "ko", "결제 확인 완료! 주문 #{orderNumber} 준비 중이에요.");

        put("payment.notReceived",
                "zh", "尚未收到订单 #{orderNumber} 的支付，请稍后再试。",
                "en", "Payment for order #{orderNumber} hasn't been received yet. Please try again.",
                "hu", "A #{orderNumber} rendelés fizetése még nem érkezett meg. Kérlek, próbáld újra.",
                "ko", "주문 #{orderNumber} 결제가 확인되지 않았어요.");

        put("payment.required.first",
                "zh", "请先完成当前订单的支付，再继续点餐。",
                "en", "Please finish paying for the pending order before ordering more.",
                "hu", "Kérlek, előbb fejezd be a függő rendelés fizetését, mielőtt újat rendelnél.",
                "ko", "대기 중인 주문 결제를 먼저 완료해주세요.");

        put("order.cancelled",
                "zh", "订单 #{orderNumber} 已取消。",
                "en", "Order #{orderNumber} has been cancelled.",
                "hu", "A #{orderNumber} rendelés törölve.",
                "ko", "주문 #{orderNumber} 을(를) 취소했어요.");

        put("order.notFound",
                "zh", "找不到这个订单，请确认订单号。",
                "en", "I couldn't find that order. Please double-check the number.",
                "hu", "Nem találom ezt a rendelést. Kérlek, ellenőrizd a rendelésszámot.",
                "ko", "그 주문을 찾을 수 없어요.");

        
        put("reorder.none",
                "zh", "你还没有历史订单，要不要先看看推荐菜？",
                "en", "You don't have any past orders yet. Want some recommendations?",
                "hu", "Még nincsenek korábbi rendeléseid. Szeretnél ajánlásokat?",
                "ko", "이전 주문이 없어요.");

        put("reorder.added",
                "zh", "已根据上次订单加入 {count} 件商品。",
                "en", "Re-added {count} items from your last order.",
                "hu", "{count} tétel újra hozzáadva az előző rendelésedből.",
                "ko", "이전 주문에서 {count} 개를 다시 담았어요.");
    }

    
    public String render(String key, String language, Map<String, Object> params) {
        Map<String, String> langMap = templates.get(key);
        if (langMap == null) return key;

        String template = langMap.getOrDefault(language, langMap.get("en"));
        if (template == null) return key;
        if (params == null || params.isEmpty()) return template;

        Matcher m = PLACEHOLDER.matcher(template);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            Object value = params.get(m.group(1));
            m.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : value.toString()));
        }
        m.appendTail(out);
        return out.toString();
    }

    private void put(String key,
                     String lang1, String text1,
                     String lang2, String text2,
                     String lang3, String text3,
                     String lang4, String text4) {
        Map<String, String> langMap = new HashMap<>();
        langMap.put(lang1, text1);
        langMap.put(lang2, text2);
        langMap.put(lang3, text3);
        langMap.put(lang4, text4);
        templates.put(key, langMap);
    }
}
