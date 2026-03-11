/**
 * 免密登录工具
 * 处理从企业微信跳转过来的自动登录
 */

// 检查是否需要自动登录
export function checkAutoLogin() {
  return new Promise((resolve) => {
    // #ifdef H5
    // H5环境下检查URL参数
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    
    if (token) {
      console.log('检测到免密登录Token:', token);
      // 保存token
      uni.setStorageSync('auto_login_token', token);
      resolve({ needLogin: true, token });
    } else {
      // 检查localStorage
      const storedToken = localStorage.getItem('family_auto_login_token');
      if (storedToken) {
        uni.setStorageSync('auto_login_token', storedToken);
        // 清除localStorage中的token
        localStorage.removeItem('family_auto_login_token');
        resolve({ needLogin: true, token: storedToken });
      } else {
        resolve({ needLogin: false });
      }
    }
    // #endif
    
    // #ifndef H5
    // 小程序环境检查storage
    const token = uni.getStorageSync('auto_login_token');
    if (token) {
      console.log('检测到免密登录Token:', token);
      resolve({ needLogin: true, token });
    } else {
      resolve({ needLogin: false });
    }
    // #endif
  });
}

// 执行免密登录
export async function doAutoLogin(token) {
  try {
    uni.showLoading({ title: '自动登录中...' });
    
    const res = await uni.request({
      url: 'https://qioba.cn:8443/api/auth/auto-login',
      method: 'POST',
      header: {
        'Content-Type': 'application/json'
      },
      data: { tempToken: token }
    });
    
    uni.hideLoading();
    
    if (res.data?.code === 200) {
      const { token: authToken, userInfo } = res.data.data;
      
      // 保存登录凭证
      uni.setStorageSync('token', authToken);
      uni.setStorageSync('userInfo', userInfo);
      uni.setStorageSync('currentFamilyId', userInfo.currentFamilyId || 1);
      
      // 清除自动登录token
      uni.removeStorageSync('auto_login_token');
      
      uni.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      });
      
      return { success: true, userInfo };
    } else {
      throw new Error(res.data?.message || '登录失败');
    }
  } catch (error) {
    uni.hideLoading();
    console.error('免密登录失败:', error);
    
    // 清除token
    uni.removeStorageSync('auto_login_token');
    
    uni.showToast({
      title: '登录已过期，请手动登录',
      icon: 'none',
      duration: 2000
    });
    
    return { success: false, error: error.message };
  }
}
