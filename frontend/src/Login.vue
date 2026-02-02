<script setup>
import { ref } from 'vue';
import { showToast } from 'vant';

const emit = defineEmits(['login-success']);

const activeMode = ref('login'); // 'login', 'register', 'guest'
const loginForm = ref({
  username: '',
  password: ''
});
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
});

const isLoading = ref(false);

// 登录
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    showToast('请输入用户名和密码');
    return;
  }

  isLoading.value = true;
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loginForm.value)
    });

    const data = await res.json();
    
    if (!res.ok) {
      throw new Error(data.message || '登录失败');
    }

    // 存储 token 和用户信息
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('nickname', data.nickname);
    localStorage.setItem('isGuest', data.isGuest);

    showToast('登录成功！');
    emit('login-success', data);
  } catch (err) {
    showToast(err.message);
  } finally {
    isLoading.value = false;
  }
};

// 注册
const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) {
    showToast('请输入用户名和密码');
    return;
  }

  if (registerForm.value.username.length < 3 || registerForm.value.username.length > 50) {
    showToast('用户名长度必须在3-50之间');
    return;
  }

  if (registerForm.value.password.length < 6 || registerForm.value.password.length > 50) {
    showToast('密码长度必须在6-50之间');
    return;
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    showToast('两次输入的密码不一致');
    return;
  }

  isLoading.value = true;
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: registerForm.value.username,
        password: registerForm.value.password,
        nickname: registerForm.value.nickname
      })
    });

    const data = await res.json();
    
    if (!res.ok) {
      throw new Error(data.message || '注册失败');
    }

    // 存储 token 和用户信息
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('nickname', data.nickname);
    localStorage.setItem('isGuest', data.isGuest);

    showToast('注册成功！');
    emit('login-success', data);
  } catch (err) {
    showToast(err.message);
  } finally {
    isLoading.value = false;
  }
};

// 游客登录
const handleGuestLogin = async () => {
  isLoading.value = true;
  try {
    const res = await fetch('/api/auth/guest', {
      method: 'POST'
    });

    const data = await res.json();
    
    if (!res.ok) {
      throw new Error(data.message || '游客登录失败');
    }

    // 存储 token 和用户信息
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('nickname', data.nickname);
    localStorage.setItem('isGuest', data.isGuest);

    showToast('以游客身份进入');
    emit('login-success', data);
  } catch (err) {
    showToast(err.message);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo and Title -->
      <div class="login-header">
        <div class="logo-icon">⚡</div>
        <h1 class="login-title">Efficiency Clock</h1>
        <p class="login-subtitle">修仙专注，逆天改命</p>
      </div>

      <!-- Tab Switcher -->
      <div class="mode-tabs">
        <button 
          :class="['mode-tab', { active: activeMode === 'login' }]"
          @click="activeMode = 'login'"
        >
          登录
        </button>
        <button 
          :class="['mode-tab', { active: activeMode === 'register' }]"
          @click="activeMode = 'register'"
        >
          注册
        </button>
      </div>

      <!-- Login Form -->
      <div v-if="activeMode === 'login'" class="form-container">
        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <input 
            v-model="loginForm.username" 
            type="text" 
            placeholder="用户名"
            class="glass-field"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码"
            class="glass-field"
            @keyup.enter="handleLogin"
          />
        </div>

        <button 
          class="primary-btn"
          :class="{ loading: isLoading }"
          @click="handleLogin"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">登录</span>
          <span v-else>登录中...</span>
        </button>
      </div>

      <!-- Register Form -->
      <div v-if="activeMode === 'register'" class="form-container">
        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <input 
            v-model="registerForm.username" 
            type="text" 
            placeholder="用户名（3-50字符）"
            class="glass-field"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7l10 5 10-5-10-5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <input 
            v-model="registerForm.nickname" 
            type="text" 
            placeholder="昵称（可选）"
            class="glass-field"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="密码（6-50字符）"
            class="glass-field"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="确认密码"
            class="glass-field"
          />
        </div>

        <button 
          class="primary-btn"
          :class="{ loading: isLoading }"
          @click="handleRegister"
          :disabled="isLoading"
        >
          <span v-if="!isLoading">注册</span>
          <span v-else>注册中...</span>
        </button>
      </div>

      <!-- Guest Login -->
      <div class="divider">
        <span>或</span>
      </div>

      <button 
        class="guest-btn"
        @click="handleGuestLogin"
        :disabled="isLoading"
      >
        <svg class="guest-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
          <path d="M14 2l1 1-1 1" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        以游客身份继续
      </button>
    </div>

    <!-- Background Effect -->
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap');

* {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0e1b 0%, #1a1625 50%, #251d35 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* Background Orbs */
.bg-orbs {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: float 15s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(114, 50, 221, 0.6) 0%, transparent 70%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(63, 236, 255, 0.4) 0%, transparent 70%);
  bottom: -250px;
  right: -250px;
  animation-delay: 5s;
}

.orb-3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.5) 0%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -30px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
}

/* Login Card */
.login-card {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 450px;
  background: rgba(30, 27, 43, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  padding: 48px 40px;
  border: 2px solid rgba(167, 139, 250, 0.2);
  box-shadow: 0 20px 80px rgba(124, 58, 237, 0.4),
              inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

/* Header */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.login-title {
  margin: 0;
  font-size: 2rem;
  font-family: 'Poppins', sans-serif;
  background: linear-gradient(135deg, #a78bfa 0%, #7c3aed 50%, #3fecff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
  letter-spacing: -1px;
  filter: drop-shadow(0 0 20px rgba(124, 58, 237, 0.4));
}

.login-subtitle {
  color: #c4b5fd;
  margin: 8px 0 0;
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* Mode Tabs */
.mode-tabs {
  display: flex;
  gap: 8px;
  background: rgba(15, 14, 27, 0.6);
  padding: 6px;
  border-radius: 16px;
  margin-bottom: 24px;
}

.mode-tab {
  flex: 1;
  padding: 12px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 600;
  font-size: 0.95rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.mode-tab:hover {
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.05);
}

.mode-tab.active {
  color: #ffffff;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.6) 0%, rgba(99, 102, 241, 0.5) 100%);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.4);
}

/* Form */
.form-container {
  margin-bottom: 24px;
}

.input-group {
  position: relative;
  margin-bottom: 16px;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #a78bfa;
  z-index: 1;
}

.glass-field {
  width: 100%;
  padding: 16px 16px 16px 48px;
  background: rgba(15, 14, 27, 0.6);
  border: 2px solid rgba(167, 139, 250, 0.2);
  border-radius: 16px;
  color: white;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
}

.glass-field::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.glass-field:hover {
  background: rgba(15, 14, 27, 0.8);
  border-color: rgba(167, 139, 250, 0.3);
}

.glass-field:focus {
  background: rgba(15, 14, 27, 0.9);
  border-color: rgba(167, 139, 250, 0.6);
  box-shadow: 0 0 0 4px rgba(124, 58, 237, 0.1),
              0 4px 20px rgba(124, 58, 237, 0.3);
}

/* Autofill fix */
.glass-field:-webkit-autofill,
.glass-field:-webkit-autofill:hover,
.glass-field:-webkit-autofill:focus {
  -webkit-text-fill-color: white !important;
  transition: background-color 9999s ease-in-out 0s;
  background-color: rgba(15, 14, 27, 0.9) !important;
  box-shadow: 0 0 0 1000px rgba(15, 14, 27, 0.9) inset !important;
}

/* Primary Button */
.primary-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 100%);
  border: none;
  border-radius: 16px;
  color: white;
  font-weight: 700;
  font-size: 1.05rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.4);
  position: relative;
  overflow: hidden;
}

.primary-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.primary-btn:hover::before {
  left: 100%;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(124, 58, 237, 0.6);
}

.primary-btn:active {
  transform: translateY(0);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.primary-btn.loading {
  background: linear-gradient(135deg, #6366f1 0%, #7c3aed 100%);
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  margin: 24px 0;
  font-size: 0.9rem;
  font-weight: 500;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.divider span {
  padding: 0 16px;
}

/* Guest Button */
.guest-btn {
  width: 100%;
  padding: 16px;
  background: rgba(63, 236, 255, 0.1);
  border: 2px solid rgba(63, 236, 255, 0.3);
  border-radius: 16px;
  color: #3fecff;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.guest-icon {
  width: 20px;
  height: 20px;
}

.guest-btn:hover {
  background: rgba(63, 236, 255, 0.2);
  border-color: rgba(63, 236, 255, 0.5);
  box-shadow: 0 0 24px rgba(63, 236, 255, 0.3);
  transform: translateY(-2px);
}

.guest-btn:active {
  transform: translateY(0);
}

.guest-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 480px) {
  .login-card {
    padding: 36px 24px;
  }

  .login-title {
    font-size: 1.75rem;
  }
}
</style>
