package com.xiaozhi.constant;

public class MessageConstant {
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String WECHAT_LOGIN_FAIL = "微信登录失败：";
    public static final String WECHAT_LOGIN_NO_OPENID = "微信登录失败：未获取到openid";

    public static final String PRODUCT_NOT_FOUND_OR_OFFLINE = "商品不存在或已下架";
    public static final String PRODUCT_OFFLINE_TEMPLATE = "商品[%s]已下架";
    public static final String PRODUCT_NOT_FOUND = "商品不存在";

    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String CATEGORY_HAS_PRODUCTS = "该分类下存在商品，无法删除";

    public static final String CART_EMPTY = "购物车为空";
    public static final String CART_PRODUCT_NOT_FOUND = "购物车中无此商品";

    public static final String ADDRESS_NOT_FOUND = "地址不存在";

    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String ORDER_STATUS_ERROR = "订单状态异常";
    public static final String ORDER_CANNOT_CANCEL = "当前订单状态不可取消";
    public static final String ORDER_TIMEOUT_CANCEL = "超时未付款，系统自动取消";
    public static final String ORDER_ITEMS_EMPTY = "下单商品不能为空";

    public static final String ADMIN_LOGIN_FAIL = "用户名或密码错误";
}
