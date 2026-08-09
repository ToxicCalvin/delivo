<script setup lang="ts">

import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { showToast, showFailToast, showSuccessToast } from 'vant';
import { authService } from '@/services/auth.service';
import { useI18n } from 'vue-i18n';
import { Locale } from 'vant';
import enUS from 'vant/es/locale/lang/en-US';
import zhCN from 'vant/es/locale/lang/zh-CN';

const router = useRouter();
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


const form = ref({
    username: '',
    password: '',
    name: '',
    phone: ''
});

// Loading state
const loading = ref(false);

/**
 * Handle register form submission
 */
const handleRegister = async () => {
    if (!form.value.username.trim() || !form.value.password.trim()) {
        showToast(t('auth.inputCredentials'));
        return;
    }
    
    loading.value = true;
    
    try {
        await authService.register(form.value.username, form.value.password, form.value.name, form.value.phone);
        showSuccessToast(t('auth.registerSuccess'));
        router.replace('/login');
    } catch (error: any) {
        console.error('Register error:', error);
        const errorMessage = error?.message || t('auth.registerFail');
        showFailToast(errorMessage);
    } finally {
        loading.value = false;
    }
};

const goToLogin = () => {
    router.replace('/login');
};

</script>

<template>
  <div class="register-page">
    <div class="lang-toggle" @click="showLangPicker = true">
        <van-icon name="exchange" />
        <span>{{ locale === 'en-US' ? 'English' : '中文' }}</span>
    </div>

    <div class="header">
        <h1>Delivo</h1>
        <p>{{ t('auth.registerSubtitle') }}</p>
    </div>
    
    <van-form @submit="handleRegister">
        <van-cell-group inset>
            <van-field
              v-model="form.username"
              name="username"
              :label="t('auth.account')"
              :placeholder="t('auth.accountPlaceholder')"
              :rules="[{ required: true, message: t('auth.accountRequired') }]"
              clearable
            />
            <van-field
              v-model="form.password"
              type="password"
              name="password"
              :label="t('auth.password')"
              :placeholder="t('auth.passwordPlaceholder')"
              :rules="[{ required: true, message: t('auth.passwordRequired') }]"
              clearable
            />
            <van-field
              v-model="form.name"
              name="name"
              :label="t('auth.name')"
              :placeholder="t('auth.namePlaceholder')"
              clearable
            />
            <van-field
              v-model="form.phone"
              name="phone"
              :label="t('auth.phone')"
              :placeholder="t('auth.phonePlaceholder')"
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
              {{ t('auth.register') }}
            </van-button>
        </div>
        <div class="footer-links">
            <span class="link" @click="goToLogin">{{ t('auth.toLogin') }}</span>
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

.register-page {
    height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background-color: var(--page-bg-color, #f7f8fa);
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
