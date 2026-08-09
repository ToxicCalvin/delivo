import request from '@/utils/request';
import type { Category, Dish, Combo } from '@/types';

export const menuService = {
    getCategories(): Promise<Category[]> {
        return request.get<any, Category[]>('/user/category/list');
    },

    getDishesByCategory(categoryId: string): Promise<Dish[]> {
        return request.get<any, Dish[]>(`/user/dish/list`, {
            params: { categoryId }
        });
    },

    getCombosByCategory(categoryId: string): Promise<Combo[]> {
        return request.get<any, Combo[]>(`/user/setmeal/list`, {
            params: { categoryId }
        });
    }
};
