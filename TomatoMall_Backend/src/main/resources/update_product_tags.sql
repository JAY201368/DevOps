-- 更新产品标签
-- 为所有没有标签的产品添加默认标签
UPDATE products SET tags = '文学,小说' WHERE tags IS NULL OR tags = '';

-- 清理标签格式（去除多余空格）
UPDATE products SET tags = TRIM(tags) WHERE tags IS NOT NULL;

-- 更新特定类型的产品标签
UPDATE products SET tags = '文学,小说,经典' WHERE title LIKE '%红楼梦%' OR title LIKE '%三国演义%' OR title LIKE '%水浒传%' OR title LIKE '%西游记%';
UPDATE products SET tags = '科技,计算机,编程' WHERE title LIKE '%编程%' OR title LIKE '%计算机%' OR title LIKE '%Python%' OR title LIKE '%Java%';
UPDATE products SET tags = '经济,管理,商业' WHERE title LIKE '%经济%' OR title LIKE '%管理%' OR title LIKE '%商业%';
UPDATE products SET tags = '历史,文化' WHERE title LIKE '%历史%' OR title LIKE '%文化%';
UPDATE products SET tags = '科学,科普' WHERE title LIKE '%科学%' OR title LIKE '%物理%' OR title LIKE '%化学%' OR title LIKE '%生物%';
UPDATE products SET tags = '艺术,设计' WHERE title LIKE '%艺术%' OR title LIKE '%设计%' OR title LIKE '%音乐%';
UPDATE products SET tags = '教育,学习,教材' WHERE title LIKE '%教育%' OR title LIKE '%学习%' OR title LIKE '%教材%';
UPDATE products SET tags = '生活,健康' WHERE title LIKE '%生活%' OR title LIKE '%健康%' OR title LIKE '%烹饪%';
UPDATE products SET tags = '哲学,思想' WHERE title LIKE '%哲学%' OR title LIKE '%思想%';
UPDATE products SET tags = '小说,文学' WHERE title LIKE '%小说%';
UPDATE products SET tags = '文学,诗歌' WHERE title LIKE '%诗%' OR title LIKE '%诗集%';
UPDATE products SET tags = '文学,散文' WHERE title LIKE '%散文%' OR title LIKE '%随笔%';
UPDATE products SET tags = '小说,科幻' WHERE title LIKE '%科幻%';
UPDATE products SET tags = '小说,悬疑' WHERE title LIKE '%悬疑%' OR title LIKE '%推理%';
UPDATE products SET tags = '小说,言情' WHERE title LIKE '%言情%' OR title LIKE '%爱情%';
UPDATE products SET tags = '小说,武侠' WHERE title LIKE '%武侠%';
UPDATE products SET tags = '小说,奇幻' WHERE title LIKE '%奇幻%' OR title LIKE '%魔幻%';

-- 确保所有产品都有标签
UPDATE products SET tags = '其他' WHERE tags IS NULL OR tags = '';

-- 查看更新后的结果
SELECT id, title, tags FROM products; 