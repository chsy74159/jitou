# 几头项目结构说明

## 根目录

- `settings.gradle.kts`：Gradle 项目设置，声明仓库源和 `:app` 模块。
- `build.gradle.kts`：根级 Gradle 插件版本声明，包括 Android、Kotlin Compose、KSP。
- `gradle.properties`：Gradle、Android、Kotlin 的构建配置。
- `gradlew.bat` / `gradlew`：Gradle Wrapper 启动脚本。
- `README.md`：项目简介、当前功能和本地运行说明。

## App 模块

- `app/build.gradle.kts`：Android app 模块配置，声明 Compose、Room、KSP、测试依赖。
- `app/proguard-rules.pro`：Release 混淆规则占位文件。
- `app/src/main/AndroidManifest.xml`：应用入口、Activity、主题、图标配置。

## Kotlin 源码

- `app/src/main/java/com/jitou/app/MainActivity.kt`：Android 单 Activity 入口，挂载 Compose 根路由。

### `model`

- `model/HaircutModels.kt`：业务领域模型，包含剪头记录、统计、约剪计划、提醒状态、历史约剪。
- `model/HaircutAnalytics.kt`：剪头统计计算，包括距离上次剪头、平均间隔、近几次间隔、周几频率。
- `model/FakeHaircutData.kt`：首次启动 Room seed 使用的演示数据。

### `data/local`

- `data/local/JitouEntities.kt`：Room Entity 与领域模型之间的转换函数。
- `data/local/JitouDao.kt`：Room DAO，定义剪头记录、约剪计划、历史约剪、提醒设置的数据库读写接口。
- `data/local/JitouDatabase.kt`：RoomDatabase 单例，创建本地 `jitou.db`。

### `data/repository`

- `data/repository/JitouRepository.kt`：数据仓库层，对 UI 暴露 `Flow`，封装 Room 读写、首次 seed、记录新增、提醒更新、约剪状态更新。

### `ui`

- `ui/home/JitouHomeScreen.kt`：应用顶层路由和首页 UI；连接 Room Repository、首页、约剪页、我的页、弹窗状态。
- `ui/appointment/AppointmentScreen.kt`：约剪页 UI；包含当前计划状态、发起约剪三步流程、历史约剪列表。
- `ui/profile/ProfileScreen.kt`：我的页 UI；包含用户信息、功能入口、统计卡片、近几次间隔、周几频率、趋势提示。
- `ui/theme/JitouTheme.kt`：Compose Material 主题和颜色配置。

## 资源文件

- `res/values/strings.xml`：应用名称等字符串资源。
- `res/values/themes.xml`：浅色系统主题。
- `res/values-night/themes.xml`：深色系统主题。
- `res/drawable/ic_launcher_background.xml`：启动图标背景。
- `res/drawable/ic_launcher_foreground.xml`：启动图标前景。
- `res/mipmap-anydpi-v26/ic_launcher.xml`：自适应启动图标。
- `res/mipmap-anydpi-v26/ic_launcher_round.xml`：圆形自适应启动图标。

## 测试

- `app/src/test/java/com/jitou/app/model/HaircutAnalyticsTest.kt`：剪头统计逻辑单元测试。
