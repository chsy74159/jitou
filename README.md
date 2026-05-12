# 几头

几头（几时剪头）是一个给固定双人使用的轻量 Android app。当前版本先实现首页可交互静态版，使用本地假数据展示剪头记录、分析、共同日程和提醒偏好。

## 当前实现

- 原生 Android：Kotlin + Jetpack Compose + Material 3
- 包名：`com.jitou.app`
- 首页入口：
  - 记录一次剪头
  - 提议下次日期
  - 查看分析
  - 提醒设置
- 约剪页：
  - 当前共同计划的无计划、待确认、待我确认、已确认状态
  - 选择日期、选择时间、发送确认三步发起流程
  - 历史约剪列表
- 我的页：
  - 用户信息卡片和个人功能入口
  - 历史平均、最近一次、最长、最短间隔统计
  - 近 5 次剪头间隔和周几频率分析
  - 趋势提示
- Room 本地数据库：
  - 剪头记录、约剪计划、约剪历史、提醒偏好已持久化到本地 Room
  - 首次启动会写入一组默认演示数据
- 当前仍不接 Supabase，不注册系统通知。

## 项目结构

主要目录和文件职责见 [docs/PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md)。

## 本地打开

当前机器未检测到 Android SDK、Gradle 或 JDK 17。建议用 Android Studio 打开本目录并安装：

- JDK 17
- Android SDK 36
- Gradle 8.13+

同步后运行 `:app` 的 debug 版本即可。
