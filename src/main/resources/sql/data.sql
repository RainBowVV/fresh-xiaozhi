USE fresh_xiaozhi;

-- 初始商品分类
INSERT INTO `category` (`name`, `sort`) VALUES
('新鲜蔬菜', 1),
('时令水果', 2),
('肉禽蛋类', 3),
('海鲜水产', 4),
('粮油调味', 5),
('乳品饮料', 6);

-- 初始测试商品
INSERT INTO `product` (`category_id`, `name`, `image`, `price`, `unit`, `description`, `status`) VALUES
-- 新鲜蔬菜
(1, '有机小白菜', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/xiaobaicai.jpg', 3.50, '斤', '当日采摘，新鲜直达', 1),
(1, '紫皮茄子', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/qiezi.jpg', 4.00, '斤', '个大饱满，口感软糯', 1),
(1, '西红柿', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/xihongshi.jpg', 5.00, '斤', '自然熟透，酸甜多汁', 1),
(1, '黄瓜', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/huanggua.jpg', 3.80, '斤', '清脆爽口，凉拌必备', 1),
(1, '土豆', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/tudou.jpg', 2.50, '斤', '粉糯香甜，炖煮皆宜', 1),
(1, '青椒', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/qingjiao.jpg', 4.50, '斤', '色泽翠绿，微辣鲜香', 1),
(1, '胡萝卜', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/huluobo.jpg', 3.00, '斤', '甜脆多汁，营养丰富', 1),
-- 时令水果
(2, '海南芒果', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/mangguo.jpg', 12.80, '斤', '香甜细腻，果肉厚实', 1),
(2, '红心火龙果', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/huolongguo.jpg', 8.50, '个', '甜度高，口感好', 1),
(2, '山东苹果', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/pingguo.jpg', 6.80, '斤', '脆甜多汁，果香浓郁', 1),
(2, '麒麟西瓜', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/xigua.jpg', 3.50, '斤', '沙瓤多汁，消暑解渴', 1),
(2, '阳光玫瑰葡萄', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/putao.jpg', 18.00, '斤', '无籽脆甜，玫瑰香气', 1),
(2, '海南香蕉', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/xiangjiao.jpg', 4.50, '斤', '软糯香甜，老少皆宜', 1),
-- 肉禽蛋类
(3, '黑猪五花肉', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/wuhuarou.jpg', 28.00, '斤', '肥瘦相间，炖炒皆宜', 1),
(3, '散养土鸡蛋', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/jidan.jpg', 15.00, '盒(10枚)', '农家散养，蛋黄饱满', 1),
(3, '新鲜鸡胸肉', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/jixiong.jpg', 12.00, '斤', '低脂高蛋白，健身首选', 1),
(3, '牛腱子肉', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/jianzirou.jpg', 42.00, '斤', '肉质紧实，卤炖皆宜', 1),
(3, '新鲜排骨', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/paigu.jpg', 32.00, '斤', '肉嫩骨香，炖汤首选', 1),
(3, '鸡翅中', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/jichizhong.jpg', 18.00, '斤', '皮脆肉嫩，烤炸皆宜', 1),
-- 海鲜水产
(4, '冰鲜大虾', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/xia.jpg', 35.00, '斤', '个头均匀，肉质紧实', 1),
(4, '鲈鱼', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/luyu.jpg', 22.00, '条', '肉质细嫩，清蒸最佳', 1),
(4, '梭子蟹', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/suozixie.jpg', 45.00, '斤', '膏肥黄满，鲜美无比', 1),
(4, '鱿鱼', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/youyu.jpg', 18.00, '斤', '新鲜弹牙，铁板烧必备', 1),
(4, '生蚝', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/shenghao.jpg', 8.00, '个', '肥美多汁，蒜蓉烤制', 1),
-- 粮油调味
(5, '东北大米', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/dami.jpg', 29.90, '袋(5kg)', '颗粒饱满，饭香浓郁', 1),
(5, '花生油', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/huashengyou.jpg', 68.00, '桶(5L)', '物理压榨，香浓纯正', 1),
(5, '生抽酱油', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/shengchou.jpg', 12.50, '瓶', '酿造酱油，鲜味十足', 1),
(5, '陈醋', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/chencu.jpg', 8.80, '瓶', '山西老陈醋，酸香醇厚', 1),
(5, '挂面', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/guamian.jpg', 6.50, '袋(500g)', '细滑爽口，久煮不糊', 1),
-- 乳品饮料
(6, '鲜牛奶', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/niunai.jpg', 6.50, '瓶(250ml)', '巴氏杀菌，新鲜营养', 1),
(6, '原味酸奶', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/suannai.jpg', 8.80, '瓶', '醇厚顺滑，益生菌发酵', 1),
(6, '鲜榨橙汁', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/chengzhi.jpg', 12.00, '瓶', '100%纯果汁，维C满满', 1),
(6, '豆浆', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/doujiang.jpg', 4.50, '杯', '现磨豆浆，香浓丝滑', 1),
(6, '椰子水', 'https://fresh-xiaozhi.oss-cn-beijing.aliyuncs.com/images/yezishui.jpg', 9.90, '瓶', '天然椰子水，清甜解渴', 1);
