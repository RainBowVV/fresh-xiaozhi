import request from './request'

export function getCategoryList(params) {
  return request({ url: '/api/admin/category/list', params })
}

export function saveCategory(data) {
  return request({ url: '/api/admin/category/save', method: 'POST', data })
}

export function deleteCategory(id) {
  return request({ url: '/api/admin/category/delete/' + id, method: 'DELETE' })
}
