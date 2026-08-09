import type { App, ComponentPublicInstance } from 'vue';
import { showFailToast } from 'vant';

export function setupGlobalErrorHandler(app: App) {
  app.config.errorHandler = (
    err: unknown,
    _instance: ComponentPublicInstance | null,
    info: string
  ) => {


    console.error('全局错误:', err);

    console.error('错误信息:', info);


    showFailToast({
      message: '发生了一个错误，请稍后重试',
      duration: 3000,
      closeOnClick: true
    });
  };
}

