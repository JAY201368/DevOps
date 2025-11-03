-- 重新计算所有商品的评分，基于现有评论数据（5分制）

-- 首先将所有商品的评分设置为NULL
UPDATE `products` SET `rate` = NULL;

-- 然后基于评论数据重新计算每个商品的平均评分
UPDATE `products` p 
SET `rate` = (
    SELECT AVG(c.rating) 
    FROM `comments` c 
    WHERE c.product_id = p.id 
    AND c.status = 1
    GROUP BY c.product_id
)
WHERE EXISTS (
    SELECT 1 
    FROM `comments` c 
    WHERE c.product_id = p.id 
    AND c.status = 1
);

-- 查看更新结果
SELECT 
    p.id,
    p.title,
    p.rate,
    COUNT(c.id) as comment_count,
    AVG(c.rating) as avg_rating
FROM `products` p
LEFT JOIN `comments` c ON p.id = c.product_id AND c.status = 1
GROUP BY p.id, p.title, p.rate
ORDER BY p.title; 