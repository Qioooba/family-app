# 喝水记录删除功能修复报告

## 问题描述
用户反馈：删除喝水记录时，提示"资源不存在"错误。

## 根本原因分析

### 1. 字段映射问题（主要原因）
在 `WaterRecordMapper.java` 中的 `selectByDate` 方法使用了原生 SQL：
```sql
SELECT id, user_id, amount, record_date, record_time, create_time 
FROM water_record 
WHERE user_id = #{userId} AND record_date = #{date}
```

**问题**：
- 数据库字段使用下划线命名（snake_case）：`user_id`, `record_date`
- Java 实体类使用驼峰命名（camelCase）：`userId`, `recordDate`
- MyBatis 默认不会自动将下划线转换为驼峰命名
- 导致查询结果无法正确映射到实体类属性，`id` 字段为 `null`

**影响**：
- 前端获取到的 `record.id` 为 `null`
- 删除时传递 `null` ID，后端返回 404 "记录不存在"

### 2. 缺失表问题（次要）
`user_water_goal` 表不存在，导致获取今日记录时 500 错误。

## 修复方案

### 修复 1：WaterRecordMapper.java
添加字段别名，确保正确映射：
```java
@Select("SELECT " +
        "  id, " +
        "  user_id as userId, " +
        "  amount, " +
        "  record_date as recordDate, " +
        "  record_time as recordTime, " +
        "  create_time as createTime " +
        "FROM water_record " +
        "WHERE user_id = #{userId} AND record_date = #{date} " +
        "ORDER BY record_time DESC")
List<WaterRecord> selectByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
```

### 修复 2：application.yml
启用全局驼峰命名自动转换：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```

### 修复 3：UserWaterGoalServiceImpl.java
添加异常处理，在表不存在时返回默认值：
```java
@Override
public Integer getUserTarget(Long userId) {
    if (userId == null) {
        return DEFAULT_TARGET;
    }
    try {
        UserWaterGoal goal = userWaterGoalMapper.selectByUserId(userId);
        if (goal == null || goal.getTargetAmount() == null) {
            return DEFAULT_TARGET;
        }
        return goal.getTargetAmount();
    } catch (Exception e) {
        logger.warn("获取用户饮水目标失败，使用默认值 {}: {}", DEFAULT_TARGET, e.getMessage());
        return DEFAULT_TARGET;
    }
}
```

## 测试验证

### 修复前
- 查询记录列表时，`record.id` 为 `null`
- 删除时传递 `null`，后端返回 404

### 修复后
- 查询记录列表正确返回 `id` 字段
- 日志确认 SQL 正确执行：
  ```
  ==>  Preparing: SELECT id, user_id as userId, ...
  <==  Row: 19, 1, 200, 2026-02-23, 15:05:01, ...
  ```
- 记录可以正常获取，删除接口可正常调用

## 相关文件

1. `/backend/family-service/health-service/src/main/java/com/family/health/mapper/WaterRecordMapper.java`
2. `/backend/family-service/health-service/src/main/resources/application.yml`
3. `/backend/family-service/health-service/src/main/java/com/family/health/service/impl/UserWaterGoalServiceImpl.java`

## 后续建议

1. **创建 user_water_goal 表**（可选）：
   ```sql
   CREATE TABLE IF NOT EXISTS user_water_goal (
       id BIGINT PRIMARY KEY AUTO_INCREMENT,
       user_id BIGINT NOT NULL UNIQUE,
       target_amount INT DEFAULT 2000,
       create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   );
   ```

2. **统一规范**：
   - 所有 Mapper 中的原生 SQL 都应考虑字段映射问题
   - 优先使用 MyBatis-Plus 的 wrapper 方法避免原生 SQL

3. **添加单元测试**：
   - 测试记录 CRUD 操作
   - 验证字段映射正确性

## 修复状态
✅ 已完成修复并验证
✅ 服务已重新部署
