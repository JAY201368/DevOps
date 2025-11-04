-- 修改products表的rate字段，允许为空且默认为空
ALTER TABLE `products` 
	CHANGE `rate` `rate` double DEFAULT NULL;

-- 可选：将现有的0评分设置为NULL（如果需要的话）
-- UPDATE `products` SET `rate` = NULL WHERE `rate` = 0; 