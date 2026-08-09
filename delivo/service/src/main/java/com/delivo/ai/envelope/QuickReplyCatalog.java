package com.delivo.ai.envelope;

import com.delivo.ai.intent.IntentType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Component
public class QuickReplyCatalog {

    
    public List<String> defaultsFor(IntentType type, String language) {
        boolean zh = "zh".equals(language);
        boolean hu = "hu".equals(language);
        boolean ko = "ko".equals(language);

        switch (type) {
            case RECOMMEND_DISHES:
                if (zh) return Arrays.asList("加入购物车", "看看别的", "去结账");
                if (hu) return Arrays.asList("Kosárba", "Mutass mást", "Fizetés");
                if (ko) return Arrays.asList("장바구니 담기", "다른 추천", "결제하기");
                return Arrays.asList("Add to cart", "Show me more", "Checkout");

            case ADD_TO_CART:
                if (zh) return Arrays.asList("继续点餐", "去结账");
                if (hu) return Arrays.asList("Tovább rendelek", "Fizetés");
                if (ko) return Arrays.asList("계속 주문", "결제하기");
                return Arrays.asList("Order more", "Checkout");

            case VIEW_CART:
                if (zh) return Arrays.asList("去结账", "继续点餐", "清空购物车");
                if (hu) return Arrays.asList("Fizetés", "Tovább rendelek", "Kosár ürítése");
                if (ko) return Arrays.asList("결제하기", "계속 주문", "장바구니 비우기");
                return Arrays.asList("Checkout", "Order more", "Clear cart");

            case CHECKOUT:
            case PROVIDE_ADDRESS:
                
                if (zh) return Collections.singletonList("查看购物车");
                if (hu) return Collections.singletonList("Kosár megtekintése");
                if (ko) return Collections.singletonList("장바구니 보기");
                return Collections.singletonList("View cart");

            case SUBMIT_ORDER:
                if (zh) return Arrays.asList("我已支付", "查看订单");
                if (hu) return Arrays.asList("Fizettem", "Rendelés megtekintése");
                if (ko) return Arrays.asList("결제 완료", "주문 보기");
                return Arrays.asList("I've paid", "View order");

            case CONFIRM_PAYMENT:
                if (zh) return Arrays.asList("查看订单状态", "再点一份");
                if (hu) return Arrays.asList("Rendelés állapota", "Újra rendelés");
                if (ko) return Arrays.asList("주문 상태 보기", "다시 주문");
                return Arrays.asList("Check order status", "Order again");

            case ORDER_STATUS:
                if (zh) return Arrays.asList("再点一份", "推荐菜品");
                if (hu) return Arrays.asList("Újra rendelés", "Ajánlatok");
                if (ko) return Arrays.asList("다시 주문", "추천 메뉴");
                return Arrays.asList("Order again", "Recommend dishes");

            case CANCEL_ORDER:
                if (zh) return Arrays.asList("推荐菜品", "重新点餐");
                if (hu) return Arrays.asList("Ajánlatok", "Új rendelés");
                if (ko) return Arrays.asList("추천 메뉴", "다시 주문");
                return Arrays.asList("Recommend dishes", "Start over");

            case REORDER:
                if (zh) return Arrays.asList("去结账", "继续添加");
                if (hu) return Arrays.asList("Fizetés", "Továbbiak hozzáadása");
                if (ko) return Arrays.asList("결제하기", "더 담기");
                return Arrays.asList("Checkout", "Add more");

            case CREATE_ADDRESS:
                if (zh) return Collections.singletonList("查看购物车");
                if (hu) return Collections.singletonList("Kosár megtekintése");
                if (ko) return Collections.singletonList("장바구니 보기");
                return Collections.singletonList("View cart");

            case SMALL_TALK:
            case CLARIFY:
            case UNKNOWN:
            default:
                if (zh) return Arrays.asList("推荐菜品", "查看购物车");
                if (hu) return Arrays.asList("Ajánlatok", "Kosár megtekintése");
                if (ko) return Arrays.asList("추천 메뉴", "장바구니 보기");
                return Arrays.asList("Recommend dishes", "View cart");
        }
    }
}
