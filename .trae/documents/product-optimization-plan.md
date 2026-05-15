# 个人笔记本产品优化实施计划

## 📋 项目概述

基于对现有个人笔记本项目的分析，本计划将分三个阶段推进产品优化和功能新增，目标是提升用户体验、增强核心竞争力。

## 🎯 第一阶段：核心体验优化（1-2周）

### 1. 回收站功能
**优先级：P0**

#### 功能描述
- 删除笔记时移入回收站而非永久删除
- 回收站保留 30 天，到期自动清理
- 支持从回收站恢复笔记
- 支持永久删除回收站中的笔记
- 清空回收站功能

#### 数据库变更
```sql
-- 修改 note 表，增加 deleted_at 字段
ALTER TABLE note ADD COLUMN deleted_at DATETIME NULL COMMENT '删除时间';

-- 或新建 recycle_bin 表
CREATE TABLE recycle_bin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  note_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT,
  deleted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expire_at DATETIME NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user(id)
);
```

#### 后端实现
- `NoteController`: 新增 `/recycle-bin` 相关接口
  - `GET /api/recycle-bin`: 获取回收站列表
  - `POST /api/recycle-bin/:id/restore`: 恢复笔记
  - `DELETE /api/recycle-bin/:id`: 永久删除
  - `DELETE /api/recycle-bin`: 清空回收站
- `NoteService`: 修改删除逻辑，移入回收站而非物理删除
- 定时任务：每天清理过期的回收站笔记

#### 前端实现
- 新建 `views/note/RecycleBin.vue`
- 修改 `NoteList.vue` 中的删除逻辑，添加二次确认
- 侧边栏添加"回收站"入口
- 回收站页面支持恢复、永久删除、清空操作

#### 文件清单
```
server/src/main/java/com/notebook/
├── controller/RecycleBinController.java
├── service/RecycleBinService.java
├── mapper/RecycleBinMapper.java
└── entity/RecycleBin.java

web/src/
├── views/note/RecycleBin.vue
├── api/recycleBin.ts
└── stores/recycleBin.ts
```

---

### 2. 笔记版本历史
**优先级：P0**

#### 功能描述
- 每次保存笔记时自动创建版本快照
- 版本列表显示时间、作者、变更摘要
- 支持查看历史版本内容（对比视图）
- 支持回退到任意历史版本
- 保留最近 50 个版本

#### 数据库变更
```sql
CREATE TABLE note_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  note_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  version_number INT NOT NULL,
  title VARCHAR(200),
  content MEDIUMTEXT,
  md_content MEDIUMTEXT,
  summary VARCHAR(500),
  change_summary VARCHAR(200),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (note_id) REFERENCES note(id),
  FOREIGN KEY (user_id) REFERENCES user(id),
  INDEX idx_note_id (note_id)
);
```

#### 后端实现
- `NoteVersionController`: 版本管理接口
  - `GET /api/notes/:id/versions`: 获取版本列表
  - `GET /api/notes/:id/versions/:versionId`: 获取版本详情
  - `POST /api/notes/:id/versions/:versionId/restore`: 回退到版本
- `NoteService`: 保存笔记时自动创建版本
- 限制版本数量，超过 50 个时清理最旧版本

#### 前端实现
- `NoteEditor.vue`: 添加"版本历史"按钮
- 新建 `components/VersionHistory.vue`: 版本列表弹窗
- 新建 `components/VersionCompare.vue`: 版本对比视图
- 支持查看、对比、回退操作

#### 文件清单
```
server/src/main/java/com/notebook/
├── controller/NoteVersionController.java
├── service/NoteVersionService.java
├── mapper/NoteVersionMapper.java
└── entity/NoteVersion.java

web/src/
├── components/VersionHistory.vue
├── components/VersionCompare.vue
├── api/noteVersion.ts
└── stores/noteVersion.ts
```

---

### 3. 笔记导出功能
**优先级：P0**

#### 功能描述
- 支持导出单篇笔记
- 支持批量导出多篇笔记
- 导出格式：Markdown (.md)、PDF (.pdf)、HTML (.html)
- 支持导出为 ZIP 压缩包（批量时）

#### 后端实现
- `ExportController`: 导出接口
  - `GET /api/export/note/:id?format=md|pdf|html`: 导出单篇
  - `POST /api/export/notes`: 批量导出
- `ExportService`: 实现各格式导出逻辑
  - Markdown: 直接使用 md_content
  - HTML: 转换或使用 html_content
  - PDF: 使用 wkhtmltopdf 或 iText 库

#### 前端实现
- `NoteDetail.vue` 和 `NoteList.vue`: 添加导出按钮
- 新建 `components/ExportDialog.vue`: 导出选项弹窗
- 支持选择格式、选择笔记（批量）

#### 文件清单
```
server/src/main/java/com/notebook/
├── controller/ExportController.java
└── service/ExportService.java

web/src/
└── components/ExportDialog.vue
```

---

### 4. 快捷键支持
**优先级：P0**

#### 功能描述
- 全局快捷键
  - `Ctrl/Cmd + N`: 新建笔记
  - `Ctrl/Cmd + K`: 快速搜索
  - `Ctrl/Cmd + S`: 保存笔记
  - `Esc`: 关闭弹窗/退出编辑
- 编辑器快捷键
  - `Ctrl/Cmd + B`: 加粗
  - `Ctrl/Cmd + I`: 斜体
  - `Ctrl/Cmd + H`: 标题
- 快捷键提示弹窗

#### 前端实现
- 使用 `vueuse/core` 的 `useMagicKeys` 或自定义 hooks
- 新建 `composables/useKeyboardShortcuts.ts`
- 新建 `components/KeyboardShortcutsHelp.vue`: 快捷键帮助弹窗
- `TopNav.vue`: 添加快捷键帮助按钮

#### 文件清单
```
web/src/
├── composables/useKeyboardShortcuts.ts
└── components/KeyboardShortcutsHelp.vue
```

---

## 🎯 第二阶段：功能增强（2-3周）

### 5. 笔记模板
**优先级：P1**

#### 功能描述
- 预置常用模板
  - 会议纪要
  - 待办清单
  - 学习笔记
  - 日记
  - 项目计划
- 支持自定义模板
- 支持从现有笔记创建模板
- 模板分类管理

#### 数据库变更
```sql
CREATE TABLE template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(200),
  category VARCHAR(50),
  content MEDIUMTEXT,
  md_content MEDIUMTEXT,
  is_system BOOLEAN DEFAULT FALSE,
  sort_order INT DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id)
);
```

#### 后端实现
- `TemplateController`: 模板管理接口
- `TemplateService`: 模板业务逻辑

#### 前端实现
- 新建 `views/template/TemplateList.vue`
- 新建 `components/TemplateSelector.vue`: 创建笔记时选择模板
- 新建 `views/template/TemplateEditor.vue`: 编辑自定义模板

---

### 6. 日历视图/时间线
**优先级：P1**

#### 功能描述
- 日历视图：按月展示笔记
- 时间线视图：按时间顺序展示笔记
- 点击日期查看当天笔记
- 支持切换视图

#### 前端实现
- 新建 `views/note/CalendarView.vue`
- 新建 `views/note/TimelineView.vue`
- 修改 `NoteList.vue`: 添加视图切换按钮
- 使用 `vue-cal` 或 `fullcalendar` 库

---

### 7. 笔记提醒
**优先级：P1**

#### 功能描述
- 给笔记设置提醒时间
- 支持重复提醒（每天/每周/每月）
- 到期时发送邮件/站内通知
- 提醒列表管理

#### 数据库变更
```sql
CREATE TABLE reminder (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  note_id BIGINT NOT NULL,
  reminder_time DATETIME NOT NULL,
  repeat_type VARCHAR(20) DEFAULT 'none', -- none, daily, weekly, monthly
  is_sent BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES user(id),
  FOREIGN KEY (note_id) REFERENCES note(id)
);
```

#### 后端实现
- `ReminderController`: 提醒管理接口
- `ReminderService`: 提醒业务逻辑
- 定时任务：每分钟检查并发送提醒

#### 前端实现
- `NoteEditor.vue`: 添加提醒设置按钮
- 新建 `components/ReminderDialog.vue`
- 新建 `views/reminder/ReminderList.vue`

---

### 8. 批量操作
**优先级：P1**

#### 功能描述
- 多选笔记
- 批量删除
- 批量移动分类
- 批量归档
- 批量添加标签

#### 前端实现
- 修改 `NoteList.vue`: 添加选择模式
- 新建 `components/BatchOperationsBar.vue`: 批量操作工具栏

---

## 🎯 第三阶段：锦上添花（3-4周）

### 9. 主题切换
**优先级：P2**

#### 功能描述
- 支持浅色/深色模式
- 支持更多配色主题
- 跟随系统主题

#### 前端实现
- 新建 `stores/theme.ts`
- 修改 `App.vue` 和全局样式
- `Settings.vue`: 添加主题设置

---

### 10. 数据统计
**优先级：P2**

#### 功能描述
- 笔记数量统计
- 写作时长统计
- 活跃天数
- 分类/标签分布
- 数据可视化图表

#### 后端实现
- `StatisticsController`: 统计数据接口

#### 前端实现
- 新建 `views/statistics/StatisticsDashboard.vue`
- 使用 `echarts` 或 `chart.js` 绘图

---

### 11. 移动端适配优化
**优先级：P2**

#### 功能描述
- 优化移动端响应式布局
- 手势支持
- PWA 支持（离线使用）

---

### 12. 笔记协作（可选）
**优先级：P2**

#### 功能描述
- 邀请协作者
- 权限管理（编辑/查看）
- 实时协作（可选，使用 WebSocket）

---

## 🛠 技术实施策略

### 数据库迁移策略
- 使用 Flyway 或 Liquibase 管理数据库版本
- 每次变更编写迁移脚本
- 回滚脚本准备

### 代码规范
- 遵循现有代码风格
- TypeScript 类型完整
- 必要的注释
- 单元测试覆盖核心逻辑

### 发布策略
- 每个阶段独立发布
- 先发布后端 API，再发布前端
- 灰度发布，监控异常

---

## 📅 时间规划

| 阶段 | 任务 | 预计时间 |
|------|------|----------|
| 第一阶段 | 回收站、版本历史、导出、快捷键 | 1-2 周 |
| 第二阶段 | 模板、日历视图、提醒、批量操作 | 2-3 周 |
| 第三阶段 | 主题、统计、移动端、协作 | 3-4 周 |

---

## ⚠️ 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 数据库迁移出错 | 高 | 先在测试环境验证，准备回滚脚本 |
| PDF 导出性能差 | 中 | 异步处理，或使用第三方服务 |
| 版本历史存储过大 | 中 | 限制版本数量，定期清理旧版本 |
| 开发时间超出预期 | 中 | 优先保证核心功能，非核心可延后 |

---

## ✅ 验收标准

### 第一阶段验收
- [ ] 回收站功能完整可用
- [ ] 版本历史可查看、可回退
- [ ] 支持导出 Markdown、PDF、HTML
- [ ] 快捷键正常工作，有帮助提示
- [ ] 无明显 Bug，性能正常
- [ ] 代码符合规范，有必要的测试

### 第二阶段验收
- [ ] 模板功能可用，有预置模板
- [ ] 日历和时间线视图正常
- [ ] 提醒功能可设置、可发送
- [ ] 批量操作流畅

### 第三阶段验收
- [ ] 主题切换正常
- [ ] 统计数据准确，图表美观
- [ ] 移动端体验良好
