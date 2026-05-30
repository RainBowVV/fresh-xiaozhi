const { get, put, del } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    isLoggedIn: false,
    cartItems: [],
    isAllSelected: true,
    totalAmount: '0.00',
    selectedCount: 0,
    scrollTop: 0
  },

  onShow() {
    const isLoggedIn = !!app.globalData.token;
    this.setData({ isLoggedIn, scrollTop: 0 });
    if (isLoggedIn) {
      this.loadCart();
    }
  },

  async loadCart() {
    try {
      const res = await get('/api/cart/list');
      const items = (res.data || []).map(item => ({
        ...item,
        selected: true
      }));
      this.setData({ cartItems: items });
      this.calcTotal();
      app.setCartCountFromItems(items);
    } catch (e) {
      console.error('加载购物车失败', e);
    }
  },

  calcTotal() {
    const { cartItems } = this.data;
    let total = 0;
    let count = 0;
    cartItems.forEach(item => {
      if (item.selected) {
        total += Number(item.price || 0) * Number(item.quantity || 0);
        count += Number(item.quantity || 0);
      }
    });
    this.setData({
      totalAmount: total.toFixed(2),
      selectedCount: count,
      isAllSelected: cartItems.length > 0 && cartItems.every(i => i.selected)
    });
  },

  toggleSelectAll() {
    const isAll = !this.data.isAllSelected;
    const cartItems = this.data.cartItems.map(item => ({
      ...item,
      selected: isAll
    }));
    this.setData({ cartItems, isAllSelected: isAll });
    this.calcTotal();
  },

  toggleSelect(e) {
    const index = e.currentTarget.dataset.index;
    const key = 'cartItems[' + index + '].selected';
    this.setData({ [key]: !this.data.cartItems[index].selected });
    this.calcTotal();
  },

  async increaseQty(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    const newQty = item.quantity + 1;
    try {
      await put('/api/cart/update', { productId: item.productId, quantity: newQty });
      const cartItems = this.data.cartItems.map((cartItem, itemIndex) => (
        itemIndex === index ? { ...cartItem, quantity: newQty } : cartItem
      ));
      this.setData({ cartItems });
      this.calcTotal();
      app.setCartCountFromItems(cartItems);
    } catch (e) {
      console.error('更新失败', e);
    }
  },

  async decreaseQty(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    if (item.quantity <= 1) {
      this.deleteItem(e);
      return;
    }
    const newQty = item.quantity - 1;
    try {
      await put('/api/cart/update', { productId: item.productId, quantity: newQty });
      const cartItems = this.data.cartItems.map((cartItem, itemIndex) => (
        itemIndex === index ? { ...cartItem, quantity: newQty } : cartItem
      ));
      this.setData({ cartItems });
      this.calcTotal();
      app.setCartCountFromItems(cartItems);
    } catch (e) {
      console.error('更新失败', e);
    }
  },

  async deleteItem(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    wx.showModal({
      title: '提示',
      content: '确定删除该商品？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/cart/delete/' + item.productId);
            const cartItems = this.data.cartItems.filter((_, i) => i !== index);
            this.setData({ cartItems });
            this.calcTotal();
            app.setCartCountFromItems(cartItems);
            wx.showToast({ title: '已删除', icon: 'success' });
          } catch (err) {
            console.error('删除失败', err);
          }
        }
      }
    });
  },

  async clearCart() {
    wx.showModal({
      title: '提示',
      content: '确定清空购物车？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await del('/api/cart/clear');
            this.setData({ cartItems: [] });
            this.calcTotal();
            app.setCartCount(0);
            wx.showToast({ title: '已清空', icon: 'success' });
          } catch (err) {
            console.error('清空失败', err);
          }
        }
      }
    });
  },

  goCheckout() {
    const selected = this.data.cartItems.filter(i => i.selected);
    if (selected.length === 0) {
      wx.showToast({ title: '请选择商品', icon: 'none' });
      return;
    }
    app.globalData._checkoutItems = selected;
    wx.navigateTo({ url: '/pages/order/create' });
  },

  goProductDetail(e) {
    wx.navigateTo({ url: '/pages/product/detail?id=' + e.currentTarget.dataset.id });
  },

  goLogin() {
    wx.switchTab({ url: '/pages/mine/mine' });
  },

  goShopping() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
