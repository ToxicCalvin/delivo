import request from '@/utils/request'


export function getOrderList(params) {
  return request({
    url: '/order/conditionSearch',
    method: 'get',
    params
  })
}


export function getOrderDetail(id) {
  return request({
    url: `/order/details/${id}`,
    method: 'get'
  })
}


export function cancelOrder(data) {
  return request({
    url: '/order/cancel',
    method: 'put',
    data
  })
}


export function confirmOrder(data) {
  return request({
    url: '/order/confirm',
    method: 'put',
    data
  })
}


export function rejectionOrder(data) {
  return request({
    url: '/order/rejection',
    method: 'put',
    data
  })
}


export function deliveryOrder(id) {
  return request({
    url: `/order/delivery/${id}`,
    method: 'put'
  })
}


export function completeOrder(id) {
  return request({
    url: `/order/complete/${id}`,
    method: 'put'
  })
}

