<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAddressStore } from '@/stores/address';
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant';
import { useI18n } from 'vue-i18n';

const router = useRouter();
const route = useRoute();
const addressStore = useAddressStore();
const { t } = useI18n();


const addressId = route.query.id as string;
const isEditMode = !!addressId;


const formData = ref({
    name: '',
    tel: '',
    area: '',
    addressDetail: '',
    isDefault: false
});

/**
 * Load address data in edit mode
 */
onMounted(async () => {
    if (isEditMode) {
        try {
            // Fetch addresses if not loaded
            if (addressStore.addresses.length === 0) {
                await addressStore.fetchAddresses();
            }

            const address = addressStore.getAddressById(addressId);

            if (address) {
                formData.value = {
                    name: address.consignee,
                    tel: address.phone,
                    // Combine all region name parts for backward compatibility
                    area: `${address.provinceName || ''}${address.cityName || ''}${address.districtName || ''}`.trim(),
                    addressDetail: address.detail,
                    isDefault: address.isDefault === 1
                };
            } else {
                showFailToast(t('common.empty'));
                router.back();
            }
        } catch {
            showFailToast(t('common.fail'));
        }
    }
});

const handleSave = async () => {
    const { name, tel, area, addressDetail, isDefault } = formData.value;

    
    if (!name || !tel || !area || !addressDetail) {
        showFailToast(t('common.empty'));
        return;
    }

    
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(tel)) {
        showFailToast(t('common.fail'));
        return;
    }

    try {
        const addressData = {
            consignee: name,
            phone: tel,
            sex: '1',
            provinceCode: '',
            provinceName: area,
            cityCode: '',
            cityName: '',
            districtCode: '',
            districtName: '',
            detail: addressDetail,
            label: '家',
            isDefault: isDefault ? 1 : 0
        };

        if (isEditMode) {
            await addressStore.updateAddress({ id: addressId, ...addressData });
        } else {
            await addressStore.addAddress(addressData);
        }

        showSuccessToast(t('common.success'));
        router.back();
    } catch (error: any) {
        showFailToast(error.message || t('common.fail'));
    }
};

const handleDelete = () => {
    showConfirmDialog({
        title: t('address.delete'),
        message: t('common.empty'),
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        confirmButtonColor: '#ee0a24'
    }).then(async () => {
        try {
            await addressStore.deleteAddress(addressId);
            showSuccessToast(t('common.success'));
            router.back();
        } catch {
            showFailToast(t('common.fail'));
        }
    }).catch(() => {
        
    });
};

const goBack = () => {
    router.back();
};

</script>

<template>
  <div class="address-edit-page">
    
    <van-nav-bar
        :title="isEditMode ? t('address.editTitle') : t('address.addTitle')"
        :left-text="t('common.back')"
        left-arrow
        fixed
        placeholder
        @click-left="goBack"
    />

    
    <van-form @submit="handleSave">
        <van-cell-group inset>
            <van-field
                v-model="formData.name"
                name="name"
                :label="t('address.name')"
                :placeholder="t('address.name')"
                :rules="[{ required: true, message: t('common.empty') }]"
            />
            <van-field
                v-model="formData.tel"
                name="tel"
                :label="t('address.phone')"
                :placeholder="t('address.phone')"
                type="tel"
                :rules="[{ required: true, pattern: /^1[3-9]\d{9}$/, message: t('common.fail') }]"
            />
            <van-field
                v-model="formData.area"
                name="area"
                :label="t('address.area')"
                :placeholder="t('address.area')"
                :rules="[{ required: true, message: t('address.areaRequired') }]"
            />
            <van-field
                v-model="formData.addressDetail"
                name="addressDetail"
                :label="t('address.detail')"
                :placeholder="t('address.detail')"
                :rules="[{ required: true, message: t('address.detailRequired') }]"
            />
        </van-cell-group>

        <van-cell-group inset style="margin-top: 12px">
            <van-cell :title="t('address.setDefault')">
                <template #right-icon>
                    <van-switch v-model="formData.isDefault" size="24" />
                </template>
            </van-cell>
        </van-cell-group>

        <div class="address-edit-buttons">
            <van-button round block type="primary" native-type="submit">
                {{ t('address.save') }}
            </van-button>
            <van-button
                v-if="isEditMode"
                round
                block
                type="danger"
                style="margin-top: 12px"
                @click="handleDelete"
            >
                {{ t('address.delete') }}
            </van-button>
        </div>
    </van-form>
  </div>
</template>

<style scoped>

.address-edit-page {
    background-color: #f7f8fa;
    min-height: 100vh;
}

.address-edit-buttons {
    padding: 16px;
}

</style>
