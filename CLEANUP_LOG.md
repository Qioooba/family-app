# 前端页面清理记录
## 时间: 2025-01-XX
## 备份分支: backup/cleanup-YYYYMMDD

---

## 分析结果总结

### 保留的文件（新功能/完整功能）

#### Task模块 - 保留的功能页面（13个）
1. ✅ calendar.vue - 任务日历（1500+行，完整功能）
2. ✅ board.vue - 任务看板（1100+行，完整功能）
3. ✅ gantt.vue - 甘特图（1100+行，完整功能）
4. ✅ timeline.vue - 任务时间线（1000+行，完整功能）
5. ✅ heatmap.vue - 任务热力图（874行，完整功能）
6. ✅ camera.vue - 拍照创建任务（完整功能，调用API）
7. ✅ ai-classify.vue - AI智能分类（完整功能，调用API）
8. ✅ signature.vue - 电子签名（Canvas绘制功能）
9. ✅ index.vue - 主任务列表
10. ✅ create.vue - 创建任务
11. ✅ detail.vue - 任务详情
12. ✅ schedule.vue - 排班表
13. ✅ reminder.vue - 提醒设置

#### 其他模块 - 保留
- ✅ pages/recipe/ - 智能菜谱（完整功能）
- ✅ pages/food/ - 饮食记录（完整功能）
- ✅ pages/wish/ - 心愿墙（虽有⚠️但功能完整）

### 删除的文件（占位符/冗余）

#### 1. Task模块 - 占位符页面（18个）
| 文件名 | 原因 | 行数 |
|--------|------|------|
| webrtc.vue | WebRTC视频占位，无真实功能 | ~80 |
| ar.vue | AR增强现实占位 | ~100 |
| vr.vue | VR虚拟现实占位 | ~60 |
| 3d.vue | 3D展示占位 | ~60 |
| screenshare.vue | 屏幕共享占位 | ~50 |
| collaborate.vue | 协作编辑占位 | ~70 |
| realtime.vue | 实时同步占位 | ~100 |
| pwa.vue | PWA安装占位 | ~80 |
| i18n.vue | 国际化演示 | ~120 |
| abtest.vue | A/B测试演示，硬编码数据 | ~100 |
| recommend.vue | 智能推荐占位，硬编码数据 | ~80 |
| mood.vue | 心情记录占位，硬编码数据 | ~120 |
| gamification.vue | 积分成就占位，硬编码数据 | ~110 |
| habit.vue | 习惯打卡占位，硬编码数据 | ~100 |
| statistics.vue | 数据统计占位，硬编码数据 | ~80 |
| files.vue | 文件协作占位，硬编码数据 | ~60 |
| personalize.vue | 个性化首页占位 | ~80 |
| version.vue | 版本对比占位，硬编码数据 | ~70 |

#### 2. 重复文件（3个）
| 文件名 | 原因 |
|--------|------|
| family/switch2.vue | switch.vue的升级版，但pages.json使用switch |
| coupon/mycoupons2.vue | mycoupons.vue的重复版本 |
| dashboard/export2.vue | export.vue的重复版本 |

#### 3. Demo文件（2个）
| 文件名 | 原因 |
|--------|------|
| demo/pull-refresh-2.vue | 演示页面 |
| demo/load-more-2.vue | 演示页面 |

#### 4. 框架状态但用户选择保留（4个目录）
- pages/ai/ - AI助手（8个文件）⚠️ 框架状态，但用户选择保留
- pages/ai-shopping/ - AI购物（2个文件）⚠️ 框架状态，但用户选择保留

---

## 删除统计
- 共删除页面: 23个 .vue 文件
- 减少代码行数: 约 3000+ 行
- 清理目录: 保持结构不变，仅删除文件

## 恢复方法
如需恢复，执行:
```bash
cd /Users/qi/family-app-local
git checkout backup/cleanup-$(date +%Y%m%d)
```

## 注意事项
1. 删除后需要更新 pages.json（已检查，不需要修改，因为删除的都是subPackages中未引用或重复的页面）
2. 部分功能如需要真正实现，可以从备份分支参考UI结构重新开发
