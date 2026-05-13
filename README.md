# 几头

几头（几时剪头）是一个给固定双人使用的轻量 Android app，用来记录剪头日期、查看周期分析、发起共同约剪计划，并根据剪头状态安排本地提醒。

## 功能概览

- 原生 Android：Kotlin + Jetpack Compose + Material 3
- 本地数据：Room 持久化剪头记录、约剪计划、历史约剪和提醒偏好
- 云端同步：Supabase Auth + PostgREST + Realtime 依赖已接入，支持账号登录后的资料、记录、约剪计划和提醒偏好同步
- 通知提醒：根据剪头记录和约剪状态计算下一次本地通知，并在开机后重新调度
- 主要页面：
  - 登录页：支持内部账号名自动补全为 `@jitou.app`
  - 首页：记录剪头、查看当前约剪状态、进入提醒和个人页
  - 约剪页：发起、修改、接受、拒绝、取消和完成共同约剪计划
  - 我的页：查看剪头统计、修改昵称、刷新远端数据和退出登录

## 技术栈

- Kotlin 2.2.21
- Android Gradle Plugin 9.2.0
- Jetpack Compose BOM 2026.04.01
- Room 2.8.4
- Kotlinx Serialization
- Ktor Android client
- Supabase Kotlin client BOM 3.1.2

## 项目结构

```text
.
├── app/
│   ├── build.gradle.kts
│   ├── schemas/                         # Room schema 导出
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/jitou/app/
│       │   │   ├── data/                # Auth、Room、Supabase、同步仓库
│       │   │   ├── model/               # 领域模型和业务规则
│       │   │   ├── notifications/       # 本地提醒调度和广播接收器
│       │   │   └── ui/                  # Compose 页面和主题
│       │   └── res/
│       └── test/                        # JVM 单元测试
├── docs/                                # 项目结构和后端设计文档
├── supabase/migrations/                 # Supabase 数据库迁移
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 本地运行

### 环境要求

- JDK 17
- Android SDK 36
- Gradle Wrapper 会自动下载 Gradle 9.4.1
- 推荐使用 Android Studio 打开项目

### 配置 Android SDK

在根目录创建或更新 `local.properties`：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

也可以通过系统环境变量设置 `ANDROID_HOME`。

### 配置 Supabase

如果要使用登录和云端同步，需要在 `local.properties` 中加入：

```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_PUBLISHABLE_KEY=your-publishable-key
```

未配置 Supabase 时，访问 `SupabaseClientProvider.client` 会抛出配置缺失错误。当前主流程进入登录页时会创建 Supabase client，因此调试真实 app 前请先完成该配置。

### 安装数据库迁移

Supabase 后端结构在：

```text
supabase/migrations/20260506000000_initial_backend_schema.sql
```

该迁移包含用户资料、双人组、成员关系、剪头记录、共同约剪计划、提醒偏好和 RLS policy。部署前请先审查 RLS 是否符合当前邀请和协作规则。

## 常用命令

```powershell
# 运行 JVM 单元测试
.\gradlew.bat testDebugUnitTest

# 构建 debug 包
.\gradlew.bat assembleDebug

# 安装 debug 包到已连接设备
.\gradlew.bat installDebug
```

如果命令提示 `SDK location not found`，请检查 `local.properties` 中的 `sdk.dir` 或 `ANDROID_HOME`。

## 数据与同步说明

- Room 是 app 的本地数据源，UI 通过 Repository 暴露的 Flow 自动更新。
- 本地新增或修改后会先写入 Room，再尝试推送到 Supabase。
- 同步字段包括 `remoteId`、`updatedAtMillis`、`deletedAtMillis` 和 `syncState`。
- 远端数据按 `updated_at` 增量拉取，并映射回本地实体。
- 约剪计划以 Supabase 中的 `joint_haircut_plans` 为协作来源，本地只保留当前活跃计划和历史展示项。

## 测试覆盖

当前已有 JVM 单元测试覆盖：

- 剪头周期统计
- 约剪状态展示规则
- 本地提醒规则
- 队列状态机
- 首页导航规则
- Supabase 远端模型映射
- 登录账号归一化

## 已知注意事项

- `README`、`docs` 和代码应保持同步；当前代码已经包含 Supabase 和系统通知逻辑。
- 首次默认演示数据的 seed 方法存在于 Repository 中，但需要确认启动链路是否实际调用。
- Supabase RLS policy 涉及真实用户数据，部署前应按邀请、确认、取消和完成流程逐条验证。
