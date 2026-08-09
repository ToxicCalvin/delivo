import { createI18n } from 'vue-i18n';
import { Locale } from 'vant';


import zhCN from './locales/zh-CN';
import enUS from './locales/en-US';


import vantZhCN from 'vant/es/locale/lang/zh-CN';
import vantEnUS from 'vant/es/locale/lang/en-US';

const messages = {
    'zh-CN': {
        ...zhCN,
        ...vantZhCN
    },
    'en-US': {
        ...enUS,
        ...vantEnUS
    }
};


const getSavedLocale = () => {
    const saved = localStorage.getItem('i18n-locale');
    if (saved && ['zh-CN', 'en-US'].includes(saved)) {
        return saved;
    }
    return 'zh-CN';
};

const i18n = createI18n({
    legacy: false, 
    locale: getSavedLocale(),
    fallbackLocale: 'zh-CN',
    messages,
});


export const syncVantLocale = (lang: string) => {
    if (lang === 'en-US') {
        Locale.use('en-US', vantEnUS);
    } else {
        Locale.use('zh-CN', vantZhCN);
    }
};


syncVantLocale(i18n.global.locale.value as string);

export default i18n;
