import request from '@/utils/request'


export function getBusinessData() {
  return request({
    url: '/workspace/businessData',
    method: 'get'
  })
}


export function getOrderOverView() {
  return request({
    url: '/workspace/overviewOrders',
    method: 'get'
  })
}


export function getDishOverView() {
  return request({
    url: '/workspace/overviewDishes',
    method: 'get'
  })
}


export function getSetmealOverView() {
  return request({
    url: '/workspace/overviewSetmeals',
    method: 'get'
  })
}

