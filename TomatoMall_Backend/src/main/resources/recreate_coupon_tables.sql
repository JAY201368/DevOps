-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS coupon_distribution_logs;
DROP TABLE IF EXISTS user_coupons;
DROP TABLE IF EXISTS coupons;

-- 创建促销券表
CREATE TABLE coupons (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '促销券名称',
  description VARCHAR(500) DEFAULT NULL COMMENT '促销券描述',
  discount_amount DECIMAL(10,2) NOT NULL COMMENT '折扣金额',
  min_order_amount DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '最低订单金额要求',
  start_date DATETIME NOT NULL COMMENT '有效期开始时间',
  end_date DATETIME NOT NULL COMMENT '有效期结束时间',
  total_quantity INT NOT NULL COMMENT '总数量',
  remaining_quantity INT NOT NULL COMMENT '剩余数量',
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:有效,0:无效)',
  PRIMARY KEY (id),
  KEY idx_coupon_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='促销券表';

-- 创建用户促销券关联表
CREATE TABLE user_coupons (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  coupon_id BIGINT NOT NULL COMMENT '促销券ID',
  is_used TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用(1:已使用,0:未使用)',
  used_time DATETIME DEFAULT NULL COMMENT '使用时间',
  order_id INT DEFAULT NULL COMMENT '关联订单ID',
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (id),
  KEY idx_user_coupon (user_id, coupon_id),
  KEY fk_user_coupons_coupon (coupon_id),
  KEY fk_user_coupons_order (order_id),
  CONSTRAINT fk_user_coupons_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_coupons_order FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE SET NULL,
  CONSTRAINT fk_user_coupons_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户促销券关联表';

-- 创建促销券发放日志表
CREATE TABLE coupon_distribution_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  admin_id BIGINT NOT NULL COMMENT '管理员ID',
  coupon_id BIGINT NOT NULL COMMENT '促销券ID',
  distribution_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  distribution_count INT NOT NULL COMMENT '发放数量',
  distribution_condition VARCHAR(500) DEFAULT NULL COMMENT '发放条件',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_coupon_distribution_log (coupon_id),
  KEY fk_coupon_distribution_logs_admin (admin_id),
  CONSTRAINT fk_coupon_distribution_logs_admin FOREIGN KEY (admin_id) REFERENCES users (id),
  CONSTRAINT fk_coupon_distribution_logs_coupon FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='促销券发放日志表'; 