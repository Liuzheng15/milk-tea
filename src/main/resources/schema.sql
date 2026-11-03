-- 创建数据库
CREATE DATABASE IF NOT EXISTS milktea DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE milktea;

-- 饮品分类表
DROP TABLE IF EXISTS category;
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '饮品分类';

-- 饮品表
DROP TABLE IF EXISTS drink;
CREATE TABLE drink (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '饮品名称',
    category_id BIGINT COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    image VARCHAR(500) COMMENT '图片URL',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '1上架 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '饮品';

-- 会员表
DROP TABLE IF EXISTS member;
CREATE TABLE member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT '手机号',
    name VARCHAR(50) COMMENT '姓名',
    points INT DEFAULT 0 COMMENT '积分余额',
    level TINYINT DEFAULT 1 COMMENT '等级 1普通 2银卡 3金卡',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '会员';

-- 订单表
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
    member_id BIGINT COMMENT '会员ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    coupon_id BIGINT COMMENT '使用的优惠券ID',
    points_earned INT COMMENT '本单获得积分',
    status TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2制作中 3已完成 4已取消 5已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    refund_time DATETIME COMMENT '退款时间'
) COMMENT '订单';

-- 订单明细表
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    drink_id BIGINT NOT NULL COMMENT '饮品ID',
    drink_name VARCHAR(100) COMMENT '饮品名称',
    quantity INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计'
) COMMENT '订单明细';

-- 库存表
DROP TABLE IF EXISTS inventory;
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '原料名称',
    quantity DECIMAL(10,2) DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20) COMMENT '单位',
    warning_threshold INT DEFAULT 10 COMMENT '预警阈值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '库存';

-- 积分明细表
DROP TABLE IF EXISTS points_log;
CREATE TABLE points_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    points INT NOT NULL COMMENT '积分变动(正增加 负扣减)',
    type TINYINT NOT NULL COMMENT '1消费获得 2兑换消耗 3退款扣回',
    order_id BIGINT COMMENT '关联订单',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '积分明细';

-- 优惠券表
DROP TABLE IF EXISTS coupon;
CREATE TABLE coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT NOT NULL COMMENT '1满减 2折扣 3立减',
    value DECIMAL(10,2) NOT NULL COMMENT '满减金额/折扣率(0-1)/立减金额',
    min_amount DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
    start_time DATETIME COMMENT '生效开始时间',
    end_time DATETIME COMMENT '生效结束时间',
    status TINYINT DEFAULT 1 COMMENT '1可用 0不可用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '优惠券';

-- 饮品原料映射表
DROP TABLE IF EXISTS drink_ingredient;
CREATE TABLE drink_ingredient (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drink_id BIGINT NOT NULL COMMENT '饮品ID',
    inventory_id BIGINT NOT NULL COMMENT '库存原料ID',
    quantity DECIMAL(10,2) NOT NULL COMMENT '每份饮品消耗原料数量'
) COMMENT '饮品原料映射';

-- 初始数据：分类
INSERT INTO category (name, sort) VALUES
('招牌奶茶', 1),
('鲜果茶', 2),
('咖啡', 3);

-- 初始数据：饮品
INSERT INTO drink (name, category_id, price, description, status) VALUES
('珍珠奶茶', 1, 12.00, '经典珍珠奶茶', 1),
('芋泥啵啵奶茶', 1, 16.00, '香芋泥+啵啵', 1),
('满杯百香果', 2, 15.00, '鲜百香果果茶', 1),
('芝士葡萄', 2, 18.00, '葡萄+芝士奶盖', 1),
('美式咖啡', 3, 14.00, '经典美式', 1),
('拿铁', 3, 18.00, '丝绒拿铁', 1);

-- 初始数据：会员
INSERT INTO member (phone, name, points, level) VALUES
('13800138000', '张三', 2500, 3),
('13900139000', '李四', 600, 2);

-- 初始数据：库存
INSERT INTO inventory (name, quantity, unit, warning_threshold) VALUES
('珍珠', 50, '份', 10),
('牛奶', 30, '升', 5),
('咖啡豆', 10, '千克', 8);

-- 初始数据：优惠券
INSERT INTO coupon (name, type, value, min_amount, status) VALUES
('满30减5', 1, 5.00, 30.00, 1),
('8折券', 2, 0.80, 0.00, 1),
('立减3元', 3, 3.00, 0.00, 1);

-- 初始数据：饮品原料映射
-- 珍珠奶茶(1): 珍珠1份 + 牛奶0.2升
-- 芋泥啵啵奶茶(2): 珍珠1份 + 牛奶0.3升
-- 满杯百香果(3): 无库存原料
-- 芝士葡萄(4): 无库存原料
-- 美式咖啡(5): 咖啡豆0.03千克
-- 拿铁(6): 咖啡豆0.03千克 + 牛奶0.2升
INSERT INTO drink_ingredient (drink_id, inventory_id, quantity) VALUES
(1, 1, 1.00),
(1, 2, 0.20),
(2, 1, 1.00),
(2, 2, 0.30),
(5, 3, 0.03),
(6, 3, 0.03),
(6, 2, 0.20);
