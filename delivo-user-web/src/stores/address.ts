import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Address } from '@/types';
import { addressService } from '@/services/address.service';

export const useAddressStore = defineStore('address', () => {
    
    const addresses = ref<Address[]>([]);
    const loading = ref<boolean>(false);
    const error = ref<string | null>(null);

    
    const allAddresses = computed(() => addresses.value);

    const defaultAddress = computed(() =>
        addresses.value.find(addr => addr.isDefault) || null
    );

    const hasAddresses = computed(() => addresses.value.length > 0);

        async function fetchAddresses(): Promise<void> {
        loading.value = true;
        error.value = null;

        try {
            const response = await addressService.getAddresses();
            addresses.value = response || [];
        } catch (e: any) {
            error.value = e.message || '获取地址列表失败';
            throw e;
        } finally {
            loading.value = false;
        }
    }

        async function addAddress(address: Omit<Address, 'id'>): Promise<void> {
        loading.value = true;
        error.value = null;

        try {
            const response = await addressService.addAddress(address);

            
            if (address.isDefault === 1) {
                addresses.value = addresses.value.map(addr => ({
                    ...addr,
                    isDefault: 0
                }));
            }

            
            if (response) {
                addresses.value.push(response);
            } else {
                
                await fetchAddresses();
            }
        } catch (e: any) {
            error.value = e.message || '添加地址失败';
            throw e;
        } finally {
            loading.value = false;
        }
    }

        async function updateAddress(address: Address): Promise<void> {
        loading.value = true;
        error.value = null;

        try {
            await addressService.updateAddress(address);

            
            const index = addresses.value.findIndex(addr => addr.id === address.id);
            if (index !== -1) {
                
                if (address.isDefault === 1) {
                    addresses.value = addresses.value.map(addr => ({
                        ...addr,
                        isDefault: addr.id === address.id ? 1 : 0
                    }));
                } else {
                    addresses.value[index] = address;
                }
            }
        } catch (e: any) {
            error.value = e.message || '更新地址失败';
            throw e;
        } finally {
            loading.value = false;
        }
    }

        async function deleteAddress(id: string): Promise<void> {
        loading.value = true;
        error.value = null;

        try {
            await addressService.deleteAddress(id);

            
            addresses.value = addresses.value.filter(addr => addr.id !== id);
        } catch (e: any) {
            error.value = e.message || '删除地址失败';
            throw e;
        } finally {
            loading.value = false;
        }
    }

        async function setDefaultAddress(id: string): Promise<void> {
        loading.value = true;
        error.value = null;

        try {
            await addressService.setDefaultAddress(id);

            
            addresses.value = addresses.value.map(addr => ({
                ...addr,
                isDefault: addr.id === id ? 1 : 0
            }));
        } catch (e: any) {
            error.value = e.message || '设置默认地址失败';
            throw e;
        } finally {
            loading.value = false;
        }
    }

        function getAddressById(id: string): Address | undefined {
        return addresses.value.find(addr => addr.id === id);
    }

    return {
        
        addresses,
        loading,
        error,

        
        allAddresses,
        defaultAddress,
        hasAddresses,

        
        fetchAddresses,
        addAddress,
        updateAddress,
        deleteAddress,
        setDefaultAddress,
        getAddressById
    };
});
