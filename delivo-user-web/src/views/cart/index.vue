<script setup lang="ts">

import { computed } from 'vue';
import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';
import { showConfirmDialog, showToast, showSuccessToast } from 'vant';
import { useI18n } from 'vue-i18n';

const cartStore = useCartStore();
const router = useRouter();
const { t } = useI18n();

const handleQuantityChange = (id: string, value: number) => {
    if (value === 0) {
        
        cartStore.removeItem(id);
        showToast(t('cart.removed'));
    } else {
        cartStore.updateQuantity(id, value);
    }
};

const handleDelete = (id: string) => {
    cartStore.removeItem(id);
    showToast(t('cart.removed'));
};

const handleClearCart = () => {
    showConfirmDialog({
        title: t('cart.clear'),
        message: t('cart.clearConfirm'),
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel')
    }).then(() => {
        cartStore.clearCart();
        showSuccessToast(t('common.success'));
    }).catch(() => {
        
    });
};

const handleCheckout = () => {
    if (cartStore.isEmpty) {
        showToast(t('common.empty'));
        return;
    }
    
    router.push('/order/confirm');
};


const totalPrice = computed(() => cartStore.totalAmount * 100); 
const isCartEmpty = computed(() => cartStore.isEmpty);

const cartCount = computed(() => 
    cartStore.totalQuantity > 0 ? cartStore.totalQuantity : undefined
);

const formatFlavor = (flavorStr: string) => {
    try {
        const arr = JSON.parse(flavorStr);
        return Array.isArray(arr) ? arr.join(', ') : flavorStr;
    } catch {
        return flavorStr;
    }
};

</script>

<template>
  <div class="cart-page">
    
    <van-nav-bar 
        :title="t('cart.title')" 
        fixed 
        placeholder 
    >
        <template #right>
            <span 
                v-if="!isCartEmpty" 
                @click="handleClearCart" 
                class="clear-btn"
            >
                {{ t('cart.clear') }}
            </span>
        </template>
    </van-nav-bar>

    
    <div class="cart-content">
        
        <div v-if="isCartEmpty" class="empty-state">
            <div class="empty-text">
                {{ t('cart.emptyMsg') }}
            </div>
        </div>

        
        <div v-else class="cart-list">
            <van-swipe-cell
                v-for="item in cartStore.items"
                :key="item.id"
                class="cart-swipe-cell"
            >
                <van-card
                    :price="item.item.price.toFixed(2)"
                    :title="item.item.name"
                    :desc="item.item.description"
                    :thumb="item.item.image"
                    class="cart-card"
                >
                    <template #tags>
                        <van-tag 
                            plain 
                            type="primary"
                            v-if="item.type === 'combo'"
                        >
                            {{ t('menu.title').includes('Delivo') ? 'Combo' : '套餐' }}
                        </van-tag>
                        <van-tag
                            plain
                            type="primary"
                            v-if="item.dishFlavor"
                            class="flavor-tag"
                        >
                            {{ formatFlavor(item.dishFlavor) }}
                        </van-tag>
                    </template>
                    
                    <template #footer>
                        <div class="cart-card-footer">
                            <div class="subtotal">
                                {{ t('cart.total') }} ¥{{ (item.item.price * item.quantity).toFixed(2) }}
                            </div>
                            <van-stepper
                                :model-value="item.quantity"
                                min="0"
                                theme="round"
                                button-size="22"
                                disable-input
                                @change="(val) => handleQuantityChange(item.id, Number(val))"
                            />
                        </div>
                    </template>
                </van-card>

                
                <template #right>
                    <van-button
                        square
                        type="danger"
                        :text="t('common.cancel') /* fallback to delete verb or generic */"
                        class="delete-button"
                        @click="handleDelete(item.id)"
                    />
                </template>
            </van-swipe-cell>
        </div>
    </div>

    
    <van-submit-bar 
        v-if="!isCartEmpty"
        :price="totalPrice" 
        :button-text="t('cart.checkout')" 
        @submit="handleCheckout" 
        class="cart-submit-bar"
    />

    
    <van-tabbar route fixed placeholder>
        <van-tabbar-item replace to="/menu" icon="home-o">
            {{ t('tabbar.menu') }}
        </van-tabbar-item>
        <van-tabbar-item replace to="/cart" icon="cart-o" :badge="cartCount">
            {{ t('tabbar.cart') }}
        </van-tabbar-item>
        <van-tabbar-item replace to="/order" icon="orders-o">
            {{ t('tabbar.order') }}
        </van-tabbar-item>
        <van-tabbar-item replace to="/user" icon="user-o">
            {{ t('tabbar.user') }}
        </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>

.cart-page {
    background-color: #f7f8fa;
    min-height: 100vh;
    padding-bottom: 60px;
}

.cart-content {
    padding-top: 12px;
    min-height: calc(100vh - 120px);
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 100px;
}

.empty-text {
    font-size: 16px;
    color: #969799;
}

.cart-list {
    padding: 0 12px;
}

.cart-swipe-cell {
    margin-bottom: 12px;
    border-radius: 8px;
    overflow: hidden;
}

.cart-card {
    background-color: #fff;
    border-radius: 8px;
}

.cart-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
}

.subtotal {
    color: #ee0a24;
    font-size: 14px;
    font-weight: bold;
}

.delete-button {
    height: 100%;
}

.clear-btn {
    color: #ee0a24;
    font-size: 14px;
    cursor: pointer;
}

.cart-submit-bar {
    bottom: 50px;
}

.flavor-tag {
    margin-left: 4px;
}

:deep(.van-card__thumb) {
    width: 80px;
    height: 80px;
    border-radius: 8px;
}

:deep(.van-card__content) {
    min-height: 80px;
}

:deep(.van-submit-bar__bar) {
    padding: 0 16px;
}

</style>
