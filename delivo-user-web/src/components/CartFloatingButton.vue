<script setup lang="ts">

import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

interface Props {
    totalQuantity: number;
    totalAmount: number;
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    disabled: false
});

const router = useRouter();
const { t } = useI18n();


const showButton = computed(() => props.totalQuantity > 0);


const handleCheckout = () => {
    if (!props.disabled && props.totalQuantity > 0) {
        router.push('/cart');
    }
};

</script>

<template>
  <transition name="slide-up">
    <div v-if="showButton" class="cart-floating-button">
      <div class="cart-floating-button__content">
        <div class="cart-floating-button__info">
          <van-badge :content="totalQuantity" max="99">
            <van-icon name="shopping-cart-o" size="24" color="#fff" />
          </van-badge>
          <span class="cart-floating-button__amount">
            ¥{{ totalAmount.toFixed(2) }}
          </span>
        </div>
        
        <van-button
          type="warning"
          round
          size="small"
          :disabled="disabled"
          @click="handleCheckout"
        >
          {{ t('cart.checkout') }}
        </van-button>
      </div>
    </div>
  </transition>
</template>

<style scoped>

.cart-floating-button {
    position: fixed;
    bottom: 60px;     left: 0;
    right: 0;
    padding: 0 16px;
    z-index: 99;
}

.cart-floating-button__content {
    background: linear-gradient(90deg, #ff6034 0%, #ee0a24 100%);
    border-radius: 24px;
    padding: 12px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 12px rgba(238, 10, 36, 0.3);
}

.cart-floating-button__info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.cart-floating-button__amount {
    color: #fff;
    font-size: 18px;
    font-weight: bold;
}

.slide-up-enter-active,
.slide-up-leave-active {
    transition: all 0.3s ease;
}

.slide-up-enter-from {
    transform: translateY(100%);
    opacity: 0;
}

.slide-up-leave-to {
    transform: translateY(100%);
    opacity: 0;
}

</style>
