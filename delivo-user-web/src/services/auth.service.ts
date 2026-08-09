import request from '@/utils/request';
import type { AuthResponse } from '@/types';

export const authService = {
        async login(username: string, password: string): Promise<AuthResponse> {
        const data: any = await request.post('/user/user/accountLogin', {
            username: username,
            password: password
        });

        return {
            token: data.token,
            user: {
                id: String(data.id),
                username: username,
                phone: data.phone || '',
                avatar: data.avatar || ''
            }
        };
    },

    async register(username: string, password: string, name?: string, phone?: string): Promise<void> {
        await request.post('/user/user/register', {
            username,
            password,
            name,
            phone
        });
    },

        logout(): Promise<void> {
        return Promise.resolve();
    },
        updatePassword(password: string): Promise<void> {
        return request.put('/user/password', { password });
    }
};
