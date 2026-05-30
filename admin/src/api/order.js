import request from './request'

export function getOrderList(params) {
  return request({ url: '/api/admin/order/list', params })
}

export function getOrderDetail(id) {
  return request({ url: '/api/admin/order/' + id })
}

export function updateOrderStatus(id, status) {
  return request({ url: '/api/admin/order/status/' + id, params: { status }, method: 'PUT' })
}
