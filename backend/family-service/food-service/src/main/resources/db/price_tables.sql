-- ==========================================
-- PriceController 相关数据库表
-- ==========================================

-- 价格记录表
CREATE TABLE IF NOT EXISTS price_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    barcode VARCHAR(32) COMMENT '条形码',
    product_name VARCHAR(200) COMMENT '商品名称',
    brand VARCHAR(100) COMMENT '品牌',
    specification VARCHAR(100) COMMENT '规格',
    store_id BIGINT COMMENT '商店ID',
    store_name VARCHAR(100) COMMENT '商店名称',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    promotion_price DECIMAL(10,2) COMMENT '促销价格',
    promotion_info VARCHAR(200) COMMENT '促销信息',
    stock_status VARCHAR(20) COMMENT '库存状态: in_stock-有货, out_of_stock-缺货, limited-限量',
    price_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '价格记录时间',
    latitude DECIMAL(10,6) COMMENT '纬度',
    longitude DECIMAL(10,6) COMMENT '经度',
    submitter_id VARCHAR(64) COMMENT '提交者ID',
    verify_status TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-已通过, 2-已拒绝',
    source TINYINT DEFAULT 1 COMMENT '来源: 1-用户提交, 2-爬虫采集',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_barcode (barcode),
    INDEX idx_store_id (store_id),
    INDEX idx_price_date (price_date),
    INDEX idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品价格记录';

-- 价格提醒表
CREATE TABLE IF NOT EXISTS price_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    barcode VARCHAR(32) COMMENT '条形码',
    product_name VARCHAR(200) COMMENT '商品名称',
    target_price DECIMAL(10,2) NOT NULL COMMENT '目标价格',
    alert_type VARCHAR(20) DEFAULT 'below' COMMENT '提醒类型: below-低于目标, above-高于目标',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-关闭, 1-开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_user_id (user_id),
    INDEX idx_barcode (barcode),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格提醒';

-- 商店信息表
CREATE TABLE IF NOT EXISTS store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    store_name VARCHAR(100) NOT NULL COMMENT '商店名称',
    address VARCHAR(200) COMMENT '地址',
    phone VARCHAR(20) COMMENT '联系电话',
    latitude DECIMAL(10,6) COMMENT '纬度',
    longitude DECIMAL(10,6) COMMENT '经度',
    category VARCHAR(50) COMMENT '类别: supermarket-超市, market-菜市场, convenience-便利店',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-关闭, 1-营业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商店信息';
