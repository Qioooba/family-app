#!/usr/bin/env node
/**
 * 自动获取企业微信外部联系人ID并更新数据库
 * 用法: 
 *   export CORPID="你的企业ID"
 *   export SECRET="你的Secret"  
 *   export MYSQL_PASSWORD="你的数据库密码"
 *   node sync-wechat-customers.js
 */

const axios = require('axios');
const mysql = require('mysql2/promise');

// 配置
const CONFIG = {
  corpId: process.env.CORPID,
  secret: process.env.SECRET,
  userId: 'XIAOXHUSHOU',  // 小助手的userid
  
  // 数据库配置
  db: {
    host: 'localhost',
    port: 3306,
    user: 'root',
    password: process.env.MYSQL_PASSWORD || 'your_secure_mysql_password',
    database: 'family_app'
  }
};

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

  const url = `https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CONFIG.corpId}&corpsecret=${CONFIG.secret}`;
  const resp = await axios.get(url);
  
  if (resp.data.access_token) {
    accessToken = resp.data.access_token;
    tokenExpireTime = Date.now() + (7200 - 300) * 1000;
    console.log('✅ 获取access_token成功');
    return accessToken;
  }
  throw new Error('获取token失败: ' + JSON.stringify(resp.data));
}

/**
 * 获取客户列表
 */
async function getCustomerList() {
  const token = await getAccessToken();
  const url = `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/list?access_token=${token}&userid=${CONFIG.userId}`;
  
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
 * 连接数据库
 */
async function connectDB() {
  return mysql.createConnection(CONFIG.db);
}

/**
 * 查询数据库用户
 */
async function getDBUsers(connection) {
  const [rows] = await connection.execute(
    'SELECT id, username, nickname, work_user_id, external_user_id FROM sys_user WHERE status = 1'
  );
  return rows;
}

/**
 * 更新用户external_user_id
 */
async function updateExternalUserId(connection, userId, externalUserId) {
  await connection.execute(
    'UPDATE sys_user SET external_user_id = ? WHERE id = ?',
    [externalUserId, userId]
  );
}

/**
 * 主函数
 */
async function main() {
  console.log('🚀 开始同步企业微信外部联系人...\n');
  
  // 检查配置
  if (!CONFIG.corpId || CONFIG.corpId === '你的企业ID') {
    console.error('❌ 错误: 请设置环境变量 CORPID');
    console.log('   export CORPID="你的企业ID"');
    process.exit(1);
  }
  
  if (!CONFIG.secret || CONFIG.secret === '你的Secret') {
    console.error('❌ 错误: 请设置环境变量 SECRET');
    console.log('   export SECRET="你的Secret"');
    process.exit(1);
  }
  
  let connection;
  
  try {
    // 1. 连接数据库
    console.log('📡 连接数据库...');
    connection = await connectDB();
    console.log('✅ 数据库连接成功\n');
    
    // 2. 获取数据库用户
    console.log('👥 查询数据库用户...');
    const dbUsers = await getDBUsers(connection);
    console.log(`✅ 找到 ${dbUsers.length} 个用户:\n`);
    
    dbUsers.forEach(u => {
      console.log(`   ID: ${u.id}, 用户名: ${u.username}, 昵称: ${u.nickname || '无'}`);
      console.log(`   - 内部成员ID: ${u.work_user_id || '未设置'}`);
      console.log(`   - 外部联系人ID: ${u.external_user_id || '未设置'}`);
      console.log('');
    });
    
    // 3. 获取企业微信客户列表
    console.log('📱 获取企业微信客户列表...');
    const customerIds = await getCustomerList();
    console.log(`✅ 找到 ${customerIds.length} 个客户\n`);
    
    if (customerIds.length === 0) {
      console.log('⚠️ 小助手没有客户，请先让客户扫码添加');
      return;
    }
    
    // 4. 获取客户详情
    console.log('🔍 获取客户详情...\n');
    const customers = [];
    
    for (const externalUserId of customerIds) {
      try {
        const detail = await getCustomerDetail(externalUserId);
        customers.push({
          externalUserId: externalUserId,
          name: detail.name,
          type: detail.type,
          avatar: detail.avatar
        });
        console.log(`   👤 ${detail.name || '未命名'}: ${externalUserId}`);
      } catch (e) {
        console.log(`   ❌ 获取 ${externalUserId} 失败: ${e.message}`);
      }
    }
    
    console.log('\n📋 匹配并更新数据库...\n');
    
    // 5. 匹配并更新
    let updatedCount = 0;
    
    for (const customer of customers) {
      const customerName = customer.name || '';
      
      // 尝试匹配用户（通过昵称或用户名）
      const matchedUser = dbUsers.find(u => {
        const nickname = (u.nickname || '').toLowerCase();
        const username = (u.username || '').toLowerCase();
        const cName = customerName.toLowerCase();
        
        return nickname === cName || 
               username === cName ||
               (cName.includes('齐') && (nickname.includes('齐') || username.includes('qi'))) ||
               (cName.includes('陶') && (nickname.includes('陶') || username.includes('tao')));
      });
      
      if (matchedUser) {
        console.log(`✅ 匹配成功: ${customerName} -> 用户ID ${matchedUser.id} (${matchedUser.username})`);
        
        if (matchedUser.external_user_id === customer.externalUserId) {
          console.log('   ℹ️ 外部联系人ID已是最新，无需更新');
        } else {
          await updateExternalUserId(connection, matchedUser.id, customer.externalUserId);
          console.log(`   📝 已更新 external_user_id: ${customer.externalUserId}`);
          updatedCount++;
        }
      } else {
        console.log(`⚠️ 未匹配: ${customerName} (${customer.externalUserId})`);
        console.log('   请在数据库中手动配置该用户的昵称或用户名');
      }
      console.log('');
    }
    
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`✅ 同步完成！更新了 ${updatedCount} 个用户`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
    
    // 6. 显示最终结果
    console.log('📊 最终用户状态:');
    const finalUsers = await getDBUsers(connection);
    finalUsers.forEach(u => {
      const hasWork = u.work_user_id ? '✅' : '❌';
      const hasExt = u.external_user_id ? '✅' : '❌';
      console.log(`   ${u.nickname || u.username}: 内部${hasWork} 外部${hasExt}`);
    });
    
  } catch (error) {
    console.error('\n❌ 错误:', error.message);
    console.error(error.stack);
  } finally {
    if (connection) {
      await connection.end();
      console.log('\n📡 数据库连接已关闭');
    }
  }
}

// 运行
main().catch(console.error);
