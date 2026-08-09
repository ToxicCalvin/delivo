package com.delivo.constant;

import com.delivo.context.BaseContext;

/**
 * Internationalized message constants.
 * Each message is stored as a pair (Chinese, English).
 * Call the static methods to get the message in the current language.
 */
public class MessageConstant {

    private MessageConstant() {}

    private static String msg(String zh, String en) {
        return BaseContext.isZh() ? zh : en;
    }

    public static String PASSWORD_ERROR() { return msg("密码错误", "Incorrect password"); }
    public static String ACCOUNT_NOT_FOUND() { return msg("账号不存在", "Account not found"); }
    public static String ACCOUNT_LOCKED() { return msg("账号被锁定", "Account is locked"); }
    public static String ALREADY_EXISTS() { return msg("已存在", "Already exists"); }
    public static String UNKNOWN_ERROR() { return msg("未知错误", "Unknown error"); }
    public static String USER_NOT_LOGIN() { return msg("用户未登录", "User not logged in"); }
    public static String CATEGORY_BE_RELATED_BY_SETMEAL() { return msg("当前分类关联了套餐,不能删除", "Category is linked to setmeals and cannot be deleted"); }
    public static String CATEGORY_BE_RELATED_BY_DISH() { return msg("当前分类关联了菜品,不能删除", "Category is linked to dishes and cannot be deleted"); }
    public static String SHOPPING_CART_IS_NULL() { return msg("购物车数据为空，不能下单", "Shopping cart is empty"); }
    public static String ADDRESS_BOOK_IS_NULL() { return msg("用户地址为空，不能下单", "No delivery address selected"); }
    public static String LOGIN_FAILED() { return msg("登录失败", "Login failed"); }
    public static String UPLOAD_FAILED() { return msg("文件上传失败", "File upload failed"); }
    public static String SETMEAL_ENABLE_FAILED() { return msg("套餐内包含未启售菜品，无法启售", "Setmeal contains discontinued dishes and cannot be enabled"); }
    public static String PASSWORD_EDIT_FAILED() { return msg("密码修改失败", "Password change failed"); }
    public static String DISH_ON_SALE() { return msg("起售中的菜品不能删除", "Dishes currently on sale cannot be deleted"); }
    public static String SETMEAL_ON_SALE() { return msg("起售中的套餐不能删除", "Setmeals currently on sale cannot be deleted"); }
    public static String DISH_BE_RELATED_BY_SETMEAL() { return msg("当前菜品关联了套餐,不能删除", "Dish is linked to setmeals and cannot be deleted"); }
    public static String ORDER_STATUS_ERROR() { return msg("订单状态错误", "Invalid order status"); }
    public static String ORDER_NOT_FOUND() { return msg("订单不存在", "Order not found"); }
    public static String DISH_UNAVAILABLE() { return msg("菜品已下架，无法下单：", "Dish is unavailable: "); }
    public static String SETMEAL_UNAVAILABLE() { return msg("套餐已下架，无法下单：", "Setmeal is unavailable: "); }
    public static String PERMISSION_DENIED() { return msg("权限不足，仅管理员可操作", "Permission denied, admin only"); }
    public static String ACCOUNT_ALREADY_EXISTS() { return msg("账号已存在", "Account already exists"); }
    public static String STORE_ADDRESS_PARSE_FAILED() { return msg("店铺地址解析失败", "Failed to parse store address"); }
    public static String DELIVERY_ADDRESS_PARSE_FAILED() { return msg("收货地址解析失败", "Failed to parse delivery address"); }
    public static String ROUTE_PLAN_FAILED() { return msg("配送路线规划失败", "Failed to plan delivery route"); }
    public static String OUT_OF_DELIVERY_RANGE() { return msg("超出配送范围", "Out of delivery range"); }
    public static String ORDER_ALREADY_PAID() { return msg("该订单已支付", "Order has already been paid"); }
    public static String USER_CANCEL() { return msg("用户取消", "Cancelled by user"); }
    public static String DEFAULT_ADDRESS_NOT_FOUND() { return msg("没有查询到默认地址", "No default address found"); }
}
