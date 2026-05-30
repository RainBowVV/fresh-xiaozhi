const { get, post } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    address: null,
    orderItems: [],
    totalAmount: '0.00',
    remark: '',
    buyNow: false,
    submitting: false
  },

  onLoad(options) {
    if (options.buyNow === '1') {
      this.setData({ buyNow: true });
    }
    this.loadOrderItems();
  },

  onShow() {
    this.loadAddress();
  },

  onUnload() {
    app.globalData._selectedAddress = null;
    app.globalData._buyNowProduct = null;
  },

  async loadAddress() {
    if (app.globalData._selectedAddress) {
      this.setData({ address: app.globalData._selectedAddress });
      app.globalData._selectedAddress = null;
      return;
    }
    if (!this.data.address) {
      try {
        const res = await get('/api/address/list');
        const addresses = res.data || [];
        const addr = addresses.find(a => a.isDefault === 1) || addresses[0] || null;
        this.setData({ address: addr });
      } catch (e) {
        console.error('加载地址失败', e);
      }
    }
  },

  loadOrderItems() {
    if (this.data.buyNow) {
      // 立即购买：从 globalData 读取商品数据，不再请求API
      const item = app.globalData._buyNowProduct;
      if (item) {
        this.setData({
          orderItems: [item],
          totalAmount: (item.price * item.quantity).toFixed(2)
        });
      }
    } else {
      // 购物车结算：从 globalData 读取已勾选商品
      const items = app.globalData._checkoutItems || [];
      let total = 0;
      items.forEach(item => {
        total += item.price * item.quantity;
      });
      this.setData({
        orderItems: items,
        totalAmount: total.toFixed(2)
      });
    }
  },

  selectAddress() {
    wx.navigateTo({ url: '/pages/address/list?select=1' });
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  async submitOrder() {
    if (this.data.submitting) return;
    if (!this.data.address) {
      wx.showToast({ title: '请选择收货地址', icon: 'none' });
      return;
    }
    if (this.data.orderItems.length === 0) {
      wx.showToast({ title: '订单商品为空', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    try {
      const payload = {
        addressId: this.data.address.id,
        remark: this.data.remark
      };

      if (this.data.buyNow) {
        payload.items = this.data.orderItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }));
      } else {
        payload.productIds = this.data.orderItems.map(item => item.productId);
      }

      const res = await post('/api/order/create', payload);
      wx.hideLoading();

      if (res.data) {
        const orderId = res.data.id;
        app.globalData._checkoutItems = null;
        app.globalData._buyNowProduct = null;
        wx.showModal({
          title: '模拟支付',
          content: '订单金额 ¥' + this.data.totalAmount + '，是否确认支付？',
          confirmText: '确认支付',
          cancelText: '稍后支付',
          success: async (modalRes) => {
            if (modalRes.confirm) {
              try {
                await post('/api/order/pay/' + orderId);
                wx.showToast({ title: '支付成功', icon: 'success' });
                app.loadCartCount();
                setTimeout(() => {
                  wx.redirectTo({ url: '/pages/order/detail?id=' + orderId });
                }, 1500);
              } catch (err) {
                wx.showToast({ title: '支付失败', icon: 'none' });
                wx.redirectTo({ url: '/pages/order/detail?id=' + orderId });
              }
            } else {
              wx.redirectTo({ url: '/pages/order/detail?id=' + orderId });
            }
          }
        });
      }
    } catch (e) {
      wx.hideLoading();
      wx.showToast({ title: '下单失败', icon: 'none' });
      this.setData({ submitting: false });
    }
  }
});
