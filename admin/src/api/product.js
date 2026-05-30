import request from './request'

export function getProductList(params) {
  return request({ url: '/api/admin/product/list', params })
}

export function saveProduct(data) {
  return request({ url: '/api/admin/product/save', method: 'POST', data })
}

export function deleteProduct(id) {
  return request({ url: '/api/admin/product/delete/' + id, method: 'DELETE' })
}

export function updateProductStatus(id, status) {
  return request({ url: '/api/admin/product/status/' + id, params: { status }, method: 'PUT' })
}
