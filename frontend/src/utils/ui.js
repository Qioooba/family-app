// UI工具
export const toast = {
  success: (msg) => {
    uni.showToast({ title: msg, icon: 'success' })
  },
  error: (msg) => {
    uni.showToast({ title: msg, icon: 'none' })
  },
  loading: (msg = '加载中...') => {
    uni.showLoading({ title: msg })
  },
  hide: () => {
    uni.hideLoading()
    uni.hideToast()
  }
}

export const loading = {
  show: (msg = '加载中...') => {
    uni.showLoading({ title: msg, mask: true })
  },
  hide: () => {
    uni.hideLoading()
  }
}

export const modal = {
  confirm: (options) => {
    return new Promise((resolve) => {
      uni.showModal({
        title: options.title || '提示',
        content: options.content,
        showCancel: options.showCancel !== false,
        confirmText: options.confirmText || '确定',
        cancelText: options.cancelText || '取消',
        confirmColor: options.confirmColor || '#5B8FF9',
        success: (res) => {
          resolve(res.confirm)
        }
      })
    })
  }
}
