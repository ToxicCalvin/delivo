<script setup lang="ts">

import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter, useRoute } from 'vue-router';
import { showToast, showFailToast, showSuccessToast } from 'vant';
import { useI18n } from 'vue-i18n';
import { Locale } from 'vant';
import enUS from 'vant/es/locale/lang/en-US';
import zhCN from 'vant/es/locale/lang/zh-CN';

const store = useAuthStore();
const router = useRouter();
const route = useRoute();
const { t, locale } = useI18n();

const showLangPicker = ref(false);
const langActions = [
    { name: '简体中文', value: 'zh-CN' },
    { name: 'English', value: 'en-US' }
];

const onLanguageSelect = (action: any) => {
    locale.value = action.value;
    localStorage.setItem('delivo_lang', action.value);
    
    if (action.value === 'en-US') {
        Locale.use('en-US', enUS);
    } else {
        Locale.use('zh-CN', zhCN);
    }
    
    showLangPicker.value = false;
};


const username = ref('');
const password = ref('');

// Loading state
const loading = ref(false);

/**
 * Handle login form submission
 * Validates inputs, calls auth store login action, and navigates on success
 */
const handleLogin = async () => {
    // Validate non-empty inputs
    if (!username.value.trim()) {
        showToast(t('auth.usernameRequired'));
        return;
    }
    
    if (!password.value.trim()) {
        showToast(t('auth.passwordRequired'));
        return;
    }

    loading.value = true;
    
    try {
        await store.login(username.value, password.value);
        console.log('Login success, navigating...', route.query); 
        showSuccessToast(t('auth.loginSuccess'));
        
        const target = route.query.redirect as string || '/';
        console.log('Target:', target);
        router.replace(target);
        console.log('Navigation called'); 
    } catch (error: any) {
        console.error('Login error:', error); 
        
        let errorMessage = error?.message || t('auth.loginFail');
        if (errorMessage === '账号不存在') {
            errorMessage = t('auth.accountNotExist');
        }
        showFailToast(errorMessage);
    } finally {
        loading.value = false;
    }
};

const goToRegister = () => {
    router.push('/register');
};

</script>

<template>
  <div class="login-page">
    <div class="lang-toggle" @click="showLangPicker = true">
        <van-icon name="exchange" />
        <span>{{ locale === 'en-US' ? 'English' : '中文' }}</span>
    </div>
    
    <div class="header">
        <h1>Delivo</h1>
        <p>{{ t('auth.loginSubtitle') }}</p>
    </div>
    
    <van-form @submit="handleLogin">
        <van-cell-group inset>
            <van-field
              v-model="username"
              name="username"
              :label="t('auth.username')"
              :placeholder="t('auth.usernamePlaceholder')"
              :rules="[{ required: true, message: t('auth.usernameRequired') }]"
              clearable
            />
            <van-field
              v-model="password"
              type="password"
              name="password"
              :label="t('auth.password')"
              :placeholder="t('auth.passwordPlaceholder')"
              :rules="[{ required: true, message: t('auth.passwordRequired') }]"
              clearable
            />
        </van-cell-group>
        <div style="margin: 16px;">
            <van-button 
              round 
              block 
              type="primary" 
              native-type="submit" 
              :loading="loading"
              :disabled="loading"
            >
              {{ t('auth.login') }}
            </van-button>
        </div>
        <div class="footer-links">
            <span class="link" @click="goToRegister">{{ t('auth.toRegister') }}</span>
        </div>
    </van-form>

    <van-action-sheet
        v-model:show="showLangPicker"
        :actions="langActions"
        @select="onLanguageSelect"
        :cancel-text="t('common.cancel')"
    />
  </div>
</template>

<style scoped>

.login-page {
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background-color: var(--page-bg-color);
    position: relative;
}
.lang-toggle {
    position: absolute;
    top: 24px;
    right: 24px;
    font-size: 14px;
    color: var(--van-primary-color, #1989fa);
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
}
.header {
    text-align: center;
    margin-bottom: 2rem;
}
.footer-links {
    text-align: center;
    margin-top: 1rem;
    font-size: 14px;
}
.link {
    color: var(--van-primary-color, #1989fa);
    cursor: pointer;
}

</style>
