-- 获取企业微信外部联系人ID的API调用示例
-- 需要先获取access_token，然后调用客户列表接口

-- 步骤1：获取access_token
-- GET https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=YOUR_CORP_ID&corpsecret=YOUR_SECRET

-- 步骤2：获取小助手的客户列表
-- GET https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=TOKEN&userid=XIAOXHUSHOU

-- 返回示例：
-- {
--   "errcode": 0,
--   "errmsg": "ok",
--   "external_userid": ["wmXxxxxxxxxxx1", "wmXxxxxxxxxxx2"]
-- }

-- 步骤3：获取客户详情（才能知道哪个ID对应齐军/陶陶）
-- GET https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get?access_token=TOKEN&external_userid=wmXxxxxxxxxxx1

-- 返回示例：
-- {
--   "errcode": 0,
--   "errmsg": "ok",
--   "external_contact": {
--     "external_userid": "wmXxxxxxxxxxx1",
--     "name": "齐军",
--     "type": 1,  -- 1表示微信用户
--     "avatar": "http://xxx.jpg"
--   }
-- }
