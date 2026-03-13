#!/usr/bin/env node
/**
 * 获取企业微信客户列表工具
 * 用法: node get-customers.js
 */

const axios = require('axios');

// 企业微信配置 - 需要填你的真实配置
const CORP_ID = process.env.CORPID || '你的企业ID';
const SECRET = process.env.SECRET || '你的Secret';
const USER_ID = 'XIAOXHUSHOU';  // 小助手的userid

// 缓存token
let accessToken = null;
let tokenExpireTime = 0;

/**
 * 获取access_token
 */
async function getAccessToken() {
  if (accessToken && Date.now() < tokenExpireTime) {
    return accessToken;
  }

  const url = `https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CORP_ID}&corpsecret=${SECRET}`;
  const resp = await axios.get(url);
  
  if (resp.data.access_token) {
    accessToken = resp.data.access_token;
    tokenExpireTime = Date.now() + (7200 - 300) * 1000;
    return accessToken;
  }
  throw new Error('获取token失败: ' + resp.data.errmsg);
}

/**
 * 获取客户列表
 */
async function getCustomerList() {
  const token = await getAccessToken();
  const url = `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=${token}&userid=${USER_ID}`;
  
  const resp = await axios.get(url);
  
  if (resp.data.errcode !== 0) {
    throw new Error('获取客户列表失败: ' + resp.data.errmsg);
  }
  
  return resp.data.external_userid || [];
}

/**
 * 获取客户详情
 */
async function getCustomerDetail(externalUserId) {
  const token = await getAccessToken();
  const url = `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get?access_token=${token}&external_userid=${externalUserId}`;
  
  const resp = await axios.get(url);
  
  if (resp.data.errcode !== 0) {
    throw new Error('获取客户详情失败: ' + resp.data.errmsg);
  }
  
  return resp.data.external_contact;
}

/**
 * 主函数
 */
async function main() {
  try {
    console.log('🔍 正在获取小助手的客户列表...\n');
    
    const customerIds = await getCustomerList();
    console.log(`✅ 找到 ${customerIds.length} 个客户:\n`);
    
    for (const externalUserId of customerIds) {
      try {
        const detail = await getCustomerDetail(externalUserId);
        console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
        console.log(`👤 名称: ${detail.name || '未设置'}`);
        console.log(`🆔 ExternalUserId: ${externalUserId}`);
        console.log(`📱 类型: ${detail.type === 1 ? '微信用户' : '企业微信用户'}`);
        console.log(`🖼️ 头像: ${detail.avatar || '无'}`);
        console.log('');
      } catch (e) {
        console.log(`❌ 获取 ${externalUserId} 详情失败: ${e.message}`);
      }
    }
    
    console.log('\n📋 生成的SQL更新语句:');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    // 这里需要根据实际返回生成SQL
    
  } catch (error) {
    console.error('❌ 错误:', error.message);
    console.log('\n💡 请检查:');
    console.log('   1. CORP_ID 和 SECRET 是否正确');
    console.log('   2. 小助手是否有"客户联系"权限');
    console.log('   3. 网络是否正常');
  }
}

// 运行
main();
