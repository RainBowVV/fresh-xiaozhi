const { get, put, del, post } = require('../../utils/request');
const { ORDER_STATUS } = require('../../utils/constants');
const app = getApp();

let countdownTimer = null

function getCountdown(createTime) {
  var t = new Date(createTime.replace('T', ' ').replace(/-/g, '/')).getTime()
  var diff = t + 30 * 60 * 1000 - Date.now()
  if (diff <= 0) return '已超时'
  var m = Math.floor(diff / 60000)
  var s = Math.floor((diff % 60000) / 1000)
  return (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s
}

Page({
  data: {
    orders: [],
    countdowns: {},
    currentTab: -1,
    statusMap: ORDER_STATUS
  },

  // Tab 对应的状态值
  tabStatusMap: {
    '-1': null,           // 全部
    '0': [0],             // 待付款
    '1': [1, 2, 3],       // 待发货（已付款+备货中+配送中）
    '4': [4],             // 已完成
    '5': [5]              // 已取消
  },

  onLoad(options) {
    if (options.status !== undefined) {
      this.setData({ currentTab: Number(options.status) });
    }
  },

  onShow() {
    if (!app.checkLogin()) return;
    this.loadOrders();
  },

  onUnload() {
    if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  },

  startCountdown() {
    if (countdownTimer) clearInterval(countdownTimer)
    this.updateCountdowns()
    countdownTimer = setInterval(() => { this.updateCountdowns() }, 1000)
  },

  updateCountdowns() {
    const countdowns = {}
    let hasPending = false
    this.data.orders.forEach(o => {
      if (o.status === 0) {
        countdowns[o.id] = getCountdown(o.createTime)
        hasPending = true
      }
    })
    this.setData({ countdowns })
    if (!hasPending && countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  },

  async loadOrders() {
    const tab = String(this.data.currentTab);
    const statuses = this.tabStatusMap[tab];
    let url = '/api/order/list';
    if (statuses) {
      url += '?statuses=' + statuses.join(',');
    }
    try {
      const res = await get(url);
      const orders = (res.data || []).map(order => ({
        ...order,
        items: order.items || []
      }));
      this.setData({ orders });
      this.startCountdown()
    } catch (e) {
      console.error('加载订单失败', e);
    }
  },

  switchTab(e) {
    const tab = Number(e.currentTarget.dataset.tab);
    this.setData({ currentTab: tab });
    this.loadOrders();
  },

  goOrderDetail(e) {
    wx.navigateTo({ url: '/pages/order/detail?id=' + e.currentTarget.dataset.id });
  },

  async cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定取消该订单？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await put('/api/order/cancel/' + id);
            wx.showToast({ title: '已取消', icon: 'success' });
            this.loadOrders();
          } catch (err) {
            wx.showToast({ title: '取消失败', icon: 'none' });
          }
        }
      }
    });
  },

  async payOrder(e) {
    const id = e.currentTarget.dataset.id;
    const order = this.data.orders.find(o => o.id === id);
    wx.showModal({
      title: '模拟支付',
      content: '订单金额 ¥' + order.totalAmount + '，是否确认支付？',
      confirmText: '确认支付',
      success: async (res) => {
        if (res.confirm) {
          try {
            await post('/api/order/pay/' + id);
            wx.showToast({ title: '支付成功', icon: 'success' });
            this.loadOrders();
          } catch (err) {
            wx.showToast({ title: '支付失败', icon: 'none' });
          }
        }
      }
    });
  },

  async deleteOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定删除该订单？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/order/delete/' + id);
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadOrders();
          } catch (err) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  async reorder(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await post('/api/cart/reorder/' + id);
      wx.showToast({ title: '已加入购物车', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/cart/cart' });
      }, 1500);
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  goShopping() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
