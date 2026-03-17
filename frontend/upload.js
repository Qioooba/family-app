const ci = require('miniprogram-ci');
const path = require('path');

// 小程序配置
const config = {
    appid: 'wxbdc70536c5e52b82',
    type: 'miniProgram',
    projectPath: path.join(__dirname, 'dist/build/mp-weixin'),
    privateKeyPath: path.join(__dirname, 'private.key'),
    ignores: ['node_modules/**/*']
};

// 上传配置
const uploadConfig = {
    version: '1.1.1',
    desc: '修复首页今日提醒不显示标题的问题',
    setting: {
        es6: true,
        es7: true,
        minify: true,
        codeProtect: false,
        autoPrefixWXSS: true
    }
};

async function upload() {
    console.log('🚀 开始上传微信小程序体验版...');
    console.log(`AppID: ${config.appid}`);
    console.log(`版本: ${uploadConfig.version}`);
    console.log(`描述: ${uploadConfig.desc}`);
    console.log('');

    try {
        const project = new ci.Project(config);
        
        const result = await ci.upload({
            project,
            ...uploadConfig,
            onProgressUpdate: (info) => {
                console.log(`⏳ 上传进度: ${info._msg}`);
            }
        });

        console.log('✅ 上传成功！');
        console.log('');
        console.log('请在微信小程序后台设置体验版：');
        console.log('1. 进入 版本管理');
        console.log('2. 找到版本 1.1.0');
        console.log('3. 点击"选为体验版"');
        console.log('');
        console.log('预览二维码信息：', result.subPackageInfo);
        
    } catch (error) {
        console.error('❌ 上传失败:', error.message);
        if (error.message.includes('private')) {
            console.log('');
            console.log('请按以下步骤获取 private.key：');
            console.log('1. 登录微信小程序后台：https://mp.weixin.qq.com');
            console.log('2. 进入 开发管理 -> 开发设置 -> 小程序代码上传');
            console.log('3. 生成并下载上传密钥(private.key)');
            console.log('4. 将文件放置到项目根目录');
        }
        process.exit(1);
    }
}

upload();
