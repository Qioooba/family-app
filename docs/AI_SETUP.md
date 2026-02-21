# 🤖 AI助手配置说明

## 配置 DeepSeek API Key

在运行 AI 服务之前，需要配置 DeepSeek API Key。

### 方式一：环境变量（推荐）

```bash
export DEEPSEEK_API_KEY=your-deepseek-api-key
```

### 方式二：修改配置文件

编辑 `backend/family-service/ai-service/src/main/resources/application.yml`：

```yaml
ai:
  deepseek:
    api-key: your-deepseek-api-key
    api-url: https://api.deepseek.com/v1/chat/completions
    model: deepseek-chat
```

## 获取 DeepSeek API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com)
2. 注册/登录账号
3. 创建 API Key
4. 复制 Key 到配置中

## 启动 AI 服务

```bash
cd backend/family-service/ai-service
mvn spring-boot:run
```

服务启动后访问：http://localhost:8090

## API 接口

### AI 对话
```http
POST /api/ai/chat
Content-Type: application/json

{
  "message": "今晚吃什么？",
  "familyId": 1,
  "sessionId": "可选，不传则创建新会话"
}
```

### 获取早安日报
```http
GET /api/ai/daily-report/morning?userName=张三
```

### 菜谱推荐
```http
POST /api/ai/recipe/recommend
Content-Type: application/json

{
  "ingredients": "土豆、牛肉、番茄",
  "preference": "偏清淡"
}
```

## 功能特性

- ✅ 支持上下文对话
- ✅ 自动保存对话历史
- ✅ 会话隔离（不同用户不同会话）
- ✅ 菜谱推荐
- ✅ 营养分析
- ✅ 早安/晚安日报

## 注意事项

1. API Key 不要提交到 Git 仓库
2. 生产环境建议使用环境变量配置
3. 首次调用可能需要等待模型加载
