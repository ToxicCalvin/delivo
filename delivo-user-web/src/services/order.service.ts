import request from '@/utils/request';
import type { Order } from '@/types';

export const orderService = {
    createOrder(orderData: {
        addressBookId: string;
        payMethod: number;
        remark: string;
        amount: number;
        estimatedDeliveryTime: string;
        deliveryStatus: number;
        packAmount: number;
        tablewareNumber: number;
        tablewareStatus: number;
    }): Promise<any> {
        return request.post('/user/order/submit', orderData);
    },

    mockPayment(paymentData: { orderNumber: string; payMethod: number }): Promise<any> {
        return request.put('/user/order/mockPayment', paymentData);
    },

    getOrderHistory(page: number, pageSize: number): Promise<{ records: Order[] }> {
        return request.get('/user/order/historyOrders', {
            params: { page, pageSize }
        });
    },

    getOrderDetail(id: string): Promise<Order> {
        return request.get(`/user/order/orderDetail/${id}`);
    },

    cancelOrder(id: string): Promise<void> {
        return request.put(`/user/order/cancel/${id}`);
    },

    repetitionOrder(id: string): Promise<void> {
        return request.post(`/user/order/repetition/${id}`);
    },

    reminderOrder(id: string): Promise<void> {
        return request.get(`/user/order/reminder/${id}`);
    }
};
