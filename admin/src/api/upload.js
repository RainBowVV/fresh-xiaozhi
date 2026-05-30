import request from './request'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/api/admin/upload/image', method: 'POST', data: formData })
}
