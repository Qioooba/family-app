# 后端开发进度

## 已完成
- [x] 重复任务后端 - 2025-02-22 00:30
  - POST /api/task/{id}/repeat - 设置重复规则
  - GET /api/task/{id}/repeat - 获取重复规则
  - 定时任务生成器（每天凌晨2点生成当日任务）
  - 支持重复类型: daily/weekly/monthly/yearly/custom

- [x] 任务提醒后端 - 2025-02-22 00:20
  - POST /api/task/{id}/reminder - 设置任务提醒（支持时间/位置提醒）
  - GET /api/task/{id}/reminders - 获取任务提醒列表
  - DELETE /api/task/reminder/{reminderId} - 删除任务提醒
  - GET /api/task/reminders/pending - 获取用户待处理时间提醒
  - POST /api/task/location/report - 上报位置触发位置提醒
  - 定时推送服务（每分钟检查时间提醒）
  - 地理围栏服务（Haversine公式计算距离）

## 进行中
- [ ] 心愿预算后端

## 待完成
- [ ] 心愿预算后端
- [ ] 心愿里程碑后端
- [ ] 扫码录入后端
- [ ] 价格追踪后端
- [ ] 优惠券管理后端
- [ ] 营养成分分析后端
- [ ] 喝水打卡后端
- [ ] 农历支持后端
- [ ] 多级提醒后端
- [ ] 贺卡生成后端
- [ ] 积分兑换后端
- [ ] 我的券包后端
- [ ] 报告生成后端
- [ ] 多家庭切换后端
- [ ] 数据导出后端
