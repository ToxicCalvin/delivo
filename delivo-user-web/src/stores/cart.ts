import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { CartItem, Dish, Combo } from '@/types';
import { cartService } from '@/services/cart.service';

export const useCartStore = defineStore('cart', () => {
    
    const items = ref<CartItem[]>([]);

    
    const cartItems = computed(() => items.value);

    const totalQuantity = computed(() => {
        return items.value.reduce((total, item) => total + item.quantity, 0);
    });

    const totalAmount = computed(() => {
        return items.value.reduce((total, item) => {
            const price = item.item.price || 0;
            return total + (price * item.quantity);
        }, 0);
    });

    const isEmpty = computed(() => items.value.length === 0);

    

        async function fetchCartList() {
        try {
            const list = await cartService.getCartList();
            items.value = list.map((c: any) => ({
                id: c.dishFlavor ? `${String(c.dishId || c.setmealId)}-${c.dishFlavor}` : String(c.dishId || c.setmealId),
                type: c.setmealId ? 'combo' : 'dish',
                item: {
                    id: String(c.dishId || c.setmealId),
                    name: c.name,
                    price: c.amount,
                    image: c.image,
                    categoryId: '',
                    description: '',
                    status: 'available'
                } as Dish | Combo,
                quantity: c.number,
                dishFlavor: c.dishFlavor
            }));
        } catch (e) {
            console.error('Failed to fetch cart', e);
        }
    }

        async function addItem(product: Dish | Combo, type: 'dish' | 'combo', dishFlavor?: string) {
        const cartItemId = dishFlavor ? `${product.id}-${dishFlavor}` : String(product.id);
        const existingItem = items.value.find(i => i.id === cartItemId && i.type === type);

        if (existingItem) {
            existingItem.quantity++;
        } else {
            items.value.push({
                id: cartItemId,
                type,
                item: product,
                dishFlavor,
                quantity: 1
            });
        }

        try {
            await cartService.addToCart({
                dishId: type === 'dish' ? String(product.id) : undefined,
                setmealId: type === 'combo' ? String(product.id) : undefined,
                dishFlavor
            });
            await fetchCartList();
        } catch (e) {
            console.error('Failed to sync backend cart add', e);
            await fetchCartList(); 
        }
    }

        async function removeItem(itemId: string) {
        const index = items.value.findIndex(i => String(i.id) === String(itemId));
        if (index === -1) return;

        const item = items.value[index];
        if (!item) return;
        items.value.splice(index, 1); 

        try {
            for (let i = 0; i < item.quantity; i++) {
                await cartService.subFromCart({
                    dishId: item.type === 'dish' ? String(item.item.id) : undefined,
                    setmealId: item.type === 'combo' ? String(item.item.id) : undefined,
                    dishFlavor: item.dishFlavor
                });
            }
            await fetchCartList();
        } catch (e) {
            await fetchCartList(); 
        }
    }

        async function updateQuantity(itemId: string, quantity: number) {
        const item = items.value.find(i => String(i.id) === String(itemId));
        if (!item) return;

        const diff = quantity - item.quantity;
        if (quantity <= 0) {
            return removeItem(itemId);
        }

        item.quantity = quantity; 

        try {
            if (diff > 0) {
                for (let i = 0; i < diff; i++) {
                    await cartService.addToCart({
                        dishId: item.type === 'dish' ? String(item.item.id) : undefined,
                        setmealId: item.type === 'combo' ? String(item.item.id) : undefined,
                        dishFlavor: item.dishFlavor
                    });
                }
            } else if (diff < 0) {
                for (let i = 0; i < Math.abs(diff); i++) {
                    await cartService.subFromCart({
                        dishId: item.type === 'dish' ? String(item.item.id) : undefined,
                        setmealId: item.type === 'combo' ? String(item.item.id) : undefined,
                        dishFlavor: item.dishFlavor
                    });
                }
            }
            await fetchCartList();
        } catch (e) {
            await fetchCartList(); 
        }
    }

        async function clearCart() {
        items.value = []; 
        try {
            await cartService.cleanCart();
            await fetchCartList();
        } catch (e) {
            await fetchCartList(); 
        }
    }

    
    fetchCartList();

    return {
        items,
        cartItems,
        totalQuantity,
        totalAmount,
        isEmpty,
        fetchCartList,
        addItem,
        removeItem,
        updateQuantity,
        clearCart
    };
});
