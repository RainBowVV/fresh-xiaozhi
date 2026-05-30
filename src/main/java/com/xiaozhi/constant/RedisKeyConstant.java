package com.xiaozhi.constant;

import java.util.concurrent.TimeUnit;

public class RedisKeyConstant {

    // ==================== 缓存过期时间 ====================
    /** 商品分类缓存过期时间：30 分钟 */
    public static final long CATEGORY_CACHE_TTL = 30;
    /** 商品缓存过期时间：30 分钟 */
    public static final long PRODUCT_CACHE_TTL = 30;
    /** 缓存过期时间单位 */
    public static final TimeUnit CACHE_TTL_UNIT = TimeUnit.MINUTES;
    /** 购物车 Hash Key: cart:{userId} */
    public static final String CART_KEY_PREFIX = "cart:";
    /** 订单超时 Sorted Set Key，score 为过期时间戳，member 为 orderId */
    public static final String ORDER_TIMEOUT_KEY = "order:timeout";
    /** 商品详情缓存 Key: product:detail:{productId} */
    public static final String PRODUCT_DETAIL_KEY = "product:detail:";
    /** 商品列表缓存 Key: product:list:{categoryId}，categoryId=0 表示全部 */
    public static final String PRODUCT_LIST_KEY = "product:list:";
    /** 分类列表缓存 Key */
    public static final String CATEGORY_LIST_KEY = "category:list";
}
