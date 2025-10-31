-- 检查促销券相关表格是否存在
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'tomato' 
AND table_name LIKE '%coupon%'; 