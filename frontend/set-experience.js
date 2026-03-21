const ci = require('miniprogram-ci');
const path = require('path');

const version = process.env.MINIPROGRAM_VERSION || process.env.npm_package_version || '1.0.0';

const project = new ci.Project({
    appid: process.env.WEIXIN_APPID || '',
    type: 'miniProgram',
    projectPath: process.env.MINIPROGRAM_PROJECT_PATH || path.join(__dirname, 'dist/build/mp-weixin'),
    privateKeyPath: process.env.MINIPROGRAM_PRIVATE_KEY_PATH || path.join(__dirname, 'private.key'),
});

async function setExperience() {
    try {
        if (!project.appid) {
            throw new Error('缺少 WEIXIN_APPID 环境变量');
        }

        console.log('🚀 正在设置为体验版...');
        console.log(`版本: ${version}`);
        
        const result = await ci.setExperience({
            project,
            version,
            experience: true,
        });
        
        console.log('✅ 体验版设置成功！');
        console.log('体验版二维码信息:', result);
        
    } catch (error) {
        console.error('❌ 设置失败:', error.message);
        
        if (error.message.includes('private')) {
            console.log('');
            console.log('⚠️  缺少 private.key 文件');
            console.log('获取方式:');
            console.log('1. 登录 https://mp.weixin.qq.com');
            console.log('2. 开发管理 → 开发设置 → 小程序代码上传');
            console.log('3. 下载上传密钥 (private.key)');
            console.log('4. 将文件放到前端项目目录');
        }
        
        process.exit(1);
    }
}

setExperience();
