import { createApp } from 'vue'
import App from './App.vue'
import './style.css'

// Vant UI
import 'vant/lib/index.css'
import {
  Button,
  Circle,
  CountDown,
  Dialog,
  Field,
  Popup,
  Toast,
  Notify,
  ConfigProvider,
  Switch,
  Tab,
  Tabs
} from 'vant'

const app = createApp(App)

app.use(Button)
app.use(Circle)
app.use(CountDown)
app.use(Dialog)
app.use(Field)
app.use(Popup)
app.use(Toast)
app.use(Notify)
app.use(ConfigProvider)
app.use(Switch)
app.use(Tab)
app.use(Tabs)

app.mount('#app')
