const { get, post } = require('../../utils/request');
const app = getApp();

let navigatingAway = false

Page({
  data: {
    categories: [],
    products: [],
    filteredProducts: [],
    currentCategoryIndex: 0,
    currentCategory: {},
    searchText: '',
    loading: false,
    scrollTop: 0
  },

  onShow() {
    if (navigatingAway) {
      navigatingAway = false
      return
    }
    this.initData();
  },

  onPullDownRefresh() {
    this.initData().then(() => wx.stopPullDownRefresh());
  },

  async initData() {
    this.setData({ loading: true });
    try {
      const catRes = await get('/api/category/list');
      const categories = catRes.data || [];
      app.globalData._categories = categories;
      const firstCat = categories[0];
      let products = [];
      if (firstCat) {
        const prodRes = await get('/api/product/list?categoryId=' + firstCat.id);
        products = prodRes.data || [];
      }
      this.setData({
        categories,
        products,
        scrollTop: this.data.scrollTop === 0 ? 0.01 : 0,
        filteredProducts: products,
        currentCategory: firstCat || {},
        currentCategoryIndex: 0
      });
    } catch (e) {
      console.error('加载失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  async loadProducts(categoryId) {
    this.setData({ loading: true });
    try {
      const res = await get('/api/product/list?categoryId=' + categoryId);
      const products = res.data || [];
      this.setData({ products, filteredProducts: products });
    } catch (e) {
      console.error('加载商品失败', e);
    } finally {
      this.setData({ loading: false });
    }
  },

  onCategoryTap(e) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.categories[index];
    this.setData({
      currentCategoryIndex: index,
      currentCategory: category,
      searchText: '',
      scrollTop: this.data.scrollTop === 0 ? 0.01 : 0
    });
    this.loadProducts(category.id);
  },

  onSearchInput(e) {
    const searchText = e.detail.value;
    this.setData({ searchText });
    if (searchText) {
      const kw = searchText.toLowerCase();
      const filtered = this.data.products.filter(p =>
        p.name.toLowerCase().includes(kw) ||
        (p.description && p.description.toLowerCase().includes(kw))
      );
      this.setData({ filteredProducts: filtered });
    } else {
      this.setData({ filteredProducts: this.data.products });
    }
  },

  goProductDetail(e) {
    const id = e.currentTarget.dataset.id;
    navigatingAway = true
    wx.navigateTo({ url: '/pages/product/detail?id=' + id });
  },

  async onQuickAdd(e) {
    if (!app.checkLogin()) return;
    const productId = e.currentTarget.dataset.id;
    try {
      await post('/api/cart/add', { productId: productId, quantity: 1 });
      wx.showToast({ title: '已加入购物车', icon: 'success' });
      await app.loadCartCount();
    } catch (err) {
      console.error('加入购物车失败', err);
    }
  },

  onProductScrollLower() {
    // 可扩展：加载更多
  }
});
