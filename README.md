# 金融市场数据库  
目前只完成database部分（修改过的部分要在README文件里写出来，就写在这一行）
## Database

### player
字段 | 说明
---- | ----
player_id | 主键，自增，玩家唯一 ID
player_name | 玩家名称
register_time | 注册时间

---

### item
字段 | 说明
---- | ----
item_id | 主键，自增，物品唯一 ID
item_name | 物品名称
rarity | 稀有度（common / rare / legendary）
description | 物品描述

---

### inventory
（玩家持有的游戏物品）

字段 | 说明
---- | ----
inventory_id | 主键，自增
player_id | 外键 → player.player_id
item_id | 外键 → item.item_id
quantity | 玩家持有数量

---

### market_order
（玩家挂单：包含 BUY / SELL）

字段 | 说明
---- | ----
order_id | 主键，自增
player_id | 外键 → player.player_id
item_id | 外键 → item.item_id
order_type | BUY / SELL
price | 单价
quantity | 下单数量
status | PENDING / PARTIAL / FILLED / CANCELLED
create_time | 创建时间

---

### trade_record
（订单撮合后的交易记录）

字段 | 说明
---- | ----
trade_id | 主键，自增
buy_order_id | 外键 → market_order.order_id
sell_order_id | 外键 → market_order.order_id
item_id | 外键 → item.item_id
price | 成交价格
quantity | 成交数量
trade_time | 成交时间

---

### price_history
（价格走势 / K 线数据）

字段 | 说明
---- | ----
history_id | 主键，自增
item_id | 外键 → item.item_id
price | 收盘价或最新成交价
record_time | 记录时间

