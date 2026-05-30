package com.xiaozhi.constant;

public class OrderConstant {
    /** 订单超时时间（分钟） */
    public static final int TIMEOUT_MINUTES = 30;

    /** 待付款 */
    public static final int STATUS_PENDING_PAYMENT = 0;
    /** 已付款 */
    public static final int STATUS_PAID = 1;
    /** 备货中 */
    public static final int STATUS_PREPARING = 2;
    /** 配送中 */
    public static final int STATUS_DELIVERING = 3;
    /** 已完成 */
    public static final int STATUS_COMPLETED = 4;
    /** 已取消 */
    public static final int STATUS_CANCELLED = 5;
}
