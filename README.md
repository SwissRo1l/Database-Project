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

## Frontend

本项目包含一个基于 Vue 3 + Vite 的前端应用，用于展示游戏金融市场的数据和交互。

### 技术栈
- **Vue 3**: 渐进式 JavaScript 框架
- **Vite**: 下一代前端构建工具
- **Vue Router**: Vue.js 的官方路由
- **Pinia**: Vue 的专属状态管理库
- **Axios**: 基于 promise 的网络请求库
- **ECharts**: 一个基于 JavaScript 的开源可视化图表库

### 项目结构
前端代码位于项目根目录下：
- `src/`: 源代码目录
  - `api/`: API 请求模块
  - `assets/`: 静态资源（CSS, 图片等）
  - `components/`: 公共组件
  - `router/`: 路由配置
  - `store/`: Pinia 状态管理
  - `utils/`: 工具函数
  - `views/`: 页面视图
- `public/`: 公共静态文件
- `vite.config.js`: Vite 配置文件

### 功能页面
- **市场 (Market)**: 浏览所有商品，查看市场行情。
- **商品详情 (ItemDetail)**: 查看特定商品的详细信息和价格走势。
- **交易 (Trade)**: 进行买入或卖出操作。
- **背包 (Inventory)**: 查看玩家持有的物品。
- **个人中心 (Profile)**: 管理个人信息。
- **登录/注册 (Login/Register)**: 用户认证。

### 快速开始

1. **安装依赖**
   ```bash
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```
   启动后访问: `http://localhost:5173`

3. **构建生产版本**
   ```bash
   npm run build
   ```
