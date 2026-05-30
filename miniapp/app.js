App({
  globalData: {
    baseUrl: 'http://localhost:8080',
    token: '',
    userInfo: null,
    cartCount: 0
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
    this.loadCartCount();
  },

  setCartCount(count) {
    const nextCount = Number(count) || 0;
    this.globalData.cartCount = nextCount;
    if (nextCount > 0) {
      wx.setTabBarBadge({ index: 1, text: nextCount > 99 ? '99+' : String(nextCount) });
    } else {
      wx.removeTabBarBadge({ index: 1 });
    }
  },

  setCartCountFromItems(items) {
    const count = (items || []).reduce((sum, item) => {
      return sum + Number(item.quantity || 0);
    }, 0);
    this.setCartCount(count);
    return count;
  },

  loadCartCount() {
    if (!this.globalData.token) {
      this.setCartCount(0);
      return Promise.resolve(0);
    }
    const { get } = require('./utils/request');
    return get('/api/cart/list')
      .then(res => this.setCartCountFromItems(res.data || []))
      .catch(() => 0);
  },

  checkLogin() {
    if (!this.globalData.token) {
      wx.showModal({
        title: '提示',
        content: '请先登录',
        confirmText: '去登录',
        success(res) {
          if (res.confirm) {
            wx.switchTab({ url: '/pages/mine/mine' });
          }
        }
      });
      return false;
    }
    return true;
  }
});
