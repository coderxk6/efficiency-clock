import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { createPinia } from 'pinia';
import router from './router';

// Vant 样式
import { Button, Field, Switch, Tab, Tabs, Circle } from 'vant';
import 'vant/lib/index.css';

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);
app.use(router);

app.use(Button);
app.use(Field);
app.use(Switch);
app.use(Tab);
app.use(Tabs);
app.use(Circle);

app.mount('#app');
