# Game Market Project (游戏资产交易市场)

这是一个基于 Spring Boot 和 Vue 3 的全栈游戏资产交易平台。项目模拟了一个真实的游戏内市场，支持玩家之间的物品买卖、价格走势分析（K线图）、资产管理以及钱包充值等功能。

![Vue 3](https://img.shields.io/badge/Vue-3.x-4FC08D?style=flat-square&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql)

## ✨ 主要功能 (Features)

### 🛒 市场系统 (Marketplace)
- **实时行情**: 首页展示热门推荐商品和 24小时涨幅榜。
- **全局搜索**: 支持通过关键词快速搜索特定装备（如 "AWP", "Dragon Lore"）。
- **视觉体验**: 全量覆盖 80+ 种武器皮肤组合的高清预览图（集成 Steam 社区资源）。

### 📊 交易中心 (Trading Center)
- **专业图表**: 集成 ECharts 绘制实时 K线图，支持查看历史价格走势。
- **深度数据**: 实时展示买卖盘口（Order Book），辅助交易决策。
- **快速撮合**: 支持限价买入/卖出，系统自动匹配最优价格进行撮合。

### 👤 用户中心 (User Profile)
- **资产背包**: 可视化管理持有的游戏饰品，支持查看稀有度、参考价和持仓成本。
- **钱包系统**: 支持模拟充值、提现，并提供详细的资金流水记录（包含买入支出、卖出收入）。
- **订单管理**: 实时查看当前挂单状态，支持随时撤单。

### 🛡️ 系统特性 (System Features)
- **数据一致性**: 启动时自动扫描并修复异常订单数据（Self-Healing）。
- **事务安全**: 严格的数据库事务控制，确保资金扣除与资产转移的原子性。
- **自动做市**: 内置数据加载器（DataLoader），初始化时自动生成丰富的市场流动性。

## 🛠 技术栈 (Tech Stack)

- **Frontend**: 
  - Vue 3 (Composition API)
  - Vite (Build Tool)
  - Vue Router (Routing)
  - Pinia (State Management)
  - ECharts (Data Visualization)
- **Backend**: 
  - Java 21
  - Spring Boot 3.3.5
  - Spring Data JPA / Hibernate
  - Lombok
- **Database**: 
  - PostgreSQL 16
- **DevOps**: 
  - Docker & Docker Compose
  - Maven
  - NPM

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
│   ├── components/          # 公共组件 (ItemCard, NavBar, Charts)
│   ├── views/               # 页面视图 (Market, Trade, Inventory)
│   ├── store/               # 状态管理 (User, Market)
│   ├── utils/               # 工具类 (Image Mapping, Request)
│   └── router/              # 路由配置
├── vite.config.js           # Vite 配置
└── package.json             # 前端依赖
```

## 🚀 快速开始 (Getting Started)

### 1. 环境准备
- Docker & Docker Compose (推荐用于数据库)
- Java 21+ (后端运行)
- Node.js 18+ (前端运行)

### 2. 启动数据库
使用 Docker 快速启动 PostgreSQL 实例：

```bash
# 首次启动
docker run --name market-postgres \
  -e POSTGRES_PASSWORD=market \
  -e POSTGRES_USER=market \
  -e POSTGRES_DB=market \
  -p 5432:5432 \
  -v $(pwd)/database/pgdata:/var/lib/postgresql/data \
  -d postgres:16

# 后续启动
docker start market-postgres
```

### 3. 启动后端服务
后端启动时会自动连接数据库并初始化测试数据。

```bash
cd backend
mvn spring-boot:run
```
- 服务端口: `8080`
- API 文档: 启动后访问后端接口

### 4. 启动前端应用
```bash
# 回到项目根目录
npm install
npm run dev
```
- 访问地址: `http://localhost:5173`

## 📝 最近更新 (Recent Updates)

- **[Feature] 全局搜索**: 导航栏新增搜索功能，支持模糊查询装备名称。
- **[UI] 图片系统升级**: 完善了 `itemImages.js` 映射表，覆盖所有 80 种生成的武器皮肤组合，解决了图片缺失问题。
- **[Fix] 数据同步**: 修复了交易后余额刷新延迟和背包显示异常的问题。
- **[UX] 交互优化**: 优化了买卖操作的反馈提示和订单状态显示。

## 📄 License

MIT License
