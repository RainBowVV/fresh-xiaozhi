const { get, post } = require('../../utils/request');
const app = getApp();

Page({
  data: {
    product: {},
    categoryName: '',
    quantity: 1,
    cartCount: 0,
    showQtyModal: false,
    pendingAction: ''
  },

  onLoad(options) {
    if (options.id) {
      this.loadProduct(options.id);
    }
  },

  onShow() {
    this.setData({ cartCount: app.globalData.cartCount });
  },

  async loadProduct(id) {
    try {
      const res = await get('/api/product/' + id);
      const product = res.data;
      this.setData({ product });

      if (app.globalData._categories) {
        const cat = app.globalData._categories.find(c => c.id === product.categoryId);
        if (cat) this.setData({ categoryName: cat.name });
      } else {
        const catRes = await get('/api/category/list');
        if (catRes.data) {
          app.globalData._categories = catRes.data;
          const cat = catRes.data.find(c => c.id === product.categoryId);
          if (cat) this.setData({ categoryName: cat.name });
        }
      }
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  previewImage() {
    wx.previewImage({
      urls: [this.data.product.image],
      current: this.data.product.image
    });
  },

  addToCart() {
    if (!app.checkLogin()) return;
    this.setData({ pendingAction: 'cart', showQtyModal: true, quantity: 1 });
  },

  buyNow() {
    if (!app.checkLogin()) return;
    this.setData({ pendingAction: 'buy', showQtyModal: true, quantity: 1 });
  },

  hideQtyModal() {
    this.setData({ showQtyModal: false });
  },

  increaseQty() {
    this.setData({ quantity: this.data.quantity + 1 });
  },

  decreaseQty() {
    if (this.data.quantity > 1) {
      this.setData({ quantity: this.data.quantity - 1 });
    }
  },

  async confirmAction() {
    const { product, quantity, pendingAction } = this.data;
    this.setData({ showQtyModal: false });

    if (pendingAction === 'cart') {
      try {
        await post('/api/cart/add', { productId: product.id, quantity });
        wx.showToast({ title: '已加入购物车', icon: 'success' });
        const count = await app.loadCartCount();
        this.setData({ cartCount: count });
      } catch (e) {
        console.error('加入购物车失败', e);
      }
    } else if (pendingAction === 'buy') {
      app.globalData._buyNowProduct = {
        productId: product.id,
        productName: product.name,
        productImage: product.image,
        price: product.price,
        quantity: quantity
      };
      wx.navigateTo({ url: '/pages/order/create?buyNow=1' });
    }
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  goCart() {
    wx.switchTab({ url: '/pages/cart/cart' });
  }
});
