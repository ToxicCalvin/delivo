import type { Address, CartItem } from '@/types';

export const validators = {
    isValidPhone(phone: string): boolean {
        const phoneRegex = /^1[3-9]\d{9}$/;
        return phoneRegex.test(phone);
    },

    isValidAddress(address: Partial<Address>): boolean {
        if (!address) return false;



        const hasConsignee = !!address.consignee && address.consignee.trim().length >= 2;
        const hasValidPhone = !!address.phone && validators.isValidPhone(address.phone);
        const hasDetail = !!address.detail && address.detail.trim().length >= 5;


        const hasArea = !!address.provinceName && address.provinceName.trim().length > 0;

        return hasConsignee && hasValidPhone && hasDetail && hasArea;
    },

    isEmptyCart(items: CartItem[]): boolean {
        return !items || items.length === 0;
    }
};
