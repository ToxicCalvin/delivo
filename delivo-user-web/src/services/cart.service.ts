import request from '@/utils/request';
import type { CartItem } from '@/types';

export interface CartDTO {
    dishId?: string;
    setmealId?: string;
    dishFlavor?: string;
}

export const cartService = {
        getCartList(): Promise<CartItem[]> {
        return request.get('/user/shoppingCart/list');
    },

        addToCart(data: CartDTO): Promise<any> {
        return request.post('/user/shoppingCart/add', data);
    },

        subFromCart(data: CartDTO): Promise<any> {
        return request.post('/user/shoppingCart/sub', data);
    },

        cleanCart(): Promise<any> {
        return request.delete('/user/shoppingCart/clean');
    }
};
