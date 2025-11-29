# Game Market Project (游戏资产交易市场)

这是一个基于 Spring Boot 和 Vue 3 的全栈游戏资产交易平台。项目模拟了一个真实的游戏内市场，支持玩家之间的物品买卖、价格走势分析（K线图）、资产管理以及钱包充值等功能。

![Vue 3](https://img.shields.io/badge/Vue-3.x-4FC08D?style=flat-square&logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql)

## ✨ 最新更新 (Latest Updates)

- **🎨 沉浸式 UI**: 全新设计的动态粒子背景（Particle Network），支持鼠标互动；新增波浪式加载动画，提升视觉流畅度。
- **🔍 高级筛选**: 市场页面新增“全部商品”视图，支持按**分类**（步枪、狙击枪、手枪、刀具）筛选，并提供多种**排序**方式（价格升/降序、最新发布）。
- **🖼️ 个性化头像**: 用户中心支持本地上传自定义头像，打造专属个人资料。
- **📦 库存管理**: 优化了背包界面，新增分页功能，轻松管理海量库存物品。

## 🚀 主要功能 (Features)

### 🛒 市场系统 (Marketplace)
- **实时行情**: 首页展示热门推荐商品和 24小时涨幅榜。
- **全局搜索**: 支持通过关键词快速搜索特定装备（如 "AWP", "Dragon Lore"）。
- **分类浏览**: 完整的分类索引和排序工具，帮助用户快速发现心仪商品。
- **视觉体验**: 全量覆盖 80+ 种武器皮肤组合的高清预览图。

### 📊 交易中心 (Trading Center)
- **专业图表**: 集成 ECharts 绘制实时 K线图，支持查看历史价格走势。
- **深度数据**: 实时展示买卖盘口（Order Book），辅助交易决策。
- **快速撮合**: 支持限价买入/卖出，系统自动匹配最优价格进行撮合。

### 👤 用户中心 (User Profile)
- **资产背包**: 可视化管理持有的游戏饰品，支持分页浏览、查看稀有度、参考价和持仓成本。
- **钱包系统**: 支持模拟充值、提现，并提供详细的资金流水记录。
- **订单管理**: 实时查看当前挂单状态，支持随时撤单。
- **个人设置**: 支持修改昵称、上传头像等个性化设置。

## 🛠 技术栈 (Tech Stack)

- **Frontend**: 
  - Vue 3 (Composition API)
  - Vite (Build Tool)
  - Vue Router (Routing)
  - Pinia (State Management)
  - ECharts (Data Visualization)
  - CSS3 Animations & Canvas
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

## 🏁 快速开始 (Quick Start)

### 1. 环境准备
确保本地已安装：
- Node.js 18+
- Java 21 (JDK)
- Maven 3.8+
- Docker (可选，用于运行数据库)

### 2. 启动数据库
项目根目录下运行：
```bash
docker start market-postgres
# 或者使用 docker-compose up -d
```

### 3. 启动后端
```bash
cd backend
mvn spring-boot:run
```
后端服务将运行在 `http://localhost:8080`。

### 4. 启动前端
```bash
npm install
npm run dev
```
前端服务将运行在 `http://localhost:5173`。

## 📂 项目结构

```
├── backend/            # Spring Boot 后端源码
│   ├── src/main/java   # Java 业务逻辑
│   └── uploads/        # 用户上传文件存储目录
├── database/           # SQL 初始化脚本
├── src/                # Vue 3 前端源码
│   ├── api/            # Axios 请求封装
│   ├── assets/         # 静态资源 & 全局样式
│   ├── components/     # 公共组件 (ParticleBackground, NavBar等)
│   ├── router/         # 路由配置
│   ├── store/          # Pinia 状态管理
│   └── views/          # 页面视图 (Market, Inventory, Profile等)
└── vite.config.js      # Vite 配置 (含代理设置)
```

## 📝 开发日志

- **2025-11-29**: 
  - 实现了基于 Canvas 的动态粒子背景。
  - 修复了库存页面的分页显示问题。
  - 完善了头像上传功能的本地存储与静态资源映射。
  - 优化了市场筛选器的交互逻辑与加载状态。

---
*Project maintained by SwissRo1l*
