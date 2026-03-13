# 企业微信API调试步骤

## 步骤1：获取 Access Token

在浏览器中访问以下URL：
```
https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ww6c1c7590db91ef85&corpsecret=Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4
```

复制返回的 `access_token` 值。

## 步骤2：打开官方调试工具

访问：https://developer.work.weixin.qq.com/resource/devtool/devtool.html

## 步骤3：测试获取客户列表

1. 选择接口：**获取客户列表** (`/externalcontact/list`)
2. 填入参数：
   - **access_token**: 步骤1获取的值
   - **userid**: `XIAOZHUSHOU`
3. 点击 **发送请求**

## 步骤4：查看结果

### 如果成功：
```json
{
  "errcode": 0,
  "errmsg": "ok",
  "external_userid": ["wmXxxxxx", "wmXxxxxx"]
}
```
把返回的 `external_userid` 列表复制给我。

### 如果失败：
```json
{
  "errcode": 48002,
  "errmsg": "api forbidden"
}
```
说明确实没有权限，需要检查企业微信后台配置。

## 备选：获取客户详情

如果有客户ID，可以测试获取详情：
- 接口：**获取客户详情** (`/externalcontact/get`)
- 参数：
  - access_token: 同上
  - external_userid: 从其他途径获取的ID

## 当前已知的ID

从浏览器开发者工具中找到的齐军ID：
- `7616550953818259457`

但格式可能不对，标准应该是 `wm` 开头。

请用官方工具测试后，把结果截图或复制给我！
