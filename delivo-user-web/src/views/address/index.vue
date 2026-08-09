<script setup lang="ts">

import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAddressStore } from '@/stores/address';
import { showFailToast, showSuccessToast } from 'vant';
import type { AddressListAddress } from 'vant';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const route = useRoute();
const addressStore = useAddressStore();
const { t } = useI18n();


const isSelectMode = computed(() => route.query.select === 'true');


const chosenAddressId = ref('');

/**
 * Load addresses on mount
 */
onMounted(async () => {
    try {
        await addressStore.fetchAddresses();
        
        // Set default address as chosen if available
        if (addressStore.defaultAddress) {
            chosenAddressId.value = addressStore.defaultAddress.id;
        }
    } catch (error: any) {
        showFailToast(t('common.fail'));
    }
});

const formattedAddresses = computed<AddressListAddress[]>(() => {
    return addressStore.addresses.map(addr => ({
        id: addr.id,
        name: addr.consignee,
        tel: addr.phone,
        address: `${addr.provinceName || ''}${addr.cityName || ''}${addr.districtName || ''}${addr.detail || ''}`,
        isDefault: addr.isDefault === 1
    }));
});

const handleAdd = () => {
    router.push('/address/edit');
};

const handleEdit = (item: AddressListAddress) => {
    router.push({
        path: '/address/edit',
        query: { id: item.id }
    });
};

const handleSelect = async (item: AddressListAddress) => {
    if (isSelectMode.value) {
        try {
            
            await addressStore.setDefaultAddress(String(item.id));
            showSuccessToast(t('common.success'));
            
            router.back();
        } catch (error: any) {
            showFailToast(t('common.fail'));
        }
    }
};

const goBack = () => {
    router.back();
};

</script>

<template>
  <div class="address-list-page">
    
    <van-nav-bar 
        :title="t('address.title')" 
        :left-text="t('common.back')"
        left-arrow 
        fixed
        placeholder
        @click-left="goBack" 
    />
    
    
    <div class="content">
        
        <div v-if="addressStore.loading" class="loading-wrapper">
            <van-loading type="spinner" size="32">加载中...</van-loading>
        </div>

        
        <van-address-list
            v-else
            v-model="chosenAddressId"
            :list="formattedAddresses"
            :default-tag-text="t('address.defaultTag')"
            @add="handleAdd"
            @edit="handleEdit"
            @select="handleSelect"
            :switchable="isSelectMode"
        >
            <template #top v-if="isSelectMode">
                <van-notice-bar
                    left-icon="info-o"
                    :text="t('address.select')"
                    color="#1989fa"
                    background="#ecf9ff"
                />
            </template>
        </van-address-list>
    </div>
  </div>
</template>

<style scoped>

.address-list-page {
    background-color: #f7f8fa;
    min-height: 100vh;
}

.content {
    min-height: calc(100vh - 46px);
}

.loading-wrapper {
    height: 50vh;
    display: flex;
    justify-content: center;
    align-items: center;
}

:deep(.van-address-list__add) {
    margin: 16px;
}

:deep(.van-address-item) {
    margin: 12px;
    border-radius: 8px;
}

</style>
