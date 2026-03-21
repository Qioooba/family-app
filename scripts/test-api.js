#!/usr/bin/env node
/**
 * 尝试不同的API获取外部联系人
 */

const axios = require('axios');
const mysql = require('mysql2/promise');

const CONFIG = {
  corpId: process.env.WECHAT_WORK_CORPID || '',
  secret: process.env.WECHAT_WORK_SECRET || '',
  agentId: process.env.WECHAT_WORK_AGENTID || ''
};

const DB_CONFIG = {
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: process.env.MYSQL_PASSWORD || '',
  database: 'family_app'
};

async function getAccessToken() {
  if (!CONFIG.corpId || !CONFIG.secret) {
    throw new Error('缺少 WECHAT_WORK_CORPID / WECHAT_WORK_SECRET 环境变量');
  }

  const url = `https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CONFIG.corpId}&corpsecret=${CONFIG.secret}`;
  const resp = await axios.get(url);
  if (resp.data.access_token) {
    return resp.data.access_token;
  }
  throw new Error(resp.data.errmsg);
}

async function testAPI(token, name, url) {
  try {
    const resp = await axios.get(url);
    console.log(`${name}:`, resp.data.errcode === 0 ? '✅ 成功' : `❌ ${resp.data.errmsg}`);
    if (resp.data.errcode === 0) {
      console.log('  数据:', JSON.stringify(resp.data, null, 2).substring(0, 500));
      return resp.data;
    }
  } catch (e) {
    console.log(`${name}: ❌ ${e.response?.data?.errmsg || e.message}`);
  }
  return null;
}

async function main() {
  console.log('🔍 测试各种企业微信API...\n');
  
  const token = await getAccessToken();
  console.log('✅ 获取token成功\n');
  
  // 测试1: 获取配置了客户联系功能的成员列表
  console.log('测试1: 获取配置了客户联系功能的成员列表');
  await testAPI(
    token,
    'get_follow_user_list',
    `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get_follow_user_list?access_token=${token}`
  );
  
  console.log('\n测试2: 通过手机号获取userid');
  // 需要CorpSecret是企业微信的通讯录同步助手Secret，不是应用Secret
  // 这里只是测试接口
  
  console.log('\n测试3: 获取部门成员');
  const deptResult = await testAPI(
    token,
    'user/simplelist',
    `https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=${token}&department_id=1&fetch_child=1`
  );
  
  if (deptResult && deptResult.userlist) {
    console.log('\n📋 企业成员列表:');
    deptResult.userlist.forEach(u => {
      console.log(`  - ${u.name}: ${u.userid}`);
    });
    
    // 如果找到小助手，更新配置
    const xiaozhu = deptResult.userlist.find(u => u.name.includes('小助手') || u.userid.toLowerCase().includes('xiao'));
    if (xiaozhu) {
      console.log(`\n✅ 找到小助手: ${xiaozhu.userid}`);
      
      // 更新数据库
      const conn = await mysql.createConnection(DB_CONFIG);
      await conn.execute(
        "UPDATE sys_config SET config_value = ? WHERE config_key = 'wechat.work.userid'",
        [xiaozhu.userid]
      );
      console.log('✅ 已更新数据库配置');
      await conn.end();
      
      // 再试一次获取客户列表
      console.log('\n🔄 用正确的userid重新获取客户列表...');
      await testAPI(
        token,
        'externalcontact/list',
        `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=${token}&userid=${xiaozhu.userid}`
      );
    }
  }
}

main().catch(console.error);
