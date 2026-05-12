# 几头后端设计

## 目标

后端使用 Supabase，负责账号身份、双人关系、剪头记录、共同约剪计划、历史约剪和云端提醒偏好。Android 端继续保留 Room 作为本地缓存，Supabase 作为跨设备与朋友协作的数据源。

## 架构

- Auth：使用 Supabase Auth，每个用户对应 `auth.users.id`。
- Database：Postgres 表 + Row Level Security。
- Client sync：Android 端 Room 先读写本地，后续加 sync repository 将本地变更同步到 Supabase。
- Realtime：约剪计划表开启 realtime 后，一方发起/修改/取消，另一方可以实时收到状态变化。
- Notification：第一版仍以本地提醒为主；后续可用 Edge Function + 推送服务做云端提醒。

## 数据模型

- `profiles`：用户资料，昵称和头像。
- `haircut_pairs`：固定双人小组。
- `haircut_pair_members`：小组成员关系。
- `haircut_records`：个人剪头记录。
- `joint_haircut_plans`：共同约剪计划，覆盖待确认、已确认、取消、完成。
- `reminder_preferences`：用户提醒偏好。

## 状态流

约剪计划状态：

- `pending`：一方发起，等待另一方确认。
- `confirmed`：双方已确认，形成共同日程。
- `cancelled`：计划取消。
- `completed`：已经完成剪头，可进入历史约剪。

关键规则：

- 同一个 pair 同一时间只允许一个 `pending` 或 `confirmed` 的活跃计划。
- 只有 pair 成员能读取该 pair 的计划。
- 只有发起人能修改待确认计划的时间。
- 非发起的另一方可以确认计划。
- pair 成员都可以取消计划。
- pair 成员都可以将 confirmed 计划标记完成。

## RLS 策略

原则：

- 用户只能读取和修改自己的 `profiles`、`reminder_preferences`、`haircut_records`。
- pair 成员可以读取 pair、成员列表和共同计划。
- pair 创建者可以创建 pair；成员关系首版可由客户端邀请流程或后台插入。
- 共同计划的写入只允许 pair 成员执行。

## Android 同步建议

Room 表保持当前本地结构，新增远端字段：

- `remoteId`
- `updatedAt`
- `deletedAt`
- `syncState`

同步策略：

- App 启动后拉取 `updated_at > lastSyncAt` 的远端数据。
- 本地新增/修改先写 Room，再异步 upsert 到 Supabase。
- 约剪计划以 Supabase 为准；如果本地与远端冲突，使用 `updated_at` 最新值。
- 删除使用软删除 `deleted_at`，避免多端同步丢失。

## API 视角

Android 端需要的查询：

- 当前用户资料：`profiles where id = auth.uid()`
- 我的剪头记录：`haircut_records where user_id = auth.uid() order by haircut_date desc`
- 我的 pair：通过 `haircut_pair_members where user_id = auth.uid()`
- 当前共同计划：`joint_haircut_plans where pair_id = ? and status in ('pending', 'confirmed')`
- 历史约剪：`joint_haircut_plans where pair_id = ? and status in ('completed', 'cancelled')`
- 提醒偏好：`reminder_preferences where user_id = auth.uid()`

## 下一步实现

1. 在 Supabase 执行 `supabase/migrations/20260506000000_initial_backend_schema.sql`。
2. Android 添加 Supabase client 配置。
3. 新建 remote data source，对应 profiles、records、plans、reminders。
4. 在现有 Room Repository 外层增加 Sync Repository。
5. 先做手动同步，再接 Realtime 订阅共同计划。
