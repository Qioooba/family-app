#!/usr/bin/env node
/**
 * 自动获取企业微信外部联系人并更新数据库
 * 使用代码中找到的配置
 */

const axios = require('axios');
const mysql = require('mysql2/promise');

// 从代码中找到的配置
const CONFIG = {
  corpId: 'ww6c1c7590db91ef85',
  secret: 'Ne0oN5Y8mNmRA_wkIP7I4PMn_sr2GFPkbBABqUaEEE4',
  agentId: '1000002',
  userId: 'XIAOZHUSHOU'
};

// 数据库配置
const DB_CONFIG = {
  host: 'localhost',
  port: 3306,
  user: 'root',
  password: process.env.MYSQL_PASSWORD || 'your_secure_mysql_password',
  database: 'family_app'
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
  console.log('🔑 正在获取access_token...');
  
  const resp = await axios.get(url);
  
  if (resp.data.access_token) {
    accessToken = resp.data.access_token;
    tokenExpireTime = Date.now() + (7200 - 300) * 1000;
    console.log('✅ 获取access_token成功\n');
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
  
  console.log('📋 正在获取客户列表...');
  const resp = await axios.get(url);
  
  if (resp.data.errcode !== 0) {
    throw new Error('获取客户列表失败: ' + resp.data.errmsg);
  }
  
  const customers = resp.data.external_userid || [];
  console.log(`✅ 找到 ${customers.length} 个客户\n`);
  return customers;
}

/**
 * 获取客户详情
 */
async function getCustomerDetail(token, externalUserId) {
  const url = `https://qyapi.weixin.qq.com/cgi-bin/externalcontact/get?access_token=${token}&external_userid=${externalUserId}`;
  
  const resp = await axios.get(url);
  
  if (resp.data.errcode !== 0) {
    throw new Error(resp.data.errmsg);
  }
  
  return resp.data.external_contact;
}

/**
 * 连接数据库
 */
async function connectDB() {
  console.log('📡 正在连接数据库...');
  const conn = await mysql.createConnection(DB_CONFIG);
  console.log('✅ 数据库连接成功\n');
  return conn;
}

/**
 * 创建sys_config表（如果不存在）
 */
async function createConfigTable(conn) {
  console.log('📝 检查并创建sys_config表...');
  
  await conn.execute(`
    CREATE TABLE IF NOT EXISTS sys_config (
      id BIGINT PRIMARY KEY AUTO_INCREMENT,
      config_key VARCHAR(100) NOT NULL COMMENT '配置键',
      config_value TEXT COMMENT '配置值',
      description VARCHAR(255) COMMENT '配置说明',
      category VARCHAR(50) DEFAULT 'general' COMMENT '配置分类',
      is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密存储',
      create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      UNIQUE KEY uk_config_key (config_key),
      INDEX idx_category (category)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表'
  `);
  
  console.log('✅ 表检查完成\n');
}

/**
 * 添加用户表字段（如果不存在）
 */
async function addUserColumns(conn) {
  console.log('📝 检查并添加用户表字段...');
  
  try {
    // 检查work_user_id字段是否存在
    const [workCols] = await conn.execute(
      "SHOW COLUMNS FROM sys_user LIKE 'work_user_id'"
    );
    
    if (workCols.length === 0) {
      await conn.execute(`
        ALTER TABLE sys_user 
        ADD COLUMN work_user_id VARCHAR(100) COMMENT '企业微信成员ID（内部通讯录）'
      `);
      console.log('  ✅ 添加 work_user_id 字段');
    } else {
      console.log('  ℹ️ work_user_id 字段已存在');
    }
    
    // 检查external_user_id字段是否存在
    const [extCols] = await conn.execute(
      "SHOW COLUMNS FROM sys_user LIKE 'external_user_id'"
    );
    
    if (extCols.length === 0) {
      await conn.execute(`
        ALTER TABLE sys_user 
        ADD COLUMN external_user_id VARCHAR(100) COMMENT '企业微信外部联系人ID（客户联系）'
      `);
      console.log('  ✅ 添加 external_user_id 字段');
    } else {
      console.log('  ℹ️ external_user_id 字段已存在');
    }
    
    console.log('✅ 用户表字段检查完成\n');
  } catch (e) {
    console.log('⚠️ 添加字段时出错:', e.message);
    throw e;
  }
}

/**
 * 获取数据库用户
 */
async function getDBUsers(conn) {
  const [rows] = await conn.execute(
    'SELECT id, username, nickname, work_user_id, external_user_id FROM sys_user WHERE status = 1'
  );
  return rows;
}

/**
 * 更新用户external_user_id
 */
async function updateUserExternalId(conn, userId, externalUserId) {
  await conn.execute(
    'UPDATE sys_user SET external_user_id = ? WHERE id = ?',
    [externalUserId, userId]
  );
}

/**
 * 将配置插入数据库（如果不存在）
 */
async function insertConfig(conn) {
  console.log('📝 正在同步配置到数据库...');
  
  const configs = [
    ['wechat.work.corpid', CONFIG.corpId, '企业微信CorpID', 'wechat_work'],
    ['wechat.work.agentid', CONFIG.agentId, '企业微信应用AgentID', 'wechat_work'],
    ['wechat.work.secret', CONFIG.secret, '企业微信应用Secret', 'wechat_work'],
    ['wechat.work.userid', CONFIG.userId, '企业微信小助手UserID', 'wechat_work']
  ];
  
  for (const [key, value, desc, category] of configs) {
    await conn.execute(
      `INSERT INTO sys_config (config_key, config_value, description, category) 
       VALUES (?, ?, ?, ?) 
       ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)`,
      [key, value, desc, category]
    );
  }
  
  console.log('✅ 配置已同步到数据库\n');
}

/**
 * 主函数
 */
async function main() {
  console.log('🚀 开始同步企业微信外部联系人...\n');
  console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
  
  let conn;
  
  try {
    // 1. 连接数据库
    conn = await connectDB();
    
    // 1.5 创建表和字段（如果不存在）
    await createConfigTable(conn);
    await addUserColumns(conn);
    
    // 2. 将配置插入数据库
    await insertConfig(conn);
    
    // 3. 获取数据库用户
    console.log('👥 查询数据库用户...');
    const users = await getDBUsers(conn);
    console.log(`✅ 找到 ${users.length} 个用户:\n`);
    
    users.forEach(u => {
      console.log(`   ID:${u.id} | 用户名:${u.username} | 昵称:${u.nickname || '无'}`);
      console.log(`   内部成员ID: ${u.work_user_id || '未设置'} | 外部联系人ID: ${u.external_user_id || '未设置'}`);
      console.log('');
    });
    
    // 4. 获取企业微信客户列表
    const customerIds = await getCustomerList();
    
    if (customerIds.length === 0) {
      console.log('⚠️ 小助手没有客户，请先让客户扫码添加');
      return;
    }
    
    // 5. 获取客户详情并匹配
    console.log('🔍 获取客户详情并匹配...\n');
    const token = await getAccessToken();
    
    let updatedCount = 0;
    
    for (const externalUserId of customerIds) {
      try {
        const detail = await getCustomerDetail(token, externalUserId);
        const customerName = detail.name || '';
        
        console.log(`👤 客户: ${customerName} (${externalUserId})`);
        
        // 匹配用户
        const matchedUser = users.find(u => {
          const nickname = (u.nickname || '').toLowerCase();
          const username = (u.username || '').toLowerCase();
          const cName = customerName.toLowerCase();
          
          // 完全匹配
          if (nickname === cName || username === cName) return true;
          
          // 部分匹配（针对中文名）
          if (cName.includes('齐') && (nickname.includes('齐') || username.includes('qi'))) return true;
          if (cName.includes('陶') && (nickname.includes('陶') || username.includes('tao'))) return true;
          
          return false;
        });
        
        if (matchedUser) {
          console.log(`   ✅ 匹配到用户: ${matchedUser.nickname || matchedUser.username} (ID:${matchedUser.id})`);
          
          if (matchedUser.external_user_id === externalUserId) {
            console.log('   ℹ️ 外部联系人ID已是最新，无需更新');
          } else {
            await updateUserExternalId(conn, matchedUser.id, externalUserId);
            console.log(`   📝 已更新 external_user_id`);
            updatedCount++;
          }
        } else {
          console.log('   ⚠️ 未匹配到用户，请在小程序中绑定');
        }
        console.log('');
        
      } catch (e) {
        console.log(`   ❌ 获取详情失败: ${e.message}\n`);
      }
    }
    
    // 6. 显示最终结果
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`✅ 同步完成！更新了 ${updatedCount} 个用户`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
    
    console.log('📊 最终用户状态:');
    const finalUsers = await getDBUsers(conn);
    finalUsers.forEach(u => {
      const hasWork = u.work_user_id ? '✅' : '❌';
      const hasExt = u.external_user_id ? '✅' : '❌';
      console.log(`   ${u.nickname || u.username}: 内部${hasWork} 外部${hasExt}`);
    });
    
    console.log('\n✨ 完成！现在推送消息会优先使用外部联系人通道');
    
  } catch (error) {
    console.error('\n❌ 错误:', error.message);
    if (error.response) {
      console.error('响应:', error.response.data);
    }
  } finally {
    if (conn) {
      await conn.end();
      console.log('\n📡 数据库连接已关闭');
    }
  }
}

// 运行
main().catch(console.error);
