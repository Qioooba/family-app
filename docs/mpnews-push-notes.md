# 提醒推送 - mpnews 公众号文章格式

## 日期
2026-03-18

## 需求
用户希望提醒推送使用**公众号文章样式**，点击后在微信内置页面打开，而非跳转 H5 页面。

## 实现方案

### 消息类型对比

| 类型 | 特点 | 是否需要 media_id | 用户体验 |
|------|------|------------------|----------|
| text | 纯文本 | 否 | ⭐⭐ 一般 |
| news | 图文卡片，跳转外部H5 | 否 | ⭐⭐⭐ 较好 |
| **mpnews** | **微信内置文章页** | **是** | ⭐⭐⭐⭐⭐ **最佳** |
| miniprogram | 小程序卡片 | 否 | ⭐⭐⭐⭐ 好（需配置） |

### 最终实现：mpnews 图文消息

```
企业微信 API: /message/send
消息类型: mpnews
```

**消息结构：**
```json
{
  "touser": "QiJun",
  "agentid": 1000002,
  "msgtype": "mpnews",
  "mpnews": {
    "articles": [
      {
        "title": "✨ 这是标题哦",
        "thumb_media_id": "3Nu4akTcToXY...",
        "author": "家庭小程序",
        "content_source_url": "",
        "content": "<html>微信文章页面内容...</html>",
        "digest": "提醒内容摘要"
      }
    ]
  }
}
```

### 关键代码变更

#### 1. RestTemplate 支持文件上传
```java
// 添加表单转换器（支持 multipart/form-data）
converters.add(new FormHttpMessageConverter());
converters.add(new ResourceHttpMessageConverter());
```

#### 2. 上传封面图获取 media_id
```java
byte[] imageData = readMiniappQrImage();
String mediaId = uploadMiniappQrImage(token, imageData);
```

上传接口：
```
POST https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=xxx&type=image
Content-Type: multipart/form-data

media: miniapp-qr.png
```

#### 3. 构建微信文章页面内容
```java
private String buildReminderMpnewsContent(WechatMessage message, String timeStr) {
    StringBuilder content = new StringBuilder();
    
    // 微信内置页面样式（渐变背景+白色卡片）
    content.append("<div style='font-family: -apple-system...; background: linear-gradient(135deg, #667eea...);'>");
    content.append("<div style='background: #fff; border-radius: 16px; padding: 24px;'>");
    
    // 头部：图标 + 标题 + 时间
    content.append("<div style='text-align: center;'>✨ 提醒详情</div>");
    
    // 提醒内容
    content.append("<div style='...'>").append(escapeHtml(title)).append("</div>");
    
    // 小程序码区域（可长按识别）
    content.append("<img src='https://qioba.cn:8443/miniapp-qr.png' ... />");
    content.append("<div>👆 长按识别小程序码</div>");
    
    return content.toString();
}
```

### 用户体验流程

```
1. 收到推送消息
   ┌─────────────────────────┐
   │    [小程序码封面图]      │
   │                         │
   │  ✨ 这是标题哦           │
   │  2026-03-18 01:58       │
   └─────────────────────────┘
           ↓ 点击
2. 进入微信内置文章页
   ┌─────────────────────────┐
   │  ✨ 提醒详情             │
   │  2026-03-18 01:58       │
   │  ─────────────────────  │
   │  这是标题哦              │
   │                         │
   │  ┌─────────────────┐    │
   │  │   [小程序码]     │    │ ← 长按识别
   │  └─────────────────┘    │
   │  👆 长按识别小程序码     │
   └─────────────────────────┘
           ↓ 长按识别
3. 跳转到小程序
```

## 测试记录

| 时间 | 用户 | 消息类型 | 结果 |
|------|------|---------|------|
| 01:58:00 | 齐老大(userId=7) | mpnews | ✅ 成功 |

日志：
```
上传图片成功, media_id=3Nu4akTcToXY...
提醒mpnews文章发送成功, userId=7, workId=QiJun
```

## 注意事项

1. **media_id 有效期**：3天，过期需要重新上传
2. **图片要求**：
   - 大小：不超过 2MB
   - 格式：jpg/png
   - 建议尺寸：900×500（封面图）
3. **HTML 内容限制**：
   - 支持基本 HTML 标签
   - 图片使用外链 URL
   - 需要转义特殊字符

## 回退机制

如果 mpnews 发送失败（如 media_id 无效），自动回退到 news 图文消息：

```java
if (errcode != 0) {
    log.warn("mpnews发送失败: {}，尝试news方式", result.get("errmsg"));
    sendReminderNewsToInternalUser(token, workUserId, message);
}
```

## Git 提交

```bash
git commit -m "feat: 提醒推送使用 mpnews 公众号文章格式"
git push origin main
```

提交哈希：`6da45fa`
