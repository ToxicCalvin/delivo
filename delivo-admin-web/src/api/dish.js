import request from '@/utils/request'


export function getDishList(params) {
  return request({
    url: '/dish/page',
    method: 'get',
    params
  })
}


export function addDish(data) {
  return request({
    url: '/dish',
    method: 'post',
    data
  })
}


export function getDishById(id) {
  return request({
    url: `/dish/${id}`,
    method: 'get'
  })
}


export function updateDish(data) {
  return request({
    url: '/dish',
    method: 'put',
    data
  })
}


export function enableOrDisableDish(params) {
  return request({
    url: `/dish/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}


export function deleteDishBatch(ids) {
  return request({
    url: '/dish',
    method: 'delete',
    params: { ids: ids.join(',') }
  })
}


export function getDishByCategoryId(categoryId) {
  return request({
    url: '/dish/list',
    method: 'get',
    params: { categoryId }
  })
}

