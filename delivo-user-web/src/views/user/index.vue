<script setup lang="ts">

import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { syncVantLocale } from '@/i18n';

const authStore = useAuthStore();
const router = useRouter();
const { t, locale } = useI18n();

const showLangAction = ref(false);
const langActions = [
    { name: '简体中文', val: 'zh-CN' },
    { name: 'English', val: 'en-US' }
];

const user = computed(() => authStore.currentUser);

const onSelectLang = (action: any) => {
    locale.value = action.val;
    localStorage.setItem('i18n-locale', action.val);
    syncVantLocale(action.val);
    showLangAction.value = false;
};

const logout = async () => {
    authStore.logout();
    router.push('/login');
};

</script>

<template>
  <div class="user-page">
    <div class="user-header">
        <div class="avatar">
            <van-icon name="user-o" size="40" />
        </div>
        <div class="info">
            <h3>{{ user?.username || t('user.notLoggedIn') }}</h3>
            <p>{{ user?.phone || t('user.noPhone') }}</p>
        </div>
    </div>

    <van-cell-group class="section" inset>
        <van-cell :title="t('user.orders')" is-link to="/order" icon="orders-o" />
        <van-cell :title="t('user.address')" is-link to="/address" icon="location-o" />
        <van-cell :title="t('user.language')" is-link @click="showLangAction = true" icon="setting-o" />
    </van-cell-group>

    <div class="logout-btn">
        <van-button type="default" block @click="logout" class="logout">
            {{ t('user.logout') }}
        </van-button>
    </div>

    <van-action-sheet 
        v-model:show="showLangAction" 
        :actions="langActions" 
        :cancel-text="t('common.cancel')" 
        close-on-click-action 
        @select="onSelectLang" 
    />
    
    
    <van-tabbar route>
      <van-tabbar-item replace to="/" icon="home-o">{{ t('tabbar.menu') }}</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="cart-o">{{ t('tabbar.cart') }}</van-tabbar-item>
      <van-tabbar-item replace to="/order" icon="orders-o">{{ t('tabbar.order') }}</van-tabbar-item>
      <van-tabbar-item replace to="/user" icon="user-o">{{ t('tabbar.user') }}</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style scoped>

.user-page {
    background-color: var(--page-bg-color);
    min-height: 100vh;
    padding-bottom: 50px; }

.user-header {
    background-color: var(--van-primary-color);
    padding: 30px 20px;
    display: flex;
    align-items: center;
    color: var(--van-primary-text-color);
    margin-bottom: 20px;
}

.avatar {
    background: #fff;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-right: 15px;
}

.info h3 {
    margin: 0 0 5px 0;
    font-size: 18px;
}

.info p {
    margin: 0;
    font-size: 14px;
    opacity: 0.8;
}

.section {
    margin-bottom: 20px;
}

.logout-btn {
    padding: 0 16px;
    margin-top: 20px;
}
.logout {
    color: #ff4d4f;
}

</style>
