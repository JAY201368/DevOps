-- 将所有标题不等于"兄弟"的商品的评分设置为NULL
UPDATE `products` 
SET `rate` = NULL 
WHERE `title` != '兄弟';

-- 查看更新结果
SELECT `id`, `title`, `rate` 
FROM `products` 
ORDER BY `title`; 