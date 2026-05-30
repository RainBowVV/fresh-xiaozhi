const { get, put, del } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    addresses: [],
    selectMode: false
  },

  onLoad(options) {
    if (options.select === '1') {
      this.setData({ selectMode: true });
    }
  },

  onShow() {
    this.loadAddresses();
  },

  async loadAddresses() {
    try {
      const res = await get('/api/address/list');
      this.setData({ addresses: res.data || [] });
    } catch (e) {
      console.error('加载地址失败', e);
    }
  },

  selectAddress(e) {
    const index = e.currentTarget.dataset.index;
    const address = this.data.addresses[index];
    app.globalData._selectedAddress = address;
    wx.navigateBack();
  },

  addAddress() {
    wx.navigateTo({ url: '/pages/address/edit' });
  },

  editAddress(e) {
    const index = e.currentTarget.dataset.index;
    const address = this.data.addresses[index];
    app.globalData._editingAddress = address;
    wx.navigateTo({ url: '/pages/address/edit?id=' + address.id });
  },

  async setDefault(e) {
    const id = e.currentTarget.dataset.id;
    try {
      await put('/api/address/default/' + id);
      wx.showToast({ title: '已设为默认', icon: 'success' });
      this.loadAddresses();
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  },

  async deleteAddress(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定删除该地址？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/address/delete/' + id);
            wx.showToast({ title: '已删除', icon: 'success' });
            this.loadAddresses();
          } catch (e) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  }
});
