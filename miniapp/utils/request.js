const request = (options) => {
  return new Promise((resolve, reject) => {
    const app = getApp();
    const { url, method = 'GET', data, header = {}, timeout = 60000 } = options;

    const baseUrl = app.globalData.baseUrl;
    const token = app.globalData.token;

    const headers = {
      'Content-Type': 'application/json',
      ...header
    };

    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    wx.request({
      url: baseUrl + url,
      method,
      data,
      header: headers,
      timeout,
      success(res) {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data);
          } else if (res.data.code === 401) {
            app.globalData.token = '';
            wx.removeStorageSync('token');
            wx.showToast({ title: '请先登录', icon: 'none' });
            setTimeout(() => {
              wx.switchTab({ url: '/pages/mine/mine' });
            }, 1500);
            reject(res.data);
          } else {
            wx.showToast({ title: res.data.msg || '请求失败', icon: 'none' });
            reject(res.data);
          }
        } else {
          wx.showToast({ title: '网络异常', icon: 'none' });
          reject(res);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络连接失败', icon: 'none' });
        reject(err);
      }
    });
  });
};

const get = (url, data) => request({ url, method: 'GET', data });
const post = (url, data) => request({ url, method: 'POST', data });
const put = (url, data) => request({ url, method: 'PUT', data });
const del = (url, data) => request({ url, method: 'DELETE', data });

module.exports = { request, get, post, put, del };
