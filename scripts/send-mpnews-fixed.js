const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');

const CONFIG = {
  corpId: process.env.WECHAT_WORK_CORPID || '',
  secret: process.env.WECHAT_WORK_SECRET || '',
  agentId: process.env.WECHAT_WORK_AGENTID || '',
  baseUrl: process.env.APP_BASE_URL || 'http://localhost:8443',
  qrImagePath: process.env.MINIAPP_QR_IMAGE_PATH || process.env.QR_IMAGE_PATH || 'docs/miniapp-qr.png'
};

async function main() {
  if (!CONFIG.corpId || !CONFIG.secret || !CONFIG.agentId) {
    throw new Error('缺少企业微信环境变量');
  }

  console.log('📤 发送修复后的图文消息...\n');
  
  const tokenResp = await axios.get('https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=' + CONFIG.corpId + '&corpsecret=' + CONFIG.secret);
  const token = tokenResp.data.access_token;
  
  // 上传图片
  const imagePath = CONFIG.qrImagePath;
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
  
  // 构建内容 - 使用图片URL而不是base64
  const timeStr = new Date().toLocaleString('zh-CN');
  const digest = '📋 任务：周末大扫除 | 📝 备注：请尽快完成 | 👤 指派人：系统';
  
  // 内容中使用media_id引用图片
  const content = '<p><strong>📋 任务详情：</strong>周末大扫除</p>' +
    '<p><strong>📝 备注：</strong>请尽快完成</p>' +
    '<p><strong>👤 指派人：</strong>系统</p>' +
    '<p><strong>📅 时间：</strong>' + timeStr + '</p>' +
    '<p>━━━━━━━━━━━━━━━</p>' +
    '<p><strong>📱 请保存下方小程序码，微信扫码进入小程序</strong></p>' +
    '<p><img src=\"' + CONFIG.baseUrl + '/miniapp-qr.png\" width=\"280\"/></p>' +
    '<p><strong>👉 扫码后直接打开小程序首页</strong></p>';
  
  // 发送图文消息 - 使用正确的链接
  const payload = {
    touser: 'QiJun',
    msgtype: 'mpnews',
    agentid: CONFIG.agentId,
    mpnews: {
      articles: [
        {
          title: '🏠 新任务：整理房间',
          thumb_media_id: mediaId,
          author: '家庭小程序',
          content_source_url: CONFIG.baseUrl + '/jump-mp/index.html',
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
    console.log('✅ 修复后的消息已发送！');
    console.log('图片使用URL引用，点击阅读原文跳转到正确的页面！');
  } else {
    console.log('❌ 失败:', sendResp.data.errmsg);
  }
}

main().catch(console.error);
