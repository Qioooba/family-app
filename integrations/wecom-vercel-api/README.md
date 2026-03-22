# 企业微信验证服务

用于企业微信的可信域名验证和接收消息回调。

## 文件说明

- `WW_verify_usnED0E2gQ05Zf5t.txt` - 企业微信域名验证文件
- `api/wechat/callback.js` - 处理企业微信回调的API
- `vercel.json` - Vercel部署配置
- `package.json` - 依赖配置

## 部署步骤

### 1. 推送到GitHub

```bash
git init
git add .
git commit -m "init"
git remote add origin https://github.com/你的用户名/wecom-vercel-api.git
git push -u origin main
```

### 2. 部署到Vercel

1. 访问 https://vercel.com
2. 用GitHub登录
3. 导入此仓库
4. 点击 Deploy
5. 获得域名：`https://xxx.vercel.app`

### 3. 配置环境变量

在Vercel控制台 → Project Settings → Environment Variables 添加：

| 变量名 | 值 |
|--------|-----|
| TOKEN | your-wecom-token |
| ENCODING_AES_KEY | your-43-char-aes-key |
| CORPID | your-wecom-corp-id |

### 4. 配置企业微信

- 可信域名：`xxx.vercel.app`
- 接收消息URL：`https://xxx.vercel.app/api/wechat/callback`
- Token：`your-wecom-token`
- EncodingAESKey：`your-43-char-aes-key`

## 验证地址

- 验证文件：`https://xxx.vercel.app/WW_verify_usnED0E2gQ05Zf5t.txt`
- 回调接口：`https://xxx.vercel.app/api/wechat/callback`
