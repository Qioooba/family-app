// 腾讯云函数入口文件 - 企业微信推送服务
// 部署方式：腾讯云控制台 → 云函数 → 新建 → 自定义创建 → Node.js 16

const axios = require('axios');

// 企业微信配置（从环境变量读取）
const CORP_ID = process.env.CORPID;
const AGENT_ID = process.env.AGENTID;
const SECRET = process.env.SECRET;

// 缓存access_token
let accessToken = null;
let tokenExpireTime = 0;

/**
 * 获取企业微信access_token
 */
async function getAccessToken() {
    // 如果token未过期，直接返回
    if (accessToken && Date.now() < tokenExpireTime) {
        return accessToken;
    }

    try {
        const url = `https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${CORP_ID}&corpsecret=${SECRET}`;
        const response = await axios.get(url);
        
        if (response.data.access_token) {
            accessToken = response.data.access_token;
            // token有效期2小时，提前5分钟刷新
            tokenExpireTime = Date.now() + (7200 - 300) * 1000;
            console.log('获取access_token成功');
            return accessToken;
        } else {
            throw new Error(response.data.errmsg || '获取token失败');
        }
    } catch (error) {
        console.error('获取access_token失败:', error.message);
        throw error;
    }
}

/**
 * 发送企业微信消息
 */
async function sendMessage(userId, title, description, url) {
    const token = await getAccessToken();
    
    const apiUrl = `https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${token}`;
    
    const message = {
        touser: userId,
        msgtype: 'textcard',
        agentid: AGENT_ID,
        textcard: {
            title: title,
            description: description,
            url: url || 'https://qioba.cn',
            btntxt: '查看详情'
        }
    };

    const response = await axios.post(apiUrl, message);
    
    if (response.data.errcode !== 0) {
        throw new Error(`发送失败: ${response.data.errmsg}`);
    }
    
    return response.data;
}

/**
 * 云函数入口
 */
exports.main_handler = async (event, context) => {
    console.log('收到请求:', JSON.stringify(event));
    
    try {
        // 解析请求体
        let body = event.body;
        if (typeof body === 'string') {
            body = JSON.parse(body);
        }
        
        const { userId, title, description, url } = body;
        
        // 参数校验
        if (!userId || !title || !description) {
            return {
                statusCode: 400,
                headers: {
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                },
                body: JSON.stringify({
                    code: 400,
                    message: '参数错误：userId, title, description 不能为空'
                })
            };
        }
        
        // 检查配置
        if (!CORP_ID || !AGENT_ID || !SECRET) {
            return {
                statusCode: 500,
                headers: {
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                },
                body: JSON.stringify({
                    code: 500,
                    message: '服务器配置错误：缺少企业微信配置'
                })
            };
        }
        
        // 发送消息
        const result = await sendMessage(userId, title, description, url);
        
        return {
            statusCode: 200,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
            body: JSON.stringify({
                code: 200,
                message: '发送成功',
                data: result
            })
        };
        
    } catch (error) {
        console.error('处理请求失败:', error);
        
        return {
            statusCode: 500,
            headers: {
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*'
            },
                body: JSON.stringify({
                    code: 500,
                message: '发送失败: ' + error.message
            })
        };
    }
};