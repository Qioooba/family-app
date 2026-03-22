// Vercel API Route - 企业微信验证和回调处理
// 部署后访问: https://your-app.vercel.app/api/wechat/callback

const crypto = require('crypto');

// 从环境变量读取配置
const TOKEN = process.env.TOKEN;
const AES_KEY = process.env.ENCODING_AES_KEY;
const CORP_ID = process.env.CORPID;

/**
 * 计算SHA1签名
 */
function sha1(str) {
  return crypto.createHash('sha1').update(str).digest('hex');
}

/**
 * 验证企业微信签名
 */
function verifySignature(token, timestamp, nonce, encrypt, signature) {
  const arr = [token, timestamp, nonce, encrypt].sort();
  const str = arr.join('');
  const calculated = sha1(str);
  return calculated === signature;
}

/**
 * AES-256-CBC解密
 */
function aesDecrypt(encrypted, aesKey) {
  const key = Buffer.from(aesKey + '=', 'base64');
  const iv = key.slice(0, 16);
  const encryptedBuffer = Buffer.from(encrypted, 'base64');
  
  const decipher = crypto.createDecipheriv('aes-256-cbc', key, iv);
  decipher.setAutoPadding(false);
  
  let decrypted = decipher.update(encryptedBuffer);
  decrypted = Buffer.concat([decrypted, decipher.final()]);
  
  return decrypted;
}

/**
 * 解密企业微信消息
 */
function decryptMsg(encryptedMsg) {
  const aesKey = AES_KEY;
  const decrypted = aesDecrypt(encryptedMsg, aesKey);
  
  // 去除PKCS7填充
  const padLen = decrypted[decrypted.length - 1];
  const unpadded = decrypted.slice(0, decrypted.length - padLen);
  
  // 解析结构: random(16B) + msg_len(4B) + msg + receiveid
  const msgLen = unpadded.readUInt32BE(16);
  const msg = unpadded.slice(20, 20 + msgLen).toString('utf8');
  const receiveId = unpadded.slice(20 + msgLen).toString('utf8');
  
  if (receiveId !== CORP_ID) {
    throw new Error('receiveId不匹配');
  }
  
  return msg;
}

module.exports = (req, res) => {
  // 设置CORS头
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST');
  
  const { msg_signature, timestamp, nonce, echostr } = req.query;

  if (!TOKEN || !AES_KEY || !CORP_ID) {
    return res.status(500).send('missing wecom env');
  }
  
  console.log('收到请求:', { method: req.method, query: req.query });
  
  // GET 请求 - URL验证
  if (req.method === 'GET' && echostr) {
    try {
      console.log('URL验证请求:', { msg_signature, timestamp, nonce, echostr });
      
      // 1. 验证签名
      const valid = verifySignature(TOKEN, timestamp, nonce, echostr, msg_signature);
      if (!valid) {
        console.log('签名验证失败');
        return res.status(403).send('fail');
      }
      
      // 2. 解密echostr
      const decrypted = decryptMsg(echostr);
      console.log('解密成功:', decrypted);
      
      // 3. 返回明文（不能加引号，不能有换行）
      return res.status(200).send(decrypted);
      
    } catch (error) {
      console.error('URL验证失败:', error);
      return res.status(500).send('fail');
    }
  }
  
  // POST 请求 - 接收消息
  if (req.method === 'POST') {
    console.log('收到消息:', req.body);
    return res.status(200).send('success');
  }
  
  // 其他请求
  res.status(200).send('Hello WeCom!');
};
