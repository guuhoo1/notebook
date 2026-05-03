# 个人记事本 (Personal Notebook)

一个简洁高效的个人笔记管理应用，支持富文本编辑、分类管理、全文搜索等功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.1.10
- MyBatis Plus 3.5.5
- MySQL 8.0+
- Redis 6.0+
- Sa-Token (认证授权)

### 前端
- Vue 3.4+
- TypeScript 5.4+
- Vite 5.2+
- Tailwind CSS 3.4+
- Pinia (状态管理)
- Vue Router 4

## 项目结构

```
notebook/
├── server/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/notebook/
│   │   │   │   ├── common/     # 公共模块
│   │   │   │   ├── config/     # 配置类
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── mapper/     # 数据访问
│   │   │   │   └── service/    # 服务层
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/init.sql
│   │   └── test/
│   └── pom.xml
│
├── web/                    # 前端项目
│   ├── src/
│   │   ├── components/     # 组件
│   │   ├── layouts/        # 布局
│   │   ├── views/          # 页面
│   │   ├── router/         # 路由
│   │   ├── stores/         # 状态管理
│   │   ├── api/            # API接口
│   │   ├── styles/         # 样式
│   │   └── lib/            # 工具函数
│   ├── package.json
│   └── vite.config.ts
│
└── docs/                   # 项目文档
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
# 1. 创建数据库并执行初始化脚本
mysql -u root -p < server/src/main/resources/db/init.sql

# 2. 修改数据库配置
# 编辑 server/src/main/resources/application.yml
# 修改数据库连接信息和Redis配置

# 3. 启动后端服务
cd server
mvn spring-boot:run
```

后端服务地址: http://localhost:8080/api

### 前端启动

```bash
# 1. 安装依赖
cd web
npm install

# 2. 启动开发服务器
npm run dev
```

前端访问地址: http://localhost:5173

## 功能特性

- ✅ 用户注册/登录
- ✅ 笔记增删改查
- ✅ 富文本编辑
- ✅ 分类管理
- ✅ 全文搜索
- ✅ 标签系统
- ✅ 笔记置顶/归档
- ✅ 响应式设计

## 默认账号

| 字段 | 值 |
|------|-----|
| 邮箱 | test@example.com |
| 手机 | 13800138000 |
| 密码 | password123 |

## 开发命令

### 后端
```bash
mvn spring-boot:run      # 启动服务
mvn test                 # 运行测试
mvn package              # 打包
```

### 前端
```bash
npm run dev              # 开发模式
npm run build            # 生产构建
npm run preview          # 预览构建
npm run lint             # 代码检查
```

## License

MIT
