# 开发进度追踪

## 已完成
- [x] 用户模块API对接（注册/登录/token/个人信息）- 2026-02-21
- [x] 任务模块API对接 - 2026-02-22 00:00 - d6199c3
  - [x] GET /api/task/list - 任务列表
  - [x] POST /api/task/create - 创建任务
  - [x] PUT /api/task/update - 更新任务
  - [x] POST /api/task/complete/{id} - 完成任务
  - [x] POST /api/task/assign/{id} - 指派任务
  - [x] GET /api/task/today/{familyId} - 今日任务
  - [x] DELETE /api/task/{id} - 删除任务
- [x] 心愿模块API对接 - 2026-02-22 00:05 - 9ef8330
  - [x] GET /api/wish/list - 心愿列表
  - [x] POST /api/wish/create - 创建心愿
  - [x] PUT /api/wish/update - 更新心愿
  - [x] POST /api/wish/claim/{id} - 认领心愿
  - [x] POST /api/wish/progress/{id} - 更新进度
  - [x] POST /api/wish/complete/{id} - 完成心愿
  - [x] POST /api/wish/abandon/{id} - 放弃心愿
  - [x] DELETE /api/wish/{id} - 删除心愿

## 进行中
- [ ] 菜谱模块API对接 - 2026-02-22 00:07

## 待完成
### P0 - 前端API对接
- [ ] 菜谱模块API对接
- [ ] 纪念日模块API对接
- [ ] 投票模块API对接
- [ ] 家庭圈模块API对接
- [ ] 购物模块API对接
- [ ] 游戏模块API对接

### P1 - 前端功能开发
- [ ] 子任务前端 - 任务详情页显示子任务
- [ ] 家务排班表前端 - 新增排班表页面
- [ ] 体重记录前端 - 饮食页添加体重入口
