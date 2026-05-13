# 几头 (Jitou) 项目代码审查报告

几头是一个基于 Android 原生开发（Kotlin + Jetpack Compose）的剪头提醒与双人协作应用。通过对代码库的深入分析，以下是本次审查的总结。

## 1. 架构概览

项目采用了清晰的 **Layered Architecture (分层架构)**，并遵循了 **Offline-First (离线优先)** 的设计原则：

- **UI 层**: 使用 Jetpack Compose 和 Material 3。通过 `ViewModel` (如 `JitouHomeViewModel`) 维护 UI 状态，并利用 `collectAsStateWithLifecycle` 观察数据流。
- **Repository 层**: `JitouRepository` 作为数据入口，协调本地 Room 数据库和 `SyncRepository`。
- **Data 层**: 
    - **Local**: 使用 Room 存储记录、约剪计划和提醒设置。
    - **Remote**: 使用 Supabase (Auth, PostgREST) 作为云端后端。
    - **Sync**: `SyncRepository` 实现了基于 `updatedAt` 时间戳的增量同步和本地变更推送。
- **Model 层**: 纯 Kotlin 定义的领域模型 (Domain Model) 和业务规则 (如 `HaircutAnalytics`, `HaircutNotificationRules`)，不依赖 Android 框架。

## 2. 技术栈亮点

- **Kotlin 2.2.21 + Compose**: 使用了非常前沿的 Kotlin 版本和 Compose 编译器插件。
- **Supabase Kotlin SDK**: 深度集成了 Supabase，包括身份验证和数据库操作。
- **Room 2.8.4**: 用于本地持久化，支持迁移逻辑 (1 -> 2)。
- **Ktor**: 用于网络通信。
- **Material 3**: 界面设计感强，使用了大量的自定义组合项 (Custom Composables)。

## 3. 核心功能分析

### 3.1 同步逻辑 (SyncRepository)
`SyncRepository` 采用了一种经典的“双向同步”方案：
1. **Pull**: 启动或刷新时，根据 `sync_metadata` 中记录的 `lastSyncAt` 增量拉取远端更新。
2. **Push**: 在本地 `upsert` 操作时将 `syncState` 标记为 `PENDING_CREATE/UPDATE`，随后通过 `pushPendingLocal` 异步推送到 Supabase。
3. **冲突处理**: 简单地使用了 `updatedAtMillis` 比较，保证本地较新的修改不被远端覆盖。

### 3.2 提醒规则 (HaircutNotificationRules)
提醒逻辑非常贴近生活场景：
- 根据上次剪头的天数，在特定周期 (22天, 25-27天, 28-31天等) 触发不同强度的提醒。
- 实现了 `HaircutNotificationScheduler`，利用 `AlarmManager` 实现精准提醒，并注册了 `BOOT_COMPLETED` 广播接收器以在重启后自动恢复闹钟。

### 3.3 UI 交互 (AppointmentFlow)
约剪功能采用了引导式的三步流程 (日期 -> 时间 -> 发送)，交互体验优于单纯的表单。

## 4. 改进建议与优化空间

### 4.1 实时性增强 (Realtime)
目前项目依赖于手动或定时调用 `refreshRemoteChanges`。
- **建议**: 启用 Supabase Realtime 订阅 `joint_haircut_plans` 表。这样当对方接受或修改计划时，本地可以立即感知并更新 UI。

### 4.2 错误处理与用户提示
部分 Repository 操作使用了 `runCatching` 但未对 UI 暴露具体的错误信息。
- **建议**: 在 `UiState` 中增加一个 `error` 字段，并在 `ViewModel` 中捕获异常后向用户展示 (如 Snackbar)。

### 4.3 代码健壮性 (Notification Rules)
在 `HaircutNotificationRules` 中，`NotificationSearchWindowDays` 设置为 370 天，且循环查找下一条提醒。
- **建议**: 考虑在极端数据 (如几年没剪头) 下的性能开销，虽然目前计算量较小，但加入 `maxIteration` 限制会更稳妥。

### 4.4 资源硬编码
虽然大部分颜色和尺寸使用了自定义 Token，但仍有部分字符串在代码中硬编码 (如 `"正在排队"`, `"去排队"`)。
- **建议**: 逐步迁移到 `strings.xml` 以便后续支持多语言。

## 5. 总结

**几头** 项目代码质量很高，不仅实现了基本的 CRUD，还完整地处理了身份验证、双端同步和系统级提醒。其 UI 设计非常精致，尤其是自定义的主题系统和约剪页面的 Pager 导航。项目结构清晰，非常适合作为中小型协作类 Android 应用的参考范本。
