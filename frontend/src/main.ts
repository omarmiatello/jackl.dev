import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import ElementUI from 'element-ui';
import locale from 'element-ui/lib/locale/lang/it'
import 'element-ui/lib/theme-chalk/index.css';

Vue.config.productionTip = false;
Vue.use(ElementUI, {locale})

new Vue({
    router,
    render: h => h(App)
}).$mount("#app");