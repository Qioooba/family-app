-- ==========================================
-- ScanController 相关数据库表
-- ==========================================

-- 条码商品库
CREATE TABLE IF NOT EXISTS barcode_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    barcode VARCHAR(32) NOT NULL COMMENT '条形码',
    product_name VARCHAR(200) COMMENT '商品名称',
    brand VARCHAR(100) COMMENT '品牌',
    specification VARCHAR(100) COMMENT '规格',
    category VARCHAR(50) COMMENT '类别',
    reference_price DECIMAL(10,2) COMMENT '参考价格',
    shelf_life_days INT COMMENT '保质期(天)',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    source VARCHAR(20) DEFAULT 'local' COMMENT '数据来源: local-本地库, api-第三方API',
    query_count INT DEFAULT 1 COMMENT '查询次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_barcode (barcode),
    UNIQUE KEY uk_barcode (barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='条码商品库';

-- 扫码识别记录
CREATE TABLE IF NOT EXISTS scan_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    family_id BIGINT COMMENT '家庭ID',
    scan_type VARCHAR(20) COMMENT '扫码类型: barcode-条形码, qrcode-二维码, image-图片识别',
    scan_content VARCHAR(500) COMMENT '扫码内容(条形码/二维码内容或图片Base64摘要)',
    result_json TEXT COMMENT '识别结果JSON',
    status TINYINT DEFAULT 0 COMMENT '识别状态: 1成功 0失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    product_name VARCHAR(200) COMMENT '商品名称(识别成功时)',
    added_to_inventory TINYINT DEFAULT 0 COMMENT '是否已添加到库存: 0-否, 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
    INDEX idx_user_id (user_id),
    INDEX idx_family_id (family_id),
    INDEX idx_scan_type (scan_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='扫码识别记录';
