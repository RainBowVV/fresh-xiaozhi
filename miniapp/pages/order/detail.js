const { get, put, del, post } = require('../../utils/request');
const { ORDER_STATUS } = require('../../utils/constants');
const app = getApp()

let detailTimer = null

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
    order: {},
    countdown: '',
    statusMap: ORDER_STATUS
  },

  onLoad(options) {
    if (options.id) {
      this._orderId = options.id;
    }
  },

  onShow() {
    if (this._orderId) {
      this.loadOrder(this._orderId);
    }
  },

  onUnload() {
    if (detailTimer) { clearInterval(detailTimer); detailTimer = null }
  },

  async loadOrder(id) {
    try {
      const res = await get('/api/order/' + id);
      this.setData({ order: res.data });
      if (res.data.status === 0) {
        this.startCountdown(res.data.createTime)
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  startCountdown(createTime) {
    if (detailTimer) clearInterval(detailTimer)
    const update = () => {
      const cd = getCountdown(createTime)
      this.setData({ countdown: cd })
      if (cd === '已超时') { clearInterval(detailTimer); detailTimer = null }
    }
    update()
    detailTimer = setInterval(update, 1000)
  },

  async cancelOrder() {
    wx.showModal({
      title: '提示',
      content: '确定取消该订单？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await put('/api/order/cancel/' + this.data.order.id);
            wx.showToast({ title: '已取消', icon: 'success' });
            this.loadOrder(this.data.order.id);
          } catch (e) {
            wx.showToast({ title: '取消失败', icon: 'none' });
          }
        }
      }
    });
  },

  async payOrder() {
    wx.showModal({
      title: '模拟支付',
      content: '订单金额 ¥' + this.data.order.totalAmount + '，是否确认支付？',
      confirmText: '确认支付',
      success: async (res) => {
        if (res.confirm) {
          try {
            await post('/api/order/pay/' + this.data.order.id);
            wx.showToast({ title: '支付成功', icon: 'success' });
            this.loadOrder(this.data.order.id);
          } catch (e) {
            wx.showToast({ title: '支付失败', icon: 'none' });
          }
        }
      }
    });
  },

  async deleteOrder() {
    wx.showModal({
      title: '提示',
      content: '确定删除该订单？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/order/delete/' + this.data.order.id);
            wx.showToast({ title: '已删除', icon: 'success' });
            setTimeout(() => wx.navigateBack(), 1500);
          } catch (e) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  },

  async reorder() {
    try {
      await post('/api/cart/reorder/' + this.data.order.id);
      wx.showToast({ title: '已加入购物车', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/cart/cart' });
      }, 1500);
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  }
});
