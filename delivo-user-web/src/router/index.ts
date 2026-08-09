
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        redirect: '/menu'
    },
    {
        path: '/menu',
        name: 'Menu',
        component: () => import('@/views/menu/index.vue'),
        meta: { requiresAuth: true, title: '点餐' }
    },
    {
        path: '/dish/:id',
        name: 'DishDetail',
        component: () => import('@/views/dish/detail.vue'),
        meta: { requiresAuth: true, title: '菜品详情' }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/auth/login.vue'),
        meta: { requiresAuth: false, title: '登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/auth/register.vue'),
        meta: { requiresAuth: false, title: '注册' }
    },
    {
        path: '/user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
    },
    {
        path: '/cart',
        name: 'Cart',
        component: () => import('@/views/cart/index.vue'),
        meta: { requiresAuth: true, title: '购物车' }
    },
    {
        path: '/order/confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/order/confirm.vue'),
        meta: { requiresAuth: true, title: '订单确认' }
    },
    {
        path: '/order',
        name: 'OrderList',
        component: () => import('@/views/order/index.vue'),
        meta: { requiresAuth: true, title: '订单历史' }
    },
    {
        path: '/order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { requiresAuth: true, title: '订单详情' }
    },
    {
        path: '/address',
        name: 'AddressList',
        component: () => import('@/views/address/index.vue'),
        meta: { requiresAuth: true, title: '地址管理' }
    },
    {
        path: '/address/edit',
        name: 'AddressEdit',
        component: () => import('@/views/address/edit.vue'),
        meta: { requiresAuth: true, title: '编辑地址' }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});


router.beforeEach((to, _from, next) => {
    const authStore = useAuthStore();
    console.log('Router Guard: to=', to.path, 'auth=', authStore.isAuthenticated);

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        next('/login');
    } else if ((to.path === '/login' || to.path === '/register') && authStore.isAuthenticated) {
        next('/');
    } else {
        next();
    }
});

export default router;
