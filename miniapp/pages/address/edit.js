const { post, del } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isEdit: false,
    addressId: null,
    form: {
      name: '',
      phone: '',
      detail: '',
      isDefault: 0
    },
    region: []
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true, addressId: Number(options.id) });
      wx.setNavigationBarTitle({ title: '编辑地址' });
      this.loadAddress(Number(options.id));
    } else {
      wx.setNavigationBarTitle({ title: '新增地址' });
    }
  },

  loadAddress(id) {
    const address = app.globalData._editingAddress;
    app.globalData._editingAddress = null;

    if (!address || Number(address.id) !== Number(id)) {
      wx.showToast({ title: '地址信息已失效，请返回重试', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({
      form: {
        name: address.name,
        phone: address.phone,
        detail: address.detail,
        isDefault: address.isDefault
      },
      region: [address.province, address.city, address.district]
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ ['form.' + field]: e.detail.value });
  },

  onRegionChange(e) {
    this.setData({ region: e.detail.value });
  },

  onDefaultChange(e) {
    this.setData({ 'form.isDefault': e.detail.value ? 1 : 0 });
  },

  async saveAddress() {
    const { form, region, isEdit, addressId } = this.data;

    if (!form.name.trim()) {
      wx.showToast({ title: '请输入收货人姓名', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(form.phone.trim())) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }
    if (!region[0]) {
      wx.showToast({ title: '请选择所在地区', icon: 'none' });
      return;
    }
    if (!form.detail.trim()) {
      wx.showToast({ title: '请输入详细地址', icon: 'none' });
      return;
    }

    const data = {
      name: form.name.trim(),
      phone: form.phone.trim(),
      province: region[0],
      city: region[1],
      district: region[2],
      detail: form.detail.trim(),
      isDefault: form.isDefault
    };

    if (isEdit) {
      data.id = addressId;
    }

    try {
      await post('/api/address/save', data);
      wx.showToast({ title: '保存成功', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 1500);
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  async deleteAddress() {
    wx.showModal({
      title: '提示',
      content: '确定删除该地址？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/address/delete/' + this.data.addressId);
            wx.showToast({ title: '已删除', icon: 'success' });
            setTimeout(() => wx.navigateBack(), 1500);
          } catch (e) {
            wx.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      }
    });
  }
});
