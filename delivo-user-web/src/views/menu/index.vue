<script setup lang="ts">

import { ref, onMounted, computed } from 'vue';
import { useMenuStore } from '@/stores/menu';
import { useCartStore } from '@/stores/cart';
import { showToast, showImagePreview, showFailToast, showSuccessToast } from 'vant';
import type { Dish, Combo } from '@/types';
import DishCard from '@/components/DishCard.vue';
import CartFloatingButton from '@/components/CartFloatingButton.vue';
import AiChatFloatingButton from '@/components/AiChatFloatingButton.vue';
import FlavorDialog from '@/components/FlavorDialog.vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

const menuStore = useMenuStore();
const cartStore = useCartStore();
const router = useRouter();
const { t } = useI18n();

const activeIndex = ref(0); 
const showFlavorDialog = ref(false);
const currentFlavorDish = ref<Dish | null>(null);

onMounted(async () => {
    try {
        await menuStore.fetchCategories();
        
        if (menuStore.categories.length > 0) {
            await selectCategory(0);
        }
    } catch (error: any) {
        showFailToast(t('menu.loadFail', { msg: error.message || t('common.fail') }));
    }
});

const selectCategory = async (index: number) => {
    activeIndex.value = index;
    const category = menuStore.categories[index];
    
    if (category) {
        try {
            await menuStore.setCurrentCategory(category.id);
        } catch (error: any) {
            showFailToast(t('menu.catFail', { msg: error.message || t('common.fail') }));
        }
    }
};

const handleAddToCartClick = (item: Dish | Combo) => {
    const dish = item as Dish;
    if (dish.flavors && dish.flavors.length > 0) {
        currentFlavorDish.value = dish;
        showFlavorDialog.value = true;
    } else {
        handleAddToCart(item);
    }
};

const handleAddToCart = (item: Dish | Combo) => {
    
    const currentCategory = menuStore.categories.find(
        c => c.id === menuStore.currentCategoryId
    );
    const type = currentCategory?.type === 'combo' ? 'combo' : 'dish';
    
    cartStore.addItem(item, type);
    showSuccessToast(t('cart.added'));
};

const handleFlavorConfirm = (dish: Dish, flavorStr: string) => {
    cartStore.addItem(dish, 'dish', flavorStr);
    showSuccessToast(t('cart.added'));
};

const updateCartQuantity = (itemId: string, quantity: number) => {
    if (quantity === 0) {
        cartStore.removeItem(itemId);
        showToast(t('cart.removed'));
    } else {
        cartStore.updateQuantity(itemId, quantity);
    }
};

const getCartQuantity = (itemId: string): number => {
    const cartItem = cartStore.items.find(i => i.item.id === itemId);
    return cartItem ? cartItem.quantity : 0;
};

const previewImage = (imageUrl: string) => {
    if (imageUrl) {
        showImagePreview([imageUrl]);
    }
};


const cartCount = computed(() => 
    cartStore.totalQuantity > 0 ? cartStore.totalQuantity : undefined
);

const cartTotalAmount = computed(() => cartStore.totalAmount);
const cartTotalQuantity = computed(() => cartStore.totalQuantity);

const topDishes = computed(() => {
    return menuStore.dishes.slice(0, 5);
});

const goToDishDetail = (item: Dish | Combo) => {
    router.push(`/dish/${item.id}`);
};

</script>

<template>
  <div class="menu-container">
    
    <van-nav-bar 
      :title="t('menu.title')" 
      fixed 
      placeholder 
      z-index="100"
    />

    
    <div class="carousel-wrapper" v-if="topDishes.length > 0">
        <van-swipe class="my-swipe" :autoplay="2000" indicator-color="white">
            <van-swipe-item v-for="item in topDishes" :key="item.id" @click="goToDishDetail(item)">
                <div class="swipe-item-content">
                    <img :src="item.image || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'" class="swipe-img" />
                    <div class="swipe-title-overlay">
                        <span>{{ item.name }}</span>
                    </div>
                </div>
            </van-swipe-item>
        </van-swipe>
    </div>
    
    
    <div class="menu-content">
        
        <div class="sidebar-wrapper">
            <van-sidebar v-model="activeIndex" @change="selectCategory">
                <van-sidebar-item 
                    v-for="category in menuStore.categories" 
                    :key="category.id" 
                    :title="category.name" 
                />
            </van-sidebar>
        </div>

        
        <div class="list-wrapper">
            
            <div v-if="menuStore.loading" class="state-wrapper">
                <van-loading type="spinner" size="32">{{ t('common.loading') }}</van-loading>
            </div>
            
            
            <div v-else-if="menuStore.currentCategoryItems.length === 0" class="state-wrapper">
                <van-empty :description="t('menu.emptyCat')" />
            </div>

            
            <div v-else class="items-list">
                <DishCard
                    v-for="item in menuStore.currentCategoryItems"
                    :key="item.id"
                    :item="item"
                    :quantity="getCartQuantity(item.id)"
                    @add="handleAddToCartClick"
                    @update-quantity="updateCartQuantity"
                    @preview="previewImage"
                    @card-click="goToDishDetail"
                />
            </div>
        </div>
    </div>

    
    <CartFloatingButton
        :total-quantity="cartTotalQuantity"
        :total-amount="cartTotalAmount"
    />

    
    <AiChatFloatingButton />

    
    <FlavorDialog 
        v-model:show="showFlavorDialog"
        :dish="currentFlavorDish"
        @confirm="handleFlavorConfirm"
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

.menu-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #f7f8fa;
}

.menu-content {
    flex: 1;
    display: flex;
    overflow: hidden;
}

.carousel-wrapper {
    padding: 12px 12px 0 12px;
    background-color: #fff;
    flex-shrink: 0;
}

.my-swipe {
    border-radius: 8px;
    overflow: hidden;
    height: 140px;
}

.swipe-item-content {
    position: relative;
    width: 100%;
    height: 100%;
}

.swipe-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.swipe-title-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 12px 8px 8px 8px;
    background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
    color: white;
    font-size: 14px;
    font-weight: bold;
}

.sidebar-wrapper {
    width: 85px;
    background-color: #f7f8fa;
    overflow-y: auto;
    height: 100%;
}

.list-wrapper {
    flex: 1;
    background-color: #fff;
    overflow-y: auto;
    height: 100%;
    padding: 12px;
    padding-bottom: 120px; }

.state-wrapper {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.items-list {
    display: flex;
    flex-direction: column;
}

:deep(.van-sidebar-item--select) {
    color: var(--van-primary-color);
    font-weight: bold;
}

:deep(.van-sidebar-item--select::before) {
    background-color: var(--van-primary-color);
}

</style>
