<script setup lang="ts">

import { ref, watch } from 'vue';
import type { Dish } from '@/types';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const props = defineProps<{
    show: boolean;
    dish: Dish | null;
}>();

const emit = defineEmits<{
    (e: 'update:show', value: boolean): void;
    (e: 'confirm', dish: Dish, flavorString: string): void;
}>();

const selectedFlavors = ref<Record<string, string>>({});


watch(() => props.show, (newVal) => {
    if (newVal && props.dish && props.dish.flavors && props.dish.flavors.length > 0) {
        selectedFlavors.value = {};
        props.dish.flavors.forEach(flavor => {
            try {
                const options = JSON.parse(flavor.value);
                if (options && options.length > 0) {
                    selectedFlavors.value[flavor.name] = options[0]; 
                }
            } catch (e) {
                console.error('Failed to parse flavors', e);
            }
        });
    }
});

const getFlavorOptions = (flavorStr: string): string[] => {
    try {
        return JSON.parse(flavorStr);
    } catch {
        return [];
    }
};

const handleOptionClick = (flavorName: string, option: string) => {
    selectedFlavors.value[flavorName] = option;
};

const handleClose = () => {
    emit('update:show', false);
};

const handleConfirm = () => {
    if (!props.dish) return;
    
    const flavorArr = Object.values(selectedFlavors.value);
    const flavorStr = JSON.stringify(flavorArr);

    emit('confirm', props.dish, flavorStr);
    emit('update:show', false);
};

</script>

<template>
  <van-popup
    :show="show"
    @update:show="$emit('update:show', $event)"
    position="bottom"
    round
    closeable
    @close="handleClose"
    class="flavor-popup"
  >
    <div class="flavor-dialog" v-if="dish">
      <div class="header">
        <h3 class="title">{{ dish.name }}</h3>
      </div>
      
      <div class="content">
        <div 
            class="flavor-group" 
            v-for="flavor in dish.flavors" 
            :key="flavor.name"
        >
          <div class="flavor-name">{{ flavor.name }}</div>
          <div class="flavor-options">
            <span 
                v-for="option in getFlavorOptions(flavor.value)" 
                :key="option"
                class="flavor-option"
                :class="{ active: selectedFlavors[flavor.name] === option }"
                @click="handleOptionClick(flavor.name, option)"
            >
              {{ option }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="footer">
        <div class="price">¥{{ dish.price.toFixed(2) }}</div>
        <van-button type="primary" round block @click="handleConfirm">{{ t('menu.addToCart') }}</van-button>
      </div>
    </div>
  </van-popup>
</template>

<style scoped>

.flavor-popup {
    max-height: 80vh;
}

.flavor-dialog {
    padding: 24px 16px;
    display: flex;
    flex-direction: column;
}

.header {
    text-align: center;
    margin-bottom: 24px;
}

.title {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #323233;
}

.content {
    margin-bottom: 24px;
}

.flavor-group {
    margin-bottom: 16px;
}

.flavor-name {
    font-size: 14px;
    color: #323233;
    margin-bottom: 12px;
}

.flavor-options {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.flavor-option {
    padding: 6px 16px;
    background-color: #f7f8fa;
    border-radius: 16px;
    font-size: 13px;
    color: #323233;
    border: 1px solid transparent;
    transition: all 0.2s;
    cursor: pointer;
}

.flavor-option.active {
    background-color: var(--van-primary-color);
    color: #fff;
    border-color: var(--van-primary-color);
}

.footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #ebedf0;
}

.price {
    font-size: 20px;
    color: #ee0a24;
    font-weight: bold;
    margin-right: 16px;
}

</style>
