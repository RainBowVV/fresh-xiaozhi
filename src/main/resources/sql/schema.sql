CREATE DATABASE IF NOT EXISTS fresh_xiaozhi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE fresh_xiaozhi;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `openid` VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid',
    `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
    `avatar_url` VARCHAR(256) DEFAULT '' COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort` INT DEFAULT 0 COMMENT '排序权重',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `image` VARCHAR(256) DEFAULT '' COMMENT '商品图片URL',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价（元）',
    `unit` VARCHAR(16) DEFAULT '' COMMENT '单位',
    `description` VARCHAR(512) DEFAULT '' COMMENT '商品描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0下架 1上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `province` VARCHAR(32) DEFAULT '' COMMENT '省',
    `city` VARCHAR(32) DEFAULT '' COMMENT '市',
    `district` VARCHAR(32) DEFAULT '' COMMENT '区',
    `detail` VARCHAR(256) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0否 1是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `address_id` BIGINT NOT NULL COMMENT '收货地址ID',
    `address_snapshot` VARCHAR(512) DEFAULT '' COMMENT '地址快照JSON',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待付款 1已付款 2备货中 3配送中 4已完成 5已取消',
    `remark` VARCHAR(256) DEFAULT '' COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称快照',
    `product_image` VARCHAR(256) DEFAULT '' COMMENT '商品图片快照',
    `price` DECIMAL(10,2) NOT NULL COMMENT '下单时单价快照',
    `quantity` INT NOT NULL COMMENT '数量',
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
