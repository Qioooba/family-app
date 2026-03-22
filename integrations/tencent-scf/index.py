# -*- coding: utf-8 -*-
"""
腾讯云函数 - 企业微信推送服务
用于发送任务通知给齐军/陶陶
"""

import json
import requests
import os

# 从环境变量读取配置
CORP_ID = os.environ.get('CORPID', '')
AGENT_ID = os.environ.get('AGENTID', '')
SECRET = os.environ.get('SECRET', '')

# 缓存access_token
access_token = None
token_expire_time = 0

def get_access_token():
    """获取企业微信access_token"""
    global access_token, token_expire_time
    
    # 如果token未过期，直接返回
    import time
    if access_token and time.time() < token_expire_time:
        return access_token
    
    url = f"https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={CORP_ID}&corpsecret={SECRET}"
    resp = requests.get(url, timeout=10)
    data = resp.json()
    
    if data.get('access_token'):
        access_token = data['access_token']
        # token有效期2小时，提前5分钟刷新
        token_expire_time = time.time() + (7200 - 300)
        return access_token
    else:
        raise Exception(f"获取token失败: {data}")

def send_message(user_id, title, description, url=None):
    """发送企业微信消息"""
    token = get_access_token()
    api_url = f"https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token={token}"
    
    message = {
        "touser": user_id,
        "msgtype": "textcard",
        "agentid": AGENT_ID,
        "textcard": {
            "title": title,
            "description": description,
            "url": url or "https://qioba.cn",
            "btntxt": "查看详情"
        }
    }
    
    resp = requests.post(api_url, json=message, timeout=10)
    return resp.json()

def main_handler(event, context):
    """云函数入口"""
    try:
        # 解析请求体
        body = event.get('body', '{}')
        if isinstance(body, str):
            body = json.loads(body)
        
        user_id = body.get('userId')
        title = body.get('title')
        description = body.get('description')
        url = body.get('url')
        
        # 参数校验
        if not all([user_id, title, description]):
            return {
                'statusCode': 400,
                'body': json.dumps({'code': 400, 'message': '参数错误: userId, title, description 不能为空'})
            }
        
        # 发送消息
        result = send_message(user_id, title, description, url)
        
        return {
            'statusCode': 200,
            'body': json.dumps({
                'code': 200,
                'message': '发送成功',
                'data': result
            })
        }
        
    except Exception as e:
        return {
            'statusCode': 500,
            'body': json.dumps({'code': 500, 'message': str(e)})
        }
