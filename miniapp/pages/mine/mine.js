const { get, post } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isLoggedIn: false,
    userInfo: null,
    cartCount: 0,
    showLogin: false,
    tempAvatar: '',
    tempNickname: '',
    canLogin: false
  },

  onShow() {
    const isLoggedIn = !!app.globalData.token;
    const userInfo = app.globalData.userInfo;
    this.setData({
      isLoggedIn,
      userInfo,
      cartCount: app.globalData.cartCount
    });
    // 只在没有缓存用户信息时请求API
    if (isLoggedIn && !userInfo) {
      this.loadUserInfo();
    }
  },

  async loadUserInfo() {
    try {
      const res = await get('/api/user/info');
      if (res.data) {
        this.setData({ userInfo: res.data });
        app.globalData.userInfo = res.data;
        wx.setStorageSync('userInfo', res.data);
      }
    } catch (e) {
      console.error('获取用户信息失败', e);
    }
  },

  // 显示登录卡片
  showLoginCard() {
    this.setData({ showLogin: true, tempAvatar: '', tempNickname: '', canLogin: false });
  },

  // 隐藏登录卡片
  hideLoginCard() {
    this.setData({ showLogin: false });
  },

  // 微信推荐接口：选择头像
  onChooseAvatar(e) {
    const avatarUrl = e.detail.avatarUrl;
    if (!avatarUrl) return;
    this.setData({
      tempAvatar: avatarUrl,
      canLogin: !!this.data.tempNickname && !!avatarUrl
    });
  },

  // 昵称输入
  onNicknameInput(e) {
    const nickname = e.detail.value;
    this.setData({
      tempNickname: nickname,
      canLogin: !!nickname && !!this.data.tempAvatar
    });
  },

  // 确认登录
  async confirmLogin() {
    const { tempAvatar, tempNickname, canLogin } = this.data;
    if (!canLogin) {
      wx.showToast({ title: '请设置头像和昵称', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '登录中...' });
    try {
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({ success: resolve, fail: reject });
      });

      if (!loginRes.code) {
        wx.hideLoading();
        wx.showToast({ title: '获取登录凭证失败', icon: 'none' });
        return;
      }

      const res = await post('/api/user/login', {
        code: loginRes.code,
        nickname: tempNickname,
        avatarUrl: tempAvatar
      });

      if (res.data) {
        app.globalData.token = res.data;
        wx.setStorageSync('token', res.data);
        this.setData({
          isLoggedIn: true,
          showLogin: false,
          tempAvatar: '',
          tempNickname: ''
        });
        this.loadUserInfo();
        app.loadCartCount();
        wx.hideLoading();
        wx.showToast({ title: '登录成功', icon: 'success' });
      }
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: '登录失败，请检查服务', icon: 'none' });
    }
  },

  logout() {
    const that = this;
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.token = '';
          app.globalData.userInfo = null;
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          app.setCartCount(0);
          that.setData({
            isLoggedIn: false,
            userInfo: null,
            cartCount: 0
          });
        }
      }
    });
  },

  showAbout() {
    wx.showModal({
      title: '关于鲜小智',
      content: '鲜小智买菜小程序 v1.0.0\n\n新鲜食材，智慧生活\n当日下单，次日送达',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  goOrderList(e) {
    if (!app.checkLogin()) return;
    const status = e.currentTarget.dataset.status;
    const url = status !== undefined ? '/pages/order/list?status=' + status : '/pages/order/list';
    wx.navigateTo({ url });
  },

  goAddress() {
    if (!app.checkLogin()) return;
    wx.navigateTo({ url: '/pages/address/list' });
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' });
  },

  goChat() {
    if (!app.checkLogin()) return;
    wx.navigateTo({ url: '/pages/chat/chat' });
  }

});
