# Game Market Project (游戏资产交易市场)
## 待修改
1.两个账户余额刷新不同步  
2.背包显示问题  
3.账户购买记录（顺序，买卖（买红色，卖绿色），加减钱）  
4.买卖订单会有一个小提示  
5.商品的图片和账户图片  

这是一个基于 Spring Boot 和 Vue 3 的全栈游戏资产交易平台。项目模拟了一个真实的游戏内市场，支持玩家之间的物品买卖、价格走势分析（K线图）、资产管理以及钱包充值等功能。

## ✨ 主要功能 (Features)

- **市场大厅 (Market)**: 浏览所有上架商品，查看热门商品和涨幅榜。
- **交易中心 (Trade)**:
  - **K线图 (K-Line Chart)**: 实时查看物品价格走势（基于 ECharts）。
  - **深度图 (Order Book)**: 查看当前的买单和卖单列表。
  - **交易面板**: 快速下单（限价买入/卖出），自动匹配撮合交易。
- **个人中心 (Profile)**:
  - **背包 (Inventory)**: 查看和管理持有的游戏资产。
  - **钱包 (Wallet)**: 查看余额、充值、查看交易流水。
  - **订单管理**: 查看和取消当前挂单。
- **安全机制**:
  - **数据一致性**: 启动时自动修复异常订单数据。
  - **交易原子性**: 保证扣款、扣货、订单生成的事务一致性。

## 🛠 技术栈 (Tech Stack)

- **Frontend**: Vue 3, Vite, Vue Router, Pinia (State Management), Axios, ECharts
- **Backend**: Java 21, Spring Boot 3.3.5, Spring Data JPA, Hibernate
- **Database**: PostgreSQL 16
- **DevOps**: Docker (Database), Maven, NPM

## 📂 项目结构 (Project Structure)

```
Database-Project/
├── backend/                 # 后端项目 (Spring Boot)
│   ├── src/main/java/       # Java 源代码 (Controller, Service, Entity)
│   ├── src/main/resources/  # 配置文件 & 数据加载器
│   └── pom.xml              # Maven 依赖
├── database/                # 数据库相关
│   ├── market.sql           # 初始化 SQL 脚本
│   └── pgdata/              # PostgreSQL 数据挂载目录
├── src/                     # 前端项目 (Vue 3)
│   ├── api/                 # API 接口层
│   ├── components/          # 公共组件 (Charts, Cards)
│   ├── views/               # 页面视图 (Trade, Market, Profile)
│   ├── store/               # 状态管理
│   └── router/              # 路由配置
├── vite.config.js           # Vite 配置
└── package.json             # 前端依赖
```

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
- Docker & Docker Compose (推荐)
- Java 21+
- Node.js 18+

### 2. 启动数据库
使用 Docker 快速启动 PostgreSQL 实例：
```bash
# 在项目根目录下运行
docker run --name market-postgres \
  -e POSTGRES_PASSWORD=market \
  -e POSTGRES_USER=market \
  -e POSTGRES_DB=market \
  -p 5432:5432 \
  -v $(pwd)/database/pgdata:/var/lib/postgresql/data \
  -d postgres:16
```

### 3. 启动后端服务
后端启动时会自动连接数据库并初始化测试数据（DataLoader）。
```bash
cd backend
# 运行 Spring Boot 应用
mvn spring-boot:run
```
- 服务端口: `8080`
- **注意**: 如果启动时检测到数据不一致（如卖单缺少资产），系统会自动修复并输出日志 `Fixed X inconsistent orders`。

### 4. 启动前端应用
```bash
# 回到项目根目录
npm install
npm run dev
```
- 访问地址: `http://localhost:5173`

## 🧪 测试账号
系统启动后会预置以下测试账号：
- **Username**: `testuser`
- **Password**: `password`

## 🔌 API 概览
| 模块 | 方法 | 路径 | 描述 |
| --- | --- | --- | --- |
| **Auth** | POST | `/api/auth/login` | 用户登录 |
| **Market** | GET | `/api/market/history` | 获取K线图历史数据 |
| **Trade** | POST | `/api/trade/execute` | 执行交易 (买入/卖出) |
| **User** | GET | `/api/user/inventory/{id}` | 获取用户背包 |

## 📝 开发日志
- **2025-11-28**: 修复了交易时的 `Seller asset not found` 数据不一致问题，增加了启动自修复逻辑。
- **2025-11-28**: 完成了交易界面的 K线图 (Candlestick Chart) 集成。
