<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { showToast, showDialog } from 'vant';
import confetti from 'canvas-confetti';

// Tabs
const activeTab = ref(0);

// Task Creation Form
const taskName = ref('');
const durationMinutes = ref(25);

// Manual Mode State
const isManualMode = ref(false);
const manualStartTime = ref('');
const manualEndTime = ref('');

// Initialize manual time defaults
const initManualTimes = () => {
  const now = new Date();
  const later = new Date(now.getTime() + 60 * 60000); // Default 1 hour
  
  const format = (d) => {
    const pad = (n) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };
  
  manualStartTime.value = format(now);
  manualEndTime.value = format(later);
};

// Auto init when switching to manual mode
import { watch } from 'vue';
watch(isManualMode, (newVal) => {
  if (newVal && !manualStartTime.value) {
    initManualTimes();
  }
});

// Computed duration in manual mode
const validDurationSeconds = computed(() => {
  if (!manualStartTime.value || !manualEndTime.value) return 0;
  const start = new Date(manualStartTime.value);
  const end = new Date(manualEndTime.value);
  const diff = (end.getTime() - start.getTime()) / 1000;
  return diff > 0 ? diff : 0;
});

const isValidTimeRange = computed(() => validDurationSeconds.value > 0);

const computedDurationText = computed(() => {
  const sec = validDurationSeconds.value;
  if (!sec) return '';
  const h = Math.floor(sec / 3600);
  const m = Math.floor((sec % 3600) / 60);
  if (h > 0) return `${h} 小时 ${m} 分钟`;
  return `${m} 分钟`;
});

// Running Tasks
const runningTasks = ref([]);

// Completed Tasks
const completedTasks = ref([]);

// User State
const user = reactive({
  rank: '凡人',
  exp: 0
});

// Load tasks from backend on mount
onMounted(async () => {
  await loadRunningTasks();
  await loadCompletedTasks();
  initManualTimes();
});

// Load running tasks from backend
const loadRunningTasks = async () => {
  try {
    const res = await fetch('/api/focus/tasks');
    if (res.ok) {
      const tasks = await res.json();
      runningTasks.value = tasks.map(task => ({
        ...task,
        currentTime: task.remainingSeconds * 1000,
        totalTime: task.durationSeconds * 1000,
        rate: (task.remainingSeconds / task.durationSeconds) * 100
      }));
      
      // Start countdown for each task
      runningTasks.value.forEach(task => {
        startTaskCountdown(task);
      });
    }
  } catch (err) {
    console.error('Failed to load running tasks:', err);
  }
};

// Load completed tasks from backend
const loadCompletedTasks = async () => {
  try {
    const res = await fetch('/api/focus/history?limit=50');
    if (res.ok) {
      completedTasks.value = await res.json();
    }
  } catch (err) {
    console.error('Failed to load completed tasks:', err);
  }
};

// Start a new task
const startFocus = async () => {
  if (!taskName.value.trim()) {
    showToast('请先确认你的修炼法门（任务名）！');
    return;
  }

  let finalDurationSeconds = durationMinutes.value * 60;
  let finalStartTime = null;

  if (isManualMode.value) {
    if (!isValidTimeRange.value) {
      showToast('出关时间必须晚于入定时间！');
      return;
    }
    finalDurationSeconds = validDurationSeconds.value;
    // Append seconds to satisfy ISO/Local time if needed, backend uses standard parse
    finalStartTime = manualStartTime.value + ':00'; 
  }

  try {
    const res = await fetch('/api/focus/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        taskName: taskName.value,
        durationSeconds: finalDurationSeconds,
        startTime: finalStartTime
      })
    });

    if (!res.ok) throw new Error('启动失败');

    const data = await res.json();
    
    // Add to running tasks
    const newTask = {
      id: data.taskId,
      taskName: taskName.value,
      durationSeconds: finalDurationSeconds,
      currentTime: finalDurationSeconds * 1000,
      totalTime: finalDurationSeconds * 1000,
      rate: 100,
      status: 'RUNNING'
    };
    
    runningTasks.value.push(newTask);
    // Important: Get the reactive proxy from the array, not the raw object
    const taskProxy = runningTasks.value.find(t => t.id === newTask.id);
    if (taskProxy) {
      startTaskCountdown(taskProxy);
    }
    
    showToast('修炼任务已开始！');
    taskName.value = '';
    activeTab.value = 1; // Switch to running tasks tab
  } catch (err) {
    showToast('心魔干扰: ' + err.message);
  }
};

// Start countdown for a task
const startTaskCountdown = (task) => {
  if (task.interval) {
    clearInterval(task.interval);
  }
  
  task.interval = setInterval(() => {
    if (task.currentTime > 0) {
      task.currentTime -= 1000;
      task.rate = (task.currentTime / task.totalTime) * 100;
    } else {
      clearInterval(task.interval);
      completeTask(task);
    }
  }, 1000);
};

// Complete a task
const completeTask = async (task) => {
  if (task.interval) {
    clearInterval(task.interval);
  }

  try {
    const res = await fetch(`/api/focus/${task.id}/complete`, {
      method: 'PUT'
    });

    if (!res.ok) {
      // 检查是否是因为任务已被其他设备完成
      await loadRunningTasks();
      await loadCompletedTasks();
      
      const isStillRunning = runningTasks.value.some(t => t.id === task.id);
      if (!isStillRunning) {
        // 任务已经在其他地方完成了，直接返回即可，不需要报错
        return;
      }
      throw new Error('完成任务失败');
    }

    const data = await res.json();
    // ... 保持原有逻辑 ...
    user.rank = data.newRank;
    user.exp = data.totalExperience;
    runningTasks.value = runningTasks.value.filter(t => t.id !== task.id);
    await loadCompletedTasks();
    fireConfetti();
    showDialog({
      title: data.levelUp ? '⚡ 境界突破 ⚡' : '✓ 本次修炼圆满',
      message: `${data.message}\n当前境界: ${data.newRank}\n累计元气: ${data.totalExperience}`,
      theme: 'round-button',
      confirmButtonColor: '#7232dd'
    });
  } catch (err) {
    showToast('心魔干扰: ' + err.message);
  }
};

// Give up a task
const giveUpTask = (task) => {
  showDialog({
    title: '心魔入侵',
    message: '半途而废容易走火入魔，确定要出关吗？',
    showCancelButton: true,
  }).then(async () => {
    if (task.interval) {
      clearInterval(task.interval);
    }
    
    try {
      await fetch(`/api/focus/${task.id}`, {
        method: 'DELETE'
      });
    } catch (err) {
      console.error('Failed to abandon task on backend:', err);
    }

    runningTasks.value = runningTasks.value.filter(t => t.id !== task.id);
    showToast('已放弃修炼');
  }).catch(() => {
    // Cancel, continue
  });
};

// Format time
const formatTime = (ms) => {
  const totalSeconds = Math.floor(ms / 1000);
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
};

// Format duration
const formatDuration = (seconds) => {
  if (seconds >= 3600) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h}小时${m}分钟`;
  }
  const minutes = Math.floor(seconds / 60);
  return `${minutes} 分钟`;
};

// Format date time
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return date.toLocaleString('zh-CN');
};

// Confetti effect
const fireConfetti = () => {
  const duration = 4000;
  const animationEnd = Date.now() + duration;
  const defaults = { startVelocity: 45, spread: 360, ticks: 100, zIndex: 9999 };

  const randomInRange = (min, max) => Math.random() * (max - min) + min;

  const interval = setInterval(function() {
    const timeLeft = animationEnd - Date.now();

    if (timeLeft <= 0) {
      return clearInterval(interval);
    }

    const particleCount = 60 * (timeLeft / duration);
    // 两个源点
    confetti({ ...defaults, particleCount, origin: { x: randomInRange(0.1, 0.4), y: Math.random() - 0.2 } });
    confetti({ ...defaults, particleCount, origin: { x: randomInRange(0.6, 0.9), y: Math.random() - 0.2 } });
    
    // 随机大爆炸
    if (Math.random() < 0.05) {
       confetti({ 
         ...defaults, 
         particleCount: 100, 
         spread: 120, 
         origin: { y: 0.6 },
         scalar: 1.2,
         colors: ['#7232dd', '#3fecff', '#ffe4e6']
       });
    }
  }, 200);
};
</script>

<template>
  <div class="app-container">
    <!-- Header -->
    <div class="header">
      <h1 class="app-title">⚡ Efficiency Clock</h1>
      <p class="subtitle">修仙专注，逆天改命</p>
      
      <div class="status-card">
        <div class="rank-badge">{{ user.rank }}</div>
        <div class="exp-text">灵力: {{ user.exp }}</div>
      </div>
    </div>

    <!-- Tab Navigation -->
    <van-tabs v-model:active="activeTab" color="#7232dd" title-active-color="#7232dd" class="custom-tabs">
      
      <!-- Tab 1: Create Task -->
      <van-tab title="创建任务">
        <div class="tab-content">
          <div class="create-form">
            <van-field 
              v-model="taskName" 
              label="修炼任务" 
              placeholder="斩杀什么心魔？" 
              class="glass-input"
            />
            
            <div class="duration-section">
              <div class="section-header">
                <p class="section-label">闭关时长</p>
                <div class="mode-switch">
                  <span :class="{ active: !isManualMode }">时长</span>
                  <van-switch v-model="isManualMode" size="20px" active-color="#7232dd" inactive-color="#7232dd" />
                  <span :class="{ active: isManualMode }">时间段</span>
                </div>
              </div>

              <!-- Duration Mode -->
              <div v-if="!isManualMode">
                <div class="duration-buttons">
                  <van-button size="small" type="primary" plain @click="durationMinutes = 1">1</van-button>
                  <van-button size="small" type="primary" plain @click="durationMinutes = 10">10</van-button>
                  <van-button size="small" type="primary" plain @click="durationMinutes = 25">25</van-button>
                  <van-button size="small" type="primary" plain @click="durationMinutes = 45">45</van-button>
                  <van-button size="small" type="primary" plain @click="durationMinutes = 60">60</van-button>
                </div>
                <van-field 
                  v-model.number="durationMinutes" 
                  type="number" 
                  label="分钟" 
                  class="glass-input mt-2" 
                />
              </div>

              <!-- Manual Time Mode -->
              <div v-else class="manual-time-picker">
                <div class="time-input-group">
                  <label>开始入定</label>
                  <input type="datetime-local" v-model="manualStartTime" class="glass-input native-time-input" />
                </div>
                <div class="time-input-group">
                  <label>预期出关</label>
                  <input type="datetime-local" v-model="manualEndTime" class="glass-input native-time-input" />
                </div>
                <div class="calculated-duration" v-if="computedDurationText">
                  预计修炼: <span>{{ computedDurationText }}</span>
                </div>
              </div>
            </div>

            <van-button 
              round 
              type="primary" 
              size="large" 
              class="start-btn" 
              @click="startFocus"
              block
              :disabled="isManualMode && !isValidTimeRange"
            >
              🚀 开始修炼
            </van-button>
          </div>
        </div>
      </van-tab>

      <!-- Tab 2: Running Tasks -->
      <van-tab title="进行中" :badge="runningTasks.length || ''">
        <div class="tab-content">
          <div v-if="runningTasks.length === 0" class="empty-state">
            <p class="empty-icon">😴</p>
            <p class="empty-text">暂无进行中的任务</p>
            <p class="empty-hint">前往「创建任务」开始修炼</p>
          </div>

          <div v-else class="task-grid">
            <div 
              v-for="task in runningTasks" 
              :key="task.id" 
              class="task-card running-card"
            >
              <div class="animated-border"></div>
              <div class="task-card-inner">
                <div class="task-header">
                  <h3 class="task-title">{{ task.taskName }}</h3>
                  <span class="task-status running">运行中</span>
                </div>

                <div class="timer-section">
                  <van-circle
                    v-model:current-rate="task.rate"
                    :rate="100"
                    :color="{ '0%': '#3fecff', '100%': '#7c3aed' }"
                    :stroke-width="60"
                    size="200px"
                    layer-color="rgba(255, 255, 255, 0.05)"
                  />
                  
                  <div class="circle-content">
                    <div class="timer-display">{{ formatTime(task.currentTime) }}</div>
                  </div>
                </div>

                <div class="task-actions">
                  <button 
                    class="action-btn success-btn"
                    @click="completeTask(task)"
                  >
                    <span class="btn-icon">✓</span>
                    <span class="btn-text">完成</span>
                    <div class="btn-ripple"></div>
                  </button>
                  <button 
                    class="action-btn danger-btn"
                    @click="giveUpTask(task)"
                  >
                    <span class="btn-icon">✗</span>
                    <span class="btn-text">放弃</span>
                    <div class="btn-ripple"></div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-tab>

      <!-- Tab 3: History -->
      <van-tab title="历史记录">
        <div class="tab-content">
          <div v-if="completedTasks.length === 0" class="empty-state">
            <p class="empty-icon">📜</p>
            <p class="empty-text">暂无历史记录</p>
          </div>

          <div v-else class="history-list">
            <div 
              v-for="task in completedTasks" 
              :key="task.id" 
              class="history-item"
            >
              <div class="history-icon">✓</div>
              <div class="history-content">
                <h4 class="history-title">{{ task.taskName }}</h4>
                <p class="history-meta">
                  {{ formatDuration(task.durationSeconds) }} · {{ formatDateTime(task.completedAt) }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </van-tab>

    </van-tabs>
  </div>
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
</style>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Poppins:wght@600;700;800&display=swap');

* {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

.app-container {
  height: 100vh; /* Force full viewport height */
  max-height: 100vh;
  overflow: hidden; /* No global scroll */
  background: linear-gradient(135deg, #0f0e1b 0%, #1a1625 50%, #251d35 100%);
  position: relative;
  display: flex;
  flex-direction: column;
}

.app-container::before {
  content: '';
  position: fixed;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle at 30% 50%, rgba(114, 50, 221, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(63, 236, 255, 0.1) 0%, transparent 50%);
  animation: float 20s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(30px, -30px) rotate(120deg); }
  66% { transform: translate(-20px, 20px) rotate(240deg); }
}

.header {
  text-align: center;
  padding: 1.5rem 1rem 0.5rem; /* Reduced padding */
  position: relative;
  z-index: 1;
  flex-shrink: 0;
}

.app-title {
  margin: 0;
  font-size: 2.2rem; /* Scaled down */
  font-family: 'Poppins', sans-serif;
  background: linear-gradient(135deg, #a78bfa 0%, #7c3aed 50%, #3fecff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 800;
  letter-spacing: -1px;
  filter: drop-shadow(0 0 20px rgba(124, 58, 237, 0.4));
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% { filter: drop-shadow(0 0 20px rgba(124, 58, 237, 0.4)); }
  50% { filter: drop-shadow(0 0 35px rgba(124, 58, 237, 0.7)); }
}

.subtitle {
  color: #c4b5fd;
  margin: 0.5rem 0 1rem; /* Compact margin */
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 10px rgba(196, 181, 253, 0.3);
}

.status-card {
  display: inline-block;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.2) 0%, rgba(99, 102, 241, 0.15) 100%);
  backdrop-filter: blur(20px);
  padding: 10px 24px; /* Scaled down */
  border-radius: 20px;
  border: 2px solid rgba(167, 139, 250, 0.3);
  box-shadow: 0 8px 32px rgba(124, 58, 237, 0.4),
              inset 0 1px 0 rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 48px rgba(124, 58, 237, 0.6),
              inset 0 1px 0 rgba(255, 255, 255, 0.15);
  border-color: rgba(167, 139, 250, 0.5);
}

.rank-badge {
  font-weight: 800;
  font-size: 1.2rem; /* Scaled down */
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 10px rgba(255, 215, 0, 0.6));
  font-family: 'Poppins', sans-serif;
}

.exp-text {
  background: linear-gradient(135deg, #3fecff 0%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 0.9rem;
  margin-top: 4px;
  font-weight: 600;
}


/* Tabs Redesign - Layout Fixing */
.custom-tabs {
  --van-tab-text-color: rgba(255, 255, 255, 0.6);
  --van-tab-active-text-color: #ffffff;
  position: relative;
  z-index: 10;
  margin-top: 0.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.custom-tabs :deep(.van-tabs__wrap) {
  height: 50px !important;
  flex-shrink: 0;
  background: transparent !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.custom-tabs :deep(.van-tabs__nav),
.custom-tabs :deep(.van-tab),
.custom-tabs :deep(.van-tabs__content) {
  background: transparent !important;
  background-color: transparent !important;
}

.custom-tabs :deep(.van-tabs__nav) {
  border: none !important;
  margin: 0;
  display: flex;
  justify-content: center;
}

.custom-tabs :deep(.van-hairline--top-bottom::after),
.custom-tabs :deep(.van-tabs__wrap::after) {
  display: none !important;
}

.custom-tabs :deep(.van-tabs__content) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.custom-tabs :deep(.van-tab__pane),
.custom-tabs :deep(.van-tab__panel) {
  flex: 1;
  min-height: 0;
  height: 100%;
}

/* ... existing tab item styles ... */
.custom-tabs :deep(.van-tab) {
  font-weight: 500;
  font-size: 1rem;
  transition: color 0.3s ease;
  flex: none;
  margin: 0 1rem;
  padding: 0;
  color: rgba(255, 255, 255, 0.6);
}

.custom-tabs :deep(.van-tab--active) {
  font-weight: 700;
  font-size: 1.1rem;
  color: #ffffff !important;
}

/* The Glowing Dot Indicator */
.custom-tabs :deep(.van-tabs__line) {
  background: #3fecff;
  width: 16px !important;
  height: 4px;
  border-radius: 4px;
  bottom: 10px;
  box-shadow: 0 0 8px #3fecff;
}

/* Scrollable Content Area */
.tab-content {
  padding: 1rem;
  overflow-y: auto !important;
  height: 100%;
  width: 100%;
  display: block;
  box-sizing: border-box;
  animation: fadeIn 0.5s ease-out;
  padding-bottom: 5rem; /* Ensure space for last item */
  -webkit-overflow-scrolling: touch;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Create Form */
.create-form {
  max-width: 500px;
  margin: 0 auto;
}

.glass-input {
  background: rgba(30, 27, 43, 0.4) !important;
  border-radius: 16px;
  border: 2px solid rgba(167, 139, 250, 0.2);
  margin-bottom: 1rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  padding: 2px 6px;
}

/* Ensure inner elements are transparent */
.glass-input :deep(.van-cell) {
  background: transparent !important;
  padding: 8px 16px;
}

/* Remove bottom white line from van-cell */
.glass-input::after {
  display: none !important;
  content: none !important;
  border-bottom: none !important;
}

.glass-input :deep(.van-field__control) {
  color: white;
  font-weight: 500;
}

/* Fix Autocomplete/Autofill white background */
.glass-input :deep(input:-webkit-autofill),
.glass-input :deep(input:-webkit-autofill:hover), 
.glass-input :deep(input:-webkit-autofill:focus), 
.glass-input :deep(input:-webkit-autofill:active) {
  -webkit-text-fill-color: white !important;
  transition: background-color 9999s ease-in-out 0s;
  background-color: transparent !important;
  box-shadow: 0 0 0 1000px transparent inset !important;
}

.glass-input:hover {
  background: rgba(30, 27, 43, 0.6) !important;
  border-color: rgba(167, 139, 250, 0.4);
  box-shadow: 0 4px 20px rgba(124, 58, 237, 0.2);
}

.glass-input:focus-within {
  border-color: rgba(167, 139, 250, 0.6);
  box-shadow: 0 4px 24px rgba(124, 58, 237, 0.3);
  background: rgba(30, 27, 43, 0.7) !important;
}

.glass-input :deep(.van-field__label) {
  color: #c4b5fd;
  font-weight: 600;
  width: 5em; /* Ensure label width consistency */
}

.glass-input :deep(input::placeholder) {
  color: #9ca3af;
}

.duration-section {
  margin: 2rem 0;
}

.section-label {
  color: #c4b5fd;
  margin-bottom: 1rem;
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: 0.3px;
}

.duration-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 1.2rem;
}

.duration-buttons .van-button {
  min-width: 60px;
  height: 48px;
  font-weight: 700;
  font-size: 1.1rem;
  border-radius: 14px;
  border: 2px solid rgba(167, 139, 250, 0.3);
  background: rgba(124, 58, 237, 0.1);
  color: #a78bfa;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.duration-buttons .van-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(124, 58, 237, 0.4);
  border-color: rgba(167, 139, 250, 0.6);
  background: rgba(124, 58, 237, 0.2);
  color: #c4b5fd;
}

.duration-buttons .van-button:active {
  transform: translateY(-1px);
}

.mt-2 {
  margin-top: 1rem;
}

.start-btn {
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 50%, #3b82f6 100%) !important;
  border: none !important;
  font-weight: 800;
  font-size: 1.2rem;
  margin-top: 2.5rem;
  height: 60px;
  border-radius: 18px;
  box-shadow: 0 10px 30px rgba(124, 58, 237, 0.5),
              0 0 0 0 rgba(124, 58, 237, 0.7);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  letter-spacing: 0.5px;
}

.start-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.start-btn:hover::before {
  left: 100%;
}

.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(124, 58, 237, 0.6),
              0 0 0 8px rgba(124, 58, 237, 0.2);
}

.start-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(124, 58, 237, 0.5);
}

/* Task Grid */
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.task-card {
  position: relative;
  background: transparent;
  border-radius: 28px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  isolation: isolate;
}

/* Animated Border Effect */
.animated-border {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(
    from 0deg,
    transparent 0%,
    rgba(63, 236, 255, 0.1) 20%,
    rgba(124, 58, 237, 0.5) 40%,
    #3fecff 50%,
    rgba(124, 58, 237, 0.5) 60%,
    rgba(63, 236, 255, 0.1) 80%,
    transparent 100%
  );
  animation: borderRotate 4s linear infinite;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s;
}

.running-card .animated-border {
  opacity: 1;
}

@keyframes borderRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.task-card-inner {
  background: linear-gradient(135deg, rgba(30, 27, 43, 0.9) 0%, rgba(26, 22, 37, 0.95) 100%);
  backdrop-filter: blur(20px);
  border-radius: 26px;
  margin: 2px; /* Space for border */
  padding: 2rem;
  height: calc(100% - 4px);
  position: relative;
  z-index: 1;
}

.task-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.task-title {
  margin: 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 800;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.task-status {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.task-status.running {
  background: linear-gradient(135deg, rgba(63, 236, 255, 0.2), rgba(6, 182, 212, 0.2));
  color: #3fecff;
  border: 1px solid rgba(63, 236, 255, 0.3);
  box-shadow: 0 0 20px rgba(63, 236, 255, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 15px rgba(63, 236, 255, 0.2); opacity: 1; }
  50% { box-shadow: 0 0 25px rgba(63, 236, 255, 0.4); opacity: 0.8; }
}

.timer-section {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 2rem 0;
  position: relative;
  height: 220px; /* Ensure container has height */
}

/* Ensure van-circle is centered */
.timer-section :deep(.van-circle) {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.circle-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* Classic center trick */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  z-index: 10;
  pointer-events: none;
}

.timer-display {
  font-size: 3.8rem;
  color: #ffffff;
  font-weight: 300; /* Thinner font like the reference image */
  font-family: 'Inter', sans-serif; /* Clean sans-serif */
  letter-spacing: 2px;
  line-height: 1;
  text-align: center;
  font-variant-numeric: tabular-nums; /* Monospaced numbers */
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.4);
}

.timer-label {
  display: none; /* Removed based on reference image */
}

/* Custom Action Buttons */
.task-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 2.5rem;
}

.action-btn {
  flex: 1;
  border: none;
  outline: none;
  padding: 14px;
  border-radius: 18px;
  font-weight: 700;
  font-size: 1.05rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: white;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.success-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.success-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.5),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.danger-btn {
  background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);
  box-shadow: 0 4px 15px rgba(239, 68, 68, 0.3),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);
  color: #ffe4e6;
}

.danger-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.5),
              inset 0 1px 0 rgba(255, 255, 255, 0.3);
  color: white;
}

.btn-icon {
  font-size: 1.2rem;
  font-weight: 800;
}

.action-btn:active {
  transform: scale(0.98);
}

/* History List */
.history-list {
  max-width: 750px;
  margin: 0 auto;
}

.history-item {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(30, 27, 43, 0.5) 0%, rgba(26, 22, 37, 0.7) 100%);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  padding: 1.3rem;
  margin-bottom: 1rem;
  border: 2px solid rgba(167, 139, 250, 0.15);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.5s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.history-item:hover {
  background: linear-gradient(135deg, rgba(30, 27, 43, 0.7) 0%, rgba(26, 22, 37, 0.9) 100%);
  border-color: rgba(16, 185, 129, 0.4);
  transform: translateX(6px);
  box-shadow: 0 6px 30px rgba(16, 185, 129, 0.2);
}

.history-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(5, 150, 105, 0.3));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #10b981;
  margin-right: 1.2rem;
  flex-shrink: 0;
  box-shadow: 0 0 20px rgba(16, 185, 129, 0.3);
  border: 2px solid rgba(16, 185, 129, 0.4);
  transition: all 0.3s ease;
}

.history-item:hover .history-icon {
  box-shadow: 0 0 30px rgba(16, 185, 129, 0.5);
  transform: rotate(360deg);
}

.history-content {
  flex: 1;
}

.history-title {
  margin: 0;
  color: white;
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: -0.2px;
}

.history-meta {
  margin: 6px 0 0;
  color: #9ca3af;
  font-size: 0.9rem;
  font-weight: 500;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 1rem;
}

.empty-icon {
  font-size: 5rem;
  margin: 0;
  filter: grayscale(0.3);
  animation: float-icon 3s ease-in-out infinite;
}

@keyframes float-icon {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-text {
  color: #9ca3af;
  font-size: 1.3rem;
  margin: 1.5rem 0 0.8rem;
  font-weight: 700;
}

.empty-hint {
  color: #6b7280;
  font-size: 1rem;
  margin: 0;
  font-weight: 500;
}

/* Manual Mode Styles */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.mode-switch {
  display: flex;
  align-items: center;
  gap: 10px;
  background: rgba(30, 27, 43, 0.4);
  padding: 6px 12px;
  border-radius: 20px;
  border: 1px solid rgba(167, 139, 250, 0.2);
}

.mode-switch span {
  font-size: 0.85rem;
  color: #888;
  font-weight: 600;
  transition: color 0.3s;
}

.mode-switch span.active {
  color: #3fecff;
}

.manual-time-picker {
  background: rgba(30, 27, 43, 0.3);
  padding: 1.5rem;
  border-radius: 16px;
  border: 1px dashed rgba(167, 139, 250, 0.3);
}

.time-input-group {
  margin-bottom: 1rem;
}

.time-input-group label {
  display: block;
  font-size: 0.9rem;
  color: #c4b5fd;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.native-time-input {
  width: 100%;
  color-scheme: dark;
  font-family: 'Inter', sans-serif;
  font-size: 1rem;
  padding: 10px;
}

/* Customizing the datetime-local input icon/color */
.native-time-input::-webkit-calendar-picker-indicator {
  filter: invert(1) opacity(0.6);
  cursor: pointer;
}

.native-time-input::-webkit-calendar-picker-indicator:hover {
  filter: invert(1) opacity(1);
}

.calculated-duration {
  text-align: right;
  font-size: 0.95rem;
  color: #888;
  margin-top: 0.8rem;
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 0.8rem;
}

.calculated-duration span {
  color: #3fecff;
  font-weight: 700;
  font-size: 1.1rem;
  margin-left: 5px;
}
</style>
