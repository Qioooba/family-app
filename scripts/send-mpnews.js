const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');

const CONFIG = {
  corpId: process.env.WECHAT_WORK_CORPID || '',
  secret: process.env.WECHAT_WORK_SECRET || '',
  agentId: process.env.WECHAT_WORK_AGENTID || '',
  baseUrl: process.env.APP_BASE_URL || 'http://localhost:8443'
};

async function main() {
  if (!CONFIG.corpId || !CONFIG.secret || !CONFIG.agentId) {
    throw new Error('缺少企业微信环境变量');
  }

  console.log('📤 发送合并的图文消息给齐军...\n');
  
  // 1. 获取token
  const tokenResp = await axios.get('https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=' + CONFIG.corpId + '&corpsecret=' + CONFIG.secret);
  const token = tokenResp.data.access_token;
  
  // 2. 上传图片
  const imagePath = '/Users/qi/.openclaw/workspace/family-app/docs/miniapp-qr.png';
  const imageData = fs.readFileSync(imagePath);
  
  const form = new FormData();
  form.append('media', imageData, {
    filename: 'miniapp-qr.png',
    contentType: 'image/png'
  });
  
  const uploadResp = await axios.post(
    'https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=' + token + '&type=image',
    form,
    { headers: form.getHeaders() }
  );
  
  const mediaId = uploadResp.data.media_id;
  console.log('✅ 图片上传成功');
  
  // 构建图文内容
  const timeStr = new Date().toLocaleString('zh-CN');
  const digest = '📋 任务：周末大扫除\n📝 备注：请尽快完成\n👤 指派人：系统\n📅 时间：' + timeStr + '\n\n📱 请长按小程序码进入小程序查看';
  
  const content = '<p><strong>📋 任务详情：</strong>周末大扫除</p>' +
    '<p><strong>📝 备注：</strong>请尽快完成</p>' +
    '<p><strong>👤 指派人：</strong>系统</p>' +
    '<p><strong>📅 时间：</strong>' + timeStr + '</p>' +
    '<p>━━━━━━━━━━━━━━━</p>' +
    '<p><strong>📱 请长按下方小程序码，识别后进入小程序查看任务</strong></p>' +
    '<p><img src="data:image/png;base64,' + imageData.toString('base64') + '"/></p>';
  
  // 3. 发送图文消息（文字+图片一起）
  const payload = {
    touser: 'QiJun',
    msgtype: 'mpnews',
    agentid: CONFIG.agentId,
    mpnews: {
      articles: [
        {
          title: '🏠 新任务：整理房间（请扫码查看）',
          thumb_media_id: mediaId,
          author: '家庭小程序',
          content_source_url: CONFIG.baseUrl,
          content: content,
          digest: digest
        }
      ]
    }
  };
  
  const sendResp = await axios.post(
    'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=' + token,
    payload
  );
  
  console.log('响应:', JSON.stringify(sendResp.data));
  
  if (sendResp.data.errcode === 0) {
    console.log('✅ 合并消息发送成功！齐军收到一条包含文字+图片的图文消息！');
  } else {
    console.log('❌ 失败:', sendResp.data.errmsg);
  }
}

main().catch(console.error);
