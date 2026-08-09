import request from '@/utils/request'


export function getSetmealList(params) {
  return request({
    url: '/setmeal/page',
    method: 'get',
    params
  })
}


export function addSetmeal(data) {
  return request({
    url: '/setmeal',
    method: 'post',
    data
  })
}


export function getSetmealById(id) {
  return request({
    url: `/setmeal/${id}`,
    method: 'get'
  })
}


export function updateSetmeal(data) {
  return request({
    url: '/setmeal',
    method: 'put',
    data
  })
}


export function enableOrDisableSetmeal(params) {
  return request({
    url: `/setmeal/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}


export function deleteSetmealBatch(ids) {
  return request({
    url: '/setmeal',
    method: 'delete',
    params: { ids: ids.join(',') }
  })
}

