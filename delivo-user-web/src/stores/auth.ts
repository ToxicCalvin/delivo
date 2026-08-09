import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { storage } from '@/utils/storage';
import type { User } from '@/types';
import { authService } from '@/services/auth.service';

export const useAuthStore = defineStore('auth', () => {

    const token = ref<string | null>(storage.getItem<string>('auth_token'));
    const user = ref<User | null>(storage.getItem<User>('user_info'));


    const isAuthenticated = computed(() => !!token.value);
    const currentUser = computed(() => user.value);



    function setToken(newToken: string | null) {
        token.value = newToken;
        if (newToken) {
            storage.setItem('auth_token', newToken);
        } else {
            storage.removeItem('auth_token');
        }
    }

    function setUser(newUser: User | null) {
        user.value = newUser;
        if (newUser) {
            storage.setItem('user_info', newUser);
        } else {
            storage.removeItem('user_info');
        }
    }

    async function login(username: string, password: string): Promise<void> {
        try {
            const response = await authService.login(username, password);

            if (response && response.token) {
                setToken(response.token);
                setUser(response.user);
            } else {

                throw new Error('Invalid login response');
            }
        } catch (error) {

            setToken(null);
            setUser(null);
            throw error;
        }
    }

    function logout() {

        setToken(null);
        setUser(null);



        authService.logout().catch(console.error);
    }

    function loadUserFromStorage() {
        const storedToken = storage.getItem<string>('auth_token');
        const storedUser = storage.getItem<User>('user_info');

        if (storedToken) token.value = storedToken;
        if (storedUser) user.value = storedUser;
    }

    return {
        token,
        user,
        isAuthenticated,
        currentUser,
        login,
        logout,
        setToken,
        setUser,
        loadUserFromStorage
    };
});
