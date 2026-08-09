
import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import { showToast } from 'vant';
import { storage } from '@/utils/storage';



const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';


const service: AxiosInstance = axios.create({
    baseURL: BASE_URL,
    timeout: 10000, 
    headers: {
        'Content-Type': 'application/json',
    },
});


service.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = storage.getItem<string>('auth_token');
        if (token && config.headers) {
            config.headers['authentication'] = token;
        }
        return config;
    },
    (error: AxiosError) => {
        
        return Promise.reject(error);
    }
);


service.interceptors.response.use(
    (response: AxiosResponse) => {
        const res = response.data;

        
        if (res.code === 1) {
            return res.data;
        }

        
        const errMsg = res.msg || '请求失败';
        showToast({
            type: 'fail',
            message: errMsg,
            duration: 3000,
            closeOnClick: true,
        });
        return Promise.reject(new Error(errMsg));
    },
    (error: AxiosError) => {
        if (!error.response) {
            
            showToast({
                type: 'fail',
                message: 'Network Error: Please check your connection',
                duration: 3000,
                closeOnClick: true,
            });
            return Promise.reject(new Error('NETWORK_ERROR'));
        }

        const { status } = error.response;

        switch (status) {
            case 401:
                
                showToast({
                    type: 'fail',
                    message: 'Session expired, please login again',
                    duration: 3000,
                    closeOnClick: true,
                });
                storage.removeItem('auth_token');
                storage.removeItem('user_info');
                
                
                
                if (window.location.pathname !== '/login') {
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 1500);
                }
                break;

            case 403:
                showToast({
                    type: 'fail',
                    message: 'Permission denied',
                    duration: 3000,
                    closeOnClick: true,
                });
                break;

            case 404:
                showToast({
                    type: 'fail',
                    message: 'Resource not found',
                    duration: 3000,
                    closeOnClick: true,
                });
                break;

            case 500:
                showToast({
                    type: 'fail',
                    message: 'Server Error: Please try again later',
                    duration: 3000,
                    closeOnClick: true,
                });
                break;

            default:
                showToast({
                    type: 'fail',
                    message: `Error: ${error.message}`,
                    duration: 3000,
                    closeOnClick: true,
                });
        }

        return Promise.reject(error);
    }
);

export default service;
