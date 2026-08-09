<script setup lang="ts">

import { computed } from 'vue';
import type { Dish, Combo } from '@/types';
import { useI18n } from 'vue-i18n';

interface Props {
    item: Dish | Combo;
    quantity?: number;
}

interface Emits {
    (e: 'add', item: Dish | Combo): void;
    (e: 'updateQuantity', itemId: string, quantity: number): void;
    (e: 'preview', imageUrl: string): void;
    (e: 'card-click', item: Dish | Combo): void;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
    quantity: 0
});

const emit = defineEmits<Emits>();


const isAvailable = computed(() => props.item.status === 'available');


const hasFlavors = computed(() => {
    const dish = props.item as Dish;
    return dish.flavors && dish.flavors.length > 0;
});


const handleAdd = () => {
    if (isAvailable.value) {
        emit('add', props.item);
    }
};


const handleQuantityChange = (value: number) => {
    emit('updateQuantity', props.item.id, value);
};


const handleImageClick = () => {
    emit('preview', props.item.image);
};


const handleCardClick = () => {
    emit('card-click', props.item);
};

</script>

<template>
  <div class="dish-card-wrapper" @click="handleCardClick">
    <van-card
      :price="item.price.toFixed(2)"
      :desc="item.description"
      :title="item.name"
      :thumb="item.image"
      @click-thumb.stop="handleImageClick"
      :class="{ 'dish-card--unavailable': !isAvailable }"
      class="dish-card"
    >
    <template #tags>
      <van-tag plain type="danger" v-if="!isAvailable">{{ t('common.soldOut') }}</van-tag>
    </template>
    
    <template #footer>
      <div class="dish-card__footer" @click.stop>
        
        <van-stepper 
          v-if="quantity > 0 && !hasFlavors"
          :model-value="quantity" 
          min="0" 
          theme="round" 
          button-size="22" 
          disable-input
          :disabled="!isAvailable"
          @change="handleQuantityChange"
        />
        
        
        <van-button 
          v-else-if="!hasFlavors" 
          icon="plus" 
          type="primary" 
          size="mini" 
          round 
          :disabled="!isAvailable"
          @click="handleAdd"
        >
          {{ t('menu.addToCart') }}
        </van-button>

        
        <van-button
          v-else
          type="primary"
          size="small"
          round
          :disabled="!isAvailable"
          @click="handleAdd"
          class="flavor-btn"
        >
          {{ t('menu.selectFlavor') }}<span v-if="quantity > 0" class="flavor-badge">{{ quantity }}</span>
        </van-button>
      </div>
    </template>
    </van-card>
  </div>
</template>

<style scoped>

.dish-card-wrapper {
    cursor: pointer;
}
.flavor-btn {
    position: relative;
    padding: 0 12px;
}
.flavor-badge {
    position: absolute;
    top: -6px;
    right: -6px;
    background-color: #ee0a24;
    color: white;
    font-size: 10px;
    padding: 0 4px;
    border-radius: 8px;
    min-width: 14px;
    text-align: center;
    border: 1px solid #fff;
}
.dish-card {
    margin-bottom: 12px;
}

.dish-card--unavailable {
    opacity: 0.6;
}

.dish-card__footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 8px;
}

</style>
