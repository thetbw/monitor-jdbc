import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css';
import 'highlight.js/styles/vs.css';
import VueClipboard from 'vue-clipboard2'

import hljs from 'highlight.js/lib/core';
import sql from 'highlight.js/lib/languages/sql';
import vuePlugin from "@highlightjs/vue-plugin";

hljs.registerLanguage('sql', sql);

Vue.use(vuePlugin);
Vue.use(ElementUI);
Vue.use(VueClipboard);

Vue.config.productionTip = false
new Vue({
  render: h => h(App),
}).$mount('#app')
