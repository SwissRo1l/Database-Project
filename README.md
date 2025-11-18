# Game Market MVP (两种实现)

一个最小可运行的“网络游戏金融市场”后端原型，支持：
- 玩家注册与充值
- 道具铸造（根据 `item_types`）
- 上架固定价格的道具
- 浏览市场与购买

技术栈：PostgreSQL + Spring Boot（Java, Maven, 内嵌Tomcat）+ Vue3（Vite）

金额使用整型的最小货币单位（如分），避免浮点误差。

## 快速开始

本项目提供网站前端，进入首页可创建账号，随后进入“市场”浏览与购买，“个人主页”可查看资产与在售。

## 数据库结构概览

核心表：
- `players`：玩家
- `currencies` / `wallets` / `ledger_entries`：货币、钱包与流水
- `item_types` / `items`：道具类型与实例（非同质）
- `listings` / `orders`：上架记录与订单
- `settings`：如 `market_fee_bps`

详见 `db/schema.sql` 与 `db/seed.sql`。

---

## 后端：Spring Boot + PostgreSQL

先决条件
- 已安装 JDK 17+、Maven、Node.js 18+、Docker（用于 Postgres）。

启动 PostgreSQL（Docker）
```
docker compose up -d db
```

启动后端（Spring Boot）——请在仓库根目录或先进入 `backend/`
```
# 从仓库根目录执行（推荐）
mvn -q -DskipTests -f backend/pom.xml flyway:migrate   # 可选：也可跳过，后端启动会自动迁移
mvn -f backend/pom.xml spring-boot:run

# 或者：先进入 backend 目录再执行
# cd backend
# mvn -q -DskipTests flyway:migrate
# mvn spring-boot:run
```
- 配置：`backend/src/main/resources/application.yml`（默认指向 `postgres://market:market@localhost:5432/market`）。
- 首次启动会通过 Flyway 执行 `V1__init.sql` 创建表并写入基础配置。

验证后端
```
curl http://localhost:8080/api/health
curl -X POST http://localhost:8080/api/players -H 'Content-Type: application/json' -d '{"username":"alice"}'
curl -X POST http://localhost:8080/api/players -H 'Content-Type: application/json' -d '{"username":"bob"}'

# 充值（Java端）
curl -X POST http://localhost:8080/api/wallets/deposit -H 'Content-Type: application/json' \
	-d '{"username":"bob","currency":"GOLD","amount":10000}'

# 铸造（Java端，需存在对应 item_type 名称，如 Iron Sword）
curl -X POST http://localhost:8080/api/items/mint -H 'Content-Type: application/json' \
	-d '{"username":"alice","itemType":"Iron Sword","metadata":"{\\"durability\\":95}"}'
## 上架/购买（Java端）
# 先上架刚铸造的道具（假设返回的 item_id 为 1）
curl -X POST http://localhost:8080/api/listings -H 'Content-Type: application/json' \
	-d '{"username":"alice","itemId":1,"currency":"GOLD","price":1500}'

# 查看在售
curl http://localhost:8080/api/listings

# 购买
curl -X POST http://localhost:8080/api/orders/purchase -H 'Content-Type: application/json' \
	-d '{"username":"bob","listingId":1}'
```

启动前端（Vue）
```
cd frontend
npm i
npm run dev
```
- 开发服务器默认端口 `5173`，已在 `vite.config.ts` 将 `/api` 代理到 `http://localhost:8080`。
 - 如需连接 Python 端，将 `frontend/.env` 的 `VITE_API_BASE` 设置为 `http://localhost:8000`；若连接 Java 端（本节），可直接使用 `/api` 代理。

前端页面说明
- 首页：创建账号后自动跳转到“市场”。
- 市场：展示在售装备（已内置少量示例数据），支持购买与快捷“铸造并上架”。
- 个人主页：输入昵称后显示钱包资产、背包与我在售的商品。

项目结构
- `backend/`：Spring Boot 项目（Maven）。
- `backend/src/main/resources/db/migration`：数据库结构与示例数据（V1/V2/V3）。
- `frontend/`：Vite + Vue3 应用，`/api` 代理至后端。

下一步建议（Java端）
- 实体与仓库：`Currency/Wallet/ItemType/Item/Listing/Order/LedgerEntry/Settings`。
- 业务服务：上架、购买（事务内锁/余额扣减/手续费/过户）。
- 控制器：`/api/listings`（查询/上架/下架）、`/api/orders`（购买/查询）。


## 一致性与并发

- 使用 SQLite 事务（`BEGIN IMMEDIATE`）确保购买过程的原子性。
- 物品状态机：`in_inventory -> listed -> sold`，上架时锁定，成交后转移所有权。
- 费用采用基点（bps）计费，避免浮点。

## 后续扩展建议

- 多币种、拍卖单、下架/过期、手续费入库钱包、道具回滚与纠纷处理。
- 切换 PostgreSQL 时可将触发器/时间戳改为 `NOW()` 并使用行级锁。

