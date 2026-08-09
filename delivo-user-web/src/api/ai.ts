import request from '@/utils/request';

export const aiApi = {
        chat(message: string): Promise<string> {
        return request.post<any, string>('/user/ai/chat', message, {
            headers: {
                'Content-Type': 'application/json' 
            },
            timeout: 60000 
        });
    },

        clearMemory(): Promise<any> {
        return request.post<any, any>('/user/ai/clear');
    }
};
