# Game Market Project (金融市场数据库项目)
## 待修改
1.市场的界面修改(g)  
2.交易大厅的api调用bug & 背包的api调用(y)      
3.交易的api调用的算法(w)  

这是一个基于 Spring Boot 和 Vue 3 的游戏资产交易市场项目。实现了前后端分离架构，包含完整的数据库设计、后端 API 和前端交互界面。

## 🛠 技术栈 (Tech Stack)

- **Frontend**: Vue 3, Vite, Vue Router, Axios
- **Backend**: Java 17, Spring Boot 3.3.5, Spring Data JPA
- **Database**: PostgreSQL 16
- **Build Tools**: Maven (Backend), NPM (Frontend)

## 📂 项目结构 (Project Structure)

```
Database-Project/
├── backend/                 # 后端项目目录 (Spring Boot)
│   ├── src/main/java/       # Java 源代码
│   ├── src/main/resources/  # 配置文件 (application.properties)
│   └── pom.xml              # Maven 依赖配置
├── src/                     # 前端源代码 (Vue)
│   ├── api/                 # API 接口封装
│   ├── components/          # Vue 组件
│   ├── views/               # 页面视图
│   └── router/              # 路由配置
├── database/                # 数据库脚本
├── vite.config.js           # Vite 配置 (包含跨域代理)
└── package.json             # 前端依赖配置
```

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
- **JDK 21** (本项目使用 Java 21)
- **Node.js 18+**
- **PostgreSQL 16**

### 2. 启动数据库
确保 PostgreSQL 服务已启动，并创建了名为 `market` 的数据库和用户。

**如果你使用 Docker:**
```bash
docker run --name market-postgres -e POSTGRES_PASSWORD=market -e POSTGRES_USER=market -e POSTGRES_DB=market -p 5432:5432 -d postgres:16
```

**初始化数据库表结构:**
项目包含一个 `database/market.sql` 文件，用于创建所需的数据库表（Schema）。
如果你的数据库是空的，请运行以下命令来初始化表结构：
```bash
psql -h localhost -U market -d market -f database/market.sql
```
*注意：测试数据（用户、物品等）将由后端应用启动时的 `DataLoader` 自动填充，无需手动插入。*

### 3. 启动后端 (Backend)
打开一个新的终端窗口，运行：
```bash
cd backend
mvn spring-boot:run
```
- 后端服务启动在: `http://localhost:8080`
- API 文档/测试: 可以使用 Postman 或 curl 测试 `/api/auth/login` 等接口。

### 4. 启动前端 (Frontend)
打开另一个终端窗口，运行：
```bash
# 安装依赖 (仅第一次需要)
npm install

# 启动开发服务器
npm run dev
```
- 前端页面访问地址: `http://localhost:5173`

### 5. 访问应用
打开浏览器访问 `http://localhost:5173`。
- **测试账号**: `testuser` / `password` (如果 DataLoader 已运行)
- **注册**: 你也可以在注册页面创建一个新账号。

## 💾 数据库设计 (Database Schema)

### Player (玩家)
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| player_id | Integer | 主键，自增 |
| player_name | String | 玩家名称 |
| level | Integer | 等级 |
| register_time | DateTime | 注册时间 |

### Asset (资产/物品)
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| asset_id | Integer | 主键，自增 |
| asset_name | String | 物品名称 |
| asset_type | String | 类型 (Weapon, Armor, etc.) |
| base_price | Decimal | 基础价格 |

### Wallet (钱包)
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| wallet_id | Integer | 主键，自增 |
| player_id | Integer | 外键 -> Player |
| balance | Decimal | 余额 |

### MarketOrder (市场挂单)
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| order_id | Integer | 主键，自增 |
| player_id | Integer | 外键 -> Player |
| asset_id | Integer | 外键 -> Asset |
| order_type | String | BUY / SELL |
| price | Decimal | 挂单价格 |
| quantity | Integer | 数量 |
| status | String | OPEN / FILLED / CANCELLED |

### TradeHistory (交易历史)
| 字段 | 类型 | 说明 |
| ---- | ---- | ---- |
| trade_id | Integer | 主键，自增 |
| buyer_id | Integer | 买家 ID |
| seller_id | Integer | 卖家 ID |
| asset_id | Integer | 资产 ID |
| price | Decimal | 成交价格 |
| quantity | Integer | 成交数量 |
| trade_time | DateTime | 交易时间 |

## 🔌 API 接口 (API Endpoints)

### Market
- `GET /api/market/listings`: 获取市场挂单列表
- `GET /api/market/categories`: 获取物品分类

### Trade
- `POST /api/trade/orders`: 创建买单/卖单
- `GET /api/trade/orders`: 获取用户的交易记录

### User
- `GET /api/user/profile/{id}`: 获取用户个人信息
- `GET /api/user/inventory/{id}`: 获取用户背包物品

### Auth
- `POST /api/auth/login`: 用户登录 (Mock)
- `POST /api/auth/register`: 用户注册 (Mock)

## ✨ 已实现功能
1. **全栈架构搭建**：Spring Boot 后端 + Vue3 前端。
2. **数据库集成**：使用 JPA 自动建表，Docker 容器化数据库。
3. **数据初始化**：系统启动时自动预加载测试用户、物品和市场挂单。
4. **市场浏览**：前端展示热门商品和涨幅榜。
5. **个人中心**：展示用户信息和交易历史。
6. **背包系统**：查看用户持有的资产。

## 魏 11/23日更改（后端）
1. **取消订单**：新增取消订单功能。
2. **支持冻结/预留**：在进行交易时下买单需冻结资金或验证卖方持有物品。
3. **修改pom.xml**：使代码可以用本地psql链接数据库，不必使用Docker。
```bash
# 1.用管理员账号 postgres 登录 psql
# 2.直接省略自定义排序规则，仅指定 UTF-8 编码和 template0 模板
CREATE DATABASE market WITH ENCODING 'UTF8' TEMPLATE template0;
# 3.创建 market 用户（密码 market）
CREATE USER market WITH PASSWORD 'market';

# 4.给用户授权 market 数据库所有权限
GRANT ALL PRIVILEGES ON DATABASE market TO market;

# 5.切换到 market 数据库，授权 schema 权限（避免创建表失败）
\c market; .
GRANT ALL PRIVILEGES ON SCHEMA public TO market;
```
4. **充值功能**：支持用户充值