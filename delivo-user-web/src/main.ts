import { createApp } from 'vue';
import { createPinia } from 'pinia';
import Vant, { setToastDefaultOptions } from 'vant';

import 'vant/lib/index.css';

import './styles/index.css';

import App from './App.vue';
import router from './router';
import i18n from './i18n';
import { setupGlobalErrorHandler } from './utils/error-handler';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(i18n);
app.use(Vant);


setToastDefaultOptions({
  duration: 3000,
  closeOnClick: true
});


setupGlobalErrorHandler(app);

app.mount('#app');
