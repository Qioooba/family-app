# 🔧 修复建议详细方案

**对应问题清单**: ISSUE_LIST.md  
**生成时间**: 2026-02-22

---

## 🔴 P0 修复方案

### FIX-P0-001: 修复前端编译失败

**问题**: uni-app版本不存在

**解决方案**:

1. **修改 frontend/package.json**

```json
{
  "dependencies": {
    "@dcloudio/uni-app": "3.0.0-alpha-4010520240409001",
    "@dcloudio/uni-h5": "3.0.0-alpha-4010520240409001",
    "@dcloudio/uni-mp-weixin": "3.0.0-alpha-4010520240409001"
  },
  "devDependencies": {
    "@dcloudio/vite-plugin-uni": "3.0.0-alpha-4010520240409001"
  }
}
```

2. **或使用最新稳定版**

```json
{
  "dependencies": {
    "@dcloudio/uni-app": "3.0.0-alpha-4020520240719001",
    "@dcloudio/uni-h5": "3.0.0-alpha-4020520240719001",
    "@dcloudio/uni-mp-weixin": "3.0.0-alpha-4020520240719001"
  }
}
```

3. **清除缓存重新安装**

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

---

### FIX-P0-002: CacheAdminController添加权限控制

**问题**: 缓存管理接口无权限控制

**解决方案**:

1. **修改 CacheAdminController.java**

```java
package com.family.common.cache;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache/admin")
@SaCheckLogin  // 添加登录校验
@SaCheckRole("admin")  // 可选：限制管理员角色
public class CacheAdminController {
    
    // 原有代码...
}
```

2. **或根据实际需求配置**

```java
@RestController
@RequestMapping("/api/cache")
public class CacheAdminController {
    
    @SaCheckLogin
    @GetMapping("/stats")
    public Result getCacheStats() {
        // 允许登录用户查看缓存统计
    }
    
    @SaCheckRole("admin")
    @DeleteMapping("/clear")
    public Result clearCache() {
        // 仅管理员可清除缓存
    }
}
```

---

## 🟠 P1 修复方案

### FIX-P1-001: 优化SELECT *查询

**问题**: 34处SELECT * 影响性能

**解决方案**:

1. **示例：FileRecordMapper.java**

修改前:
```java
@Select("SELECT * FROM file_record WHERE file_md5 = #{md5} AND status = 1 LIMIT 1")
FileRecord selectByMd5(@Param("md5") String md5);
```

修改后:
```java
@Select("SELECT id, file_name, file_size, file_path, file_md5, " +
        "folder_id, creator_id, create_time, update_time, status " +
        "FROM file_record WHERE file_md5 = #{md5} AND status = 1 LIMIT 1")
FileRecord selectByMd5(@Param("md5") String md5);
```

2. **使用MyBatis Plus Wrapper**

```java
// 替代SELECT *，只查询需要字段
LambdaQueryWrapper<FileRecord> wrapper = Wrappers.lambdaQuery();
wrapper.select(FileRecord::getId, FileRecord::getFileName, 
               FileRecord::getFilePath, FileRecord::getFileMd5)
       .eq(FileRecord::getFileMd5, md5)
       .eq(FileRecord::getStatus, 1)
       .last("LIMIT 1");
return fileRecordMapper.selectOne(wrapper);
```

3. **批量修复脚本**

```bash
# 查找所有SELECT * 
grep -rn "SELECT \*" backend --include="*.java" | grep -v target
```

---

### FIX-P1-002: 拆分Vue大组件

**问题**: 15个组件超过1000行

**解决方案**:

1. **拆分策略 - 以task/calendar.vue为例**

当前结构:
```
pages/task/calendar.vue (1658行)
```

建议拆分:
```
pages/task/
├── calendar/
│   ├── index.vue          # 主组件 (300行)
│   ├── CalendarGrid.vue   # 日历网格 (300行)
│   ├── TaskList.vue       # 任务列表 (300行)
│   ├── TaskItem.vue       # 任务项 (200行)
│   ├── CalendarHeader.vue # 日历头部 (200行)
│   └── useCalendar.js     # 逻辑抽离 (300行)
```

2. **逻辑抽离示例**

创建 `composables/useCalendar.js`:
```javascript
import { ref, computed } from 'vue';

export function useCalendar() {
  const currentDate = ref(new Date());
  const tasks = ref([]);
  
  const calendarDays = computed(() => {
    // 计算日历天数逻辑
  });
  
  const fetchTasks = async () => {
    // 获取任务逻辑
  };
  
  return {
    currentDate,
    tasks,
    calendarDays,
    fetchTasks
  };
}
```

3. **主组件简化**

```vue
<template>
  <view class="calendar-page">
    <CalendarHeader v-model="currentDate" />
    <CalendarGrid :days="calendarDays" @select="onDaySelect" />
    <TaskList :tasks="currentTasks" @refresh="fetchTasks" />
  </view>
</template>

<script setup>
import { useCalendar } from './composables/useCalendar';
import CalendarHeader from './components/CalendarHeader.vue';
import CalendarGrid from './components/CalendarGrid.vue';
import TaskList from './components/TaskList.vue';

const { currentDate, calendarDays, currentTasks, fetchTasks } = useCalendar();
</script>
```

4. **优先级拆分顺序**

| 优先级 | 文件 | 行数 | 预计拆分数 |
|--------|------|------|-----------|
| 1 | task/calendar.vue | 1658 | 5个组件 |
| 2 | coupon/manage.vue | 1447 | 4个组件 |
| 3 | task/detail.vue | 1239 | 4个组件 |
| 4 | shopping/price.vue | 1228 | 3个组件 |

---

### FIX-P1-003: 修复JDK编译警告

**问题**: 未与-source 17一起设置系统模块位置

**解决方案**:

1. **修改根pom.xml**

```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>17</release>  <!-- 替代source和target -->
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

2. **验证修复**

```bash
cd backend
mvn clean compile
# 警告应该消失
```

---

## 🟡 P2 修复方案

### FIX-P2-001: 添加单元测试

**问题**: 无单元测试

**解决方案**:

1. **添加测试依赖到pom.xml**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

2. **创建测试类示例**

```java
// user-service/src/test/java/com/family/user/service/UserServiceTest.java
package com.family.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testRegister() {
        // 测试注册逻辑
    }
    
    @Test
    void testLogin() {
        // 测试登录逻辑
    }
}
```

3. **测试目录结构**

```
backend/
├── family-service/user-service/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   │       └── java/
│   │           └── com/family/user/
│   │               ├── service/
│   │               │   └── UserServiceTest.java
│   │               └── controller/
│   │                   └── UserControllerTest.java
```

---

### FIX-P2-002: 修复WebSocket类型警告

**问题**: MessageWebSocketHandler使用了未经检查的操作

**解决方案**:

1. **添加泛型类型**

```java
// 修改前
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    Map data = JSON.parseObject(message.getPayload());
}

// 修改后
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    Map<String, Object> data = JSON.parseObject(message.getPayload(), 
                                                 new TypeReference<Map<String, Object>>() {});
}
```

2. **或添加@SuppressWarnings**

```java
@SuppressWarnings("unchecked")
@Override
protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    // 原有代码
}
```

---

## 🔵 P3 修复方案

### FIX-P3-001: 清理配置文件

**问题**: target目录中的配置文件与源码重复

**解决方案**:

1. **更新.gitignore**

```gitignore
# 已有配置，确保target被忽略
target/
*.class
*.jar
```

2. **清理已提交的target目录**

```bash
# 从git中删除target目录（但保留本地文件）
git rm -r --cached backend/**/target

# 提交更改
git commit -m "chore: 清理target目录"
```

---

## 🚀 修复执行计划

### 第一阶段（立即执行）
1. FIX-P0-001: 修复前端版本
2. FIX-P0-002: 添加权限控制

### 第二阶段（本周完成）
3. FIX-P1-003: 修复JDK警告
4. FIX-P1-001: 优化主要SELECT *查询（前10个）

### 第三阶段（本月完成）
5. FIX-P1-002: 拆分大组件（前5个）
6. FIX-P2-002: 修复WebSocket警告

### 第四阶段（下月规划）
7. FIX-P2-001: 搭建单元测试框架
8. FIX-P1-001: 完成所有SELECT *优化
9. FIX-P1-002: 完成所有组件拆分

---

## 📋 修复检查清单

- [ ] 前端package.json版本已更新
- [ ] 前端npm install成功
- [ ] CacheAdminController已添加@SaCheckLogin
- [ ] 根pom.xml已使用<release>17</release>
- [ ] 后端编译无警告
- [ ] FileRecordMapper已优化SELECT *
- [ ] task/calendar.vue已拆分
- [ ] WebSocket警告已修复
- [ ] 单元测试框架已搭建

---

*报告生成时间: 2026-02-22*
