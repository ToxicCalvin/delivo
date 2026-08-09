import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/index.vue') },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/index.vue') },
      { path: 'employee', name: 'Employee', component: () => import('@/views/employee/index.vue') },
      { path: 'category', name: 'Category', component: () => import('@/views/category/index.vue') },
      { path: 'dish', name: 'Dish', component: () => import('@/views/dish/index.vue') },
      { path: 'setmeal', name: 'Setmeal', component: () => import('@/views/setmeal/index.vue') },
      { path: 'order', name: 'Order', component: () => import('@/views/order/index.vue') },
      { path: 'report', name: 'Report', component: () => import('@/views/report/index.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    token ? next('/') : next()
  } else {
    token ? next() : next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

export default router
