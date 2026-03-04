<script setup>
</script>

<template>
  <router-view></router-view>
</template>

<style>
/* Global Scrollbar Style - High Visibility */
::-webkit-scrollbar {
  width: 10px;
  height: 10px;
  display: block;
}

::-webkit-scrollbar-track {
  background: rgba(15, 14, 27, 0.5);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, #7c3aed, #3fecff);
  border-radius: 10px;
  border: 2px solid rgba(15, 14, 27, 0.8);
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, #9333ea, #06b6d4);
}
.tab-content::-webkit-scrollbar-thumb {
  background-color: rgba(139, 92, 246, 0.5);
  border-radius: 4px;
}

/* Firefox */
* {
  scrollbar-width: thin;
  scrollbar-color: rgba(139, 92, 246, 0.5) rgba(30, 27, 43, 0.3);
}

/* Vant Dialog 全级深度定制 - 无分割线版 */
.van-dialog {
  background: #111019 !important; /* 极简深黑色 */
  border: 1px solid rgba(167, 139, 250, 0.15) !important; /* 带有微弱紫光的呼吸边框 */
  border-radius: 28px !important;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.8) !important;
  overflow: visible !important;
}

/* 强制隐藏所有 Vant 默认线条 */
[class*='van-hairline']::after {
  display: none !important;
  border: none !important;
}

.van-dialog__header {
  color: #ffffff !important;
  font-weight: 800 !important;
  padding-top: 35px !important;
  font-size: 1.45rem !important;
  letter-spacing: 1.5px;
}

.van-dialog__content {
  background: transparent !important;
}

.van-dialog__message {
  color: #94a3b8 !important; /* 高级银灰色，拒绝紫色 */
  font-size: 1.05rem !important;
  line-height: 1.7 !important;
  padding: 20px 32px 30px !important;
  font-weight: 400;
}

.van-dialog__footer {
  padding: 0 32px 32px !important;
  display: flex !important;
  gap: 16px !important;
}

/* 按钮基础通用样式 */
.van-dialog__cancel,
.van-dialog__confirm {
  flex: 1 !important;
  height: 52px !important;
  border-radius: 16px !important;
  font-size: 1.05rem !important;
  font-weight: 700 !important;
  border: none !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

/* 取消按钮（深色玻璃态） */
.van-dialog__cancel {
  background: rgba(255, 255, 255, 0.05) !important;
  color: #94a3b8 !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.van-dialog__cancel:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  color: #ffffff !important;
}

.van-dialog__cancel:active {
  transform: scale(0.96) !important;
}

/* 确认按钮（核心渐变紫） */
.van-dialog__confirm {
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 100%) !important;
  color: #ffffff !important;
  box-shadow: 0 8px 25px rgba(124, 58, 237, 0.4) !important;
}

.van-dialog__confirm:hover {
  background: linear-gradient(135deg, #8b5cf6 0%, #818cf8 100%) !important;
  transform: translateY(-2px) !important;
  box-shadow: 0 12px 30px rgba(124, 58, 237, 0.6) !important;
}

.van-dialog__confirm:active {
  transform: translateY(0) scale(0.96) !important;
  box-shadow: 0 4px 15px rgba(124, 58, 237, 0.4) !important;
}

@keyframes dialogShow {
  0% { opacity: 0; transform: translate3d(-50%, -40%, 0) scale(0.9); }
  100% { opacity: 1; transform: translate3d(-50%, -50%, 0) scale(1); }
}

.van-dialog-bounce-enter-active {
  animation: dialogShow 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) forwards !important;
}
</style>
