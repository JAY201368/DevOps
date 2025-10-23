-- 添加广告表
CREATE TABLE IF NOT EXISTS advertisements (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '广告id',
    title VARCHAR(50) NOT NULL COMMENT '广告标题，不允许为空',
    content VARCHAR(500) NOT NULL COMMENT '广告内容',
    image_url VARCHAR(500) NOT NULL COMMENT '广告图片url',
    product_id INT NOT NULL COMMENT '所属商品id，不允许为空',
    FOREIGN KEY (product_id) REFERENCES products(id)
) COMMENT='广告表';

-- 添加愿望单表
CREATE TABLE IF NOT EXISTS wish_list_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '愿望单项id',
    user_id BIGINT NOT NULL COMMENT '用户id',
    book_id BIGINT NOT NULL COMMENT '图书id',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES products(id),
    UNIQUE KEY uk_user_book (user_id, book_id) COMMENT '确保同一用户不会重复添加同一本书'
) COMMENT='愿望单表';