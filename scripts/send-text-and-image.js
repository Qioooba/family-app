const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');

const CONFIG = {
  corpId: process.env.WECHAT_WORK_CORPID || '',
  secret: process.env.WECHAT_WORK_SECRET || '',
  agentId: process.env.WECHAT_WORK_AGENTID || ''
};

async function main() {
  if (!CONFIG.corpId || !CONFIG.secret || !CONFIG.agentId) {
    throw new Error('缺少企业微信环境变量');
  }

  console.log('📤 发送修复后的消息（图片能显示）...\n');
  
  const tokenResp = await axios.get('https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=' + CONFIG.corpId + '&corpsecret=' + CONFIG.secret);
  const token = tokenResp.data.access_token;
  
  // 1. 先上传图片
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
  console.log('✅ 图片上传成功, media_id:', mediaId);
  
  // 方式1: 发送文字消息（换行用br标签）
  const timeStr = new Date().toLocaleString('zh-CN');
  const textContent = '🏠 新任务：整理房间\n\n' +
    '📋 任务详情：周末大扫除\n' +
    '📝 备注：请尽快完成\n' +
    '👤 指派人：系统\n' +
    '📅 时间：' + timeStr + '\n\n' +
    '━━━━━━━━━━━━━━━\n\n' +
    '📱 请长按识别下方小程序码，进入小程序查看任务';
  
  // 先发送文字
  const textResp = await axios.post(
    'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=' + token,
    {
      touser: 'QiJun',
      msgtype: 'text',
      agentid: CONFIG.agentId,
      text: { content: textContent }
    }
  );
  
  console.log('文字消息:', textResp.data.errcode === 0 ? '✅ 成功' : '❌ 失败');
  
  // 再发送图片
  const imageResp = await axios.post(
    'https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=' + token,
    {
      touser: 'QiJun',
      msgtype: 'image',
      agentid: CONFIG.agentId,
      image: { media_id: mediaId }
    }
  );
  
  console.log('图片消息:', imageResp.data.errcode === 0 ? '✅ 成功' : '❌ 失败');
  
  console.log('\n💡 发送了2条消息：');
  console.log('1. 文字消息（带换行）');
  console.log('2. 小程序码图片');
}

main().catch(console.error);
