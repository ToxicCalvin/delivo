<script setup lang="ts">

import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMenuStore } from '@/stores/menu';
import { useCartStore } from '@/stores/cart';
import { showToast, showSuccessToast, showImagePreview } from 'vant';
import type { Dish } from '@/types';
import FlavorDialog from '@/components/FlavorDialog.vue';
import CartFloatingButton from '@/components/CartFloatingButton.vue';
import { useI18n } from 'vue-i18n';

const route = useRoute();
const router = useRouter();
const menuStore = useMenuStore();
const cartStore = useCartStore();
const { t } = useI18n();

const dishId = route.params.id as string;
const dish = ref<Dish | null>(null);

const showFlavorDialog = ref(false);

const isAvailable = computed(() => dish.value?.status === 'available');

const hasFlavors = computed(() => {
    return dish.value?.flavors && dish.value.flavors.length > 0;
});

onMounted(async () => {
    
    const findDish = () => {
        let found = menuStore.dishes.find(d => String(d.id) === dishId);
        if (!found) found = menuStore.combos.find(c => String(c.id) === dishId) as any;
        return found;
    };

    let found = findDish();

    
    if (!found) {
        try {
            if (menuStore.categories.length === 0) {
                await menuStore.fetchCategories();
            }
            
            const promises = menuStore.categories.map(c => menuStore.fetchItemsByCategory(c.id));
            await Promise.allSettled(promises);
            
            
            found = findDish();
        } catch (error: any) {
            console.error('Failed to pre-load menu data', error);
        }
    }

    if (found) {
        dish.value = found as Dish;
    } else {
        showToast(t('dish.notFound'));
        router.back();
    }
});

const handleGoBack = () => {
    router.back();
};

const handleAddToCart = () => {
    if (!dish.value || !isAvailable.value) return;

    if (hasFlavors.value) {
        showFlavorDialog.value = true;
    } else {
        cartStore.addItem(dish.value, 'dish');
        showSuccessToast(t('cart.added'));
    }
};

const handleFlavorConfirm = (confirmedDish: Dish, flavorStr: string) => {
    cartStore.addItem(confirmedDish, 'dish', flavorStr);
    showSuccessToast(t('cart.added'));
};

const previewImage = () => {
    if (dish.value?.image) {
        showImagePreview([dish.value.image]);
    }
};

</script>

<template>
  <div class="dish-detail-page">
    <van-nav-bar 
      :title="t('dish.detailTitle')" 
      left-arrow 
      @click-left="handleGoBack"
      fixed
      placeholder
    />

    <div v-if="dish" class="detail-content">
      
      <div class="image-wrapper" @click="previewImage">
        <img :src="dish.image" :alt="dish.name" />
        <van-tag v-if="!isAvailable" type="danger" size="large" class="sold-out-tag">{{ t('common.soldOut') }}</van-tag>
      </div>

      
      <div class="info-section">
        <h2 class="dish-name">{{ dish.name }}</h2>
        <div class="price-row">
          <span class="price">¥{{ dish.price.toFixed(2) }}</span>
        </div>
        <p class="description" v-if="dish.description">{{ dish.description }}</p>
      </div>

      
      <div class="action-section">
        <van-button 
            type="primary" 
            block 
            round
            :disabled="!isAvailable"
            @click="handleAddToCart"
        >
            <span v-if="hasFlavors">{{ t('menu.selectFlavor') }}</span>
            <span v-else>{{ t('menu.addToCart') }}</span>
        </van-button>
      </div>

      
      <div class="reviews-section">
        <h3 class="section-title">{{ t('dish.reviews') }}</h3>
        <van-empty image="search" :description="t('dish.reviewsComingSoon')" />
      </div>
    </div>
    <div v-else class="loading-state">
      <van-loading type="spinner" size="24px">{{ t('common.loading') }}</van-loading>
    </div>

    
    <FlavorDialog 
        v-model:show="showFlavorDialog"
        :dish="dish"
        @confirm="handleFlavorConfirm"
    />

    
    <CartFloatingButton
        :total-quantity="cartStore.totalQuantity"
        :total-amount="cartStore.totalAmount"
    />
  </div>
</template>

<style scoped>

.dish-detail-page {
    background-color: #f7f8fa;
    min-height: 100vh;
    padding-bottom: 80px; }

.loading-state {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 60vh;
}

.image-wrapper {
    position: relative;
    width: 100%;
    height: 300px;
    background-color: #fff;
}

.image-wrapper img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.sold-out-tag {
    position: absolute;
    top: 16px;
    right: 16px;
}

.info-section {
    background-color: #fff;
    padding: 16px;
    margin-bottom: 12px;
}

.dish-name {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: bold;
    color: #323233;
}

.price-row {
    margin-bottom: 12px;
}

.price {
    font-size: 24px;
    color: #ee0a24;
    font-weight: bold;
}

.description {
    font-size: 14px;
    color: #969799;
    line-height: 1.5;
    margin: 0;
}

.action-section {
    background-color: #fff;
    padding: 16px;
    margin-bottom: 12px;
}

.reviews-section {
    background-color: #fff;
    padding: 16px;
    border-radius: 8px 8px 0 0;
}

.section-title {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: bold;
    color: #323233;
}

</style>
