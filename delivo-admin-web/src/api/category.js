import request from '@/utils/request'


export function getCategoryList(params) {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}


export function getCategoryByType(type) {
  return request({
    url: '/category/list',
    method: 'get',
    params: { type }
  })
}


export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}


export function getCategoryById(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}


export function updateCategory(data) {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}


export function enableOrDisableCategory(params) {
  return request({
    url: `/category/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}


export function deleteCategory(id) {
  return request({
    url: `/category`,
    method: 'delete',
    params: { id }
  })
}

