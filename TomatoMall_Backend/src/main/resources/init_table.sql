-- 添加广告表
CREATE TABLE IF NOT EXISTS advertisements (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '广告id',
    title VARCHAR(50) NOT NULL COMMENT '广告标题，不允许为空',
    content VARCHAR(500) NOT NULL COMMENT '广告内容',
    image_url VARCHAR(500) NOT NULL COMMENT '广告图片url',
    product_id INT NOT NULL COMMENT '所属商品id，不允许为空',
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='广告表'; 