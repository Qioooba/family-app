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

  console.log('📤 发送图文消息（修正版）...\n');
  
  const tokenResp = await axios.get('https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=' + CONFIG.corpId + '&corpsecret=' + CONFIG.secret);
  const token = tokenResp.data.access_token;
  
  // 上传图片
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
  
  // 上传图片到企业微信素材库（用于正文中的图片）
  const form2 = new FormData();
  form2.append('media', imageData, {
    filename: 'miniapp-qr-content.png',
    contentType: 'image/png'
  });
  
  const uploadResp2 = await axios.post(
    'https://qyapi.weixin.qq.com/cgi-bin/media/upload?access_token=' + token + '&type=image',
    form2,
    { headers: form2.getHeaders() }
  );
  
  const contentMediaId = uploadResp2.data.media_id;
  console.log('✅ 正文图片上传成功');
  
  const timeStr = new Date().toLocaleString('zh-CN');
  
  // 使用正确的HTML格式，图片用data-src引用media_id
  const content = '<p><strong>📋 任务详情：</strong>周末大扫除</p>' +
    '<p><strong>📝 备注：</strong>请尽快完成</p>' +
    '<p><strong>👤 指派人：</strong>系统</p>' +
    '<p><strong>📅 时间：</strong>' + timeStr + '</p>' +
    '<hr/>' +
    '<p><strong>📱 请长按识别下方小程序码</strong></p>' +
    '<p><img src="https://qyapi.weixin.qq.com/cgi-bin/media/get?access_token=' + token + '&media_id=' + contentMediaId + '" width="280"/></p>' +
    '<p>👉 扫码后进入小程序查看任务</p>';
  
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
          content_source_url: CONFIG.baseUrl + '/jump-mp/index.html',
          content: content,
          digest: '📋 任务：周末大扫除 \n📝 备注：请尽快完成 \n👤 指派人：系统 \n📱 请长按小程序码查看'
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
    console.log('✅ 图文消息发送成功！');
  } else {
    console.log('❌ 失败:', sendResp.data.errmsg);
  }
}

main().catch(console.error);
