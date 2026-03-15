const ci = require('miniprogram-ci');
const path = require('path');

// 小程序配置
const project = new ci.Project({
    appid: 'wxbdc70536c5e52b82',
    type: 'miniProgram',
    projectPath: path.join(__dirname, 'dist/build/mp-weixin'),
    privateKeyPath: path.join(__dirname, 'private.key'),
});

// 设置为体验版
async function setExperience() {
    try {
        console.log('🚀 正在设置为体验版...');
        
        const result = await ci.setExperience({
            project,
            version: '1.1.1',
            experience: true, // 设置为体验版
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
