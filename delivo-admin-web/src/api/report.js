import request from '@/utils/request'


export function getTurnoverStatistics(begin, end) {
  return request({
    url: '/report/turnoverStatistics',
    method: 'get',
    params: { begin, end }
  })
}


export function getUserStatistics(begin, end) {
  return request({
    url: '/report/userStatistics',
    method: 'get',
    params: { begin, end }
  })
}


export function getOrderStatistics(begin, end) {
  return request({
    url: '/report/ordersStatistics',
    method: 'get',
    params: { begin, end }
  })
}


export function getSalesTop10(begin, end) {
  return request({
    url: '/report/top10',
    method: 'get',
    params: { begin, end }
  })
}


export function exportBusinessData() {
  return request({
    url: '/report/export',
    method: 'get',
    responseType: 'blob'
  })
}

