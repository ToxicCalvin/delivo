import request from '@/utils/request'


export function getEmployeeList(params) {
  return request({
    url: '/employee/page',
    method: 'get',
    params
  })
}


export function addEmployee(data) {
  return request({
    url: '/employee',
    method: 'post',
    data
  })
}


export function getEmployeeById(id) {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}


export function updateEmployee(data) {
  return request({
    url: '/employee',
    method: 'put',
    data
  })
}


export function enableOrDisableEmployee(params) {
  return request({
    url: `/employee/status/${params.status}`,
    method: 'post',
    params: { id: params.id }
  })
}

