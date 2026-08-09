import request from '@/utils/request';
import type { Address } from '@/types';

export const addressService = {
    getAddresses(): Promise<Address[]> {
        return request.get<any, Address[]>('/user/addressBook/list');
    },

    addAddress(address: Omit<Address, 'id'>): Promise<Address> {
        return request.post<any, Address>('/user/addressBook', address);
    },

    getDefaultAddress(): Promise<Address> {
        return request.get<any, Address>('/user/addressBook/default');
    },

    updateAddress(address: Address): Promise<void> {
        return request.put<any, void>('/user/addressBook', address);
    },

    setDefaultAddress(id: string): Promise<void> {
        return request.put<any, void>('/user/addressBook/default', { id });
    },

    deleteAddress(id: string): Promise<void> {
        return request.delete<any, void>('/user/addressBook', {
            params: { id }
        });
    },

    getAddressById(id: string): Promise<Address> {
        return request.get<any, Address>(`/user/addressBook/${id}`);
    }
};
