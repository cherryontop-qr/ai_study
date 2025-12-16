<template>
  <div class="today-tasks">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>今日任务</span>
        </div>
      </template>

      <div class="target-setting">
        <el-form :inline="true">
          <el-form-item label="今日目标时长：">
            <el-input-number
              v-model="dailyTargetMinutes"
              :min="0"
              :max="1440"
              :step="10"
              @change="saveTarget"
            />
            <span style="margin-left: 8px;">分钟</span>
          </el-form-item>
        </el-form>
      </div>

      <div class="today-summary">
        今日目标：{{ dailyTargetMinutes }} 分钟 ｜ 已完成：{{ todayMinutes }} 分钟
      </div>

      <el-divider />

      <div class="task-list">
        <div
          v-for="item in dailyTodos"
          :key="item.id"
          class="task-item"
        >
          <el-checkbox v-model="item.done" @change="() => toggleToday(item)">
            <div class="task-title">{{ item.text }}</div>
          </el-checkbox>
          <div class="task-meta">
            <span>目标 {{ item.targetHours || 0 }}h</span>
            <span>今日 {{ item.todayTargetMinutes || 0 }}m</span>
            <el-button
              size="small"
              type="danger"
              text
              @click="removeToday(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>
        <div v-if="dailyTodos.length === 0" class="empty-tip">
          暂无今日任务，请前往"学习任务"页面添加任务
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { updateTask } from '@/api/task';

const STORAGE_TODOS = 'dashboard_today_todos';
const STORAGE_TARGET = 'dashboard_today_target';
const STORAGE_LEARNED = 'task_learned_minutes';
const STORAGE_WEEK = 'dashboard_week_info';

const dailyTodos = ref<Array<any>>([]);
const dailyTargetMinutes = ref<number>(120);
const todayMinutes = ref<number>(0);

type WeekInfo = {
  startDate: string;
  minutes: number;
};

const getTodayDateStr = (): string => {
  const d = new Date();
  const m = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
};

const loadWeekInfo = (): WeekInfo => {
  const raw = localStorage.getItem(STORAGE_WEEK);
  let info: WeekInfo;
  if (raw) {
    try {
      info = JSON.parse(raw) as WeekInfo;
    } catch {
      info = { startDate: getTodayDateStr(), minutes: 0 };
    }
  } else {
    info = { startDate: getTodayDateStr(), minutes: 0 };
  }

  try {
    const start = new Date(info.startDate);
    const today = new Date(getTodayDateStr());
    const diffMs = today.getTime() - start.getTime();
    const diffDays = diffMs / (1000 * 60 * 60 * 24);
    if (diffDays >= 7 || diffDays < 0) {
      info = { startDate: getTodayDateStr(), minutes: 0 };
    }
  } catch {
    info = { startDate: getTodayDateStr(), minutes: 0 };
  }

  localStorage.setItem(STORAGE_WEEK, JSON.stringify(info));
  return info;
};

const updateWeekMinutesByDelta = (delta: number) => {
  if (!delta) return;
  const info = loadWeekInfo();
  info.minutes = Math.max(0, (info.minutes || 0) + delta);
  localStorage.setItem(STORAGE_WEEK, JSON.stringify(info));
};

const loadDailyData = () => {
  try {
    const todos = localStorage.getItem(STORAGE_TODOS);
    const target = localStorage.getItem(STORAGE_TARGET);
    if (todos) {
      dailyTodos.value = JSON.parse(todos);
    }
    if (target) {
      dailyTargetMinutes.value = Number(target);
    }
    todayMinutes.value = dailyTodos.value
      .filter((t: any) => t.done)
      .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);
  } catch (error) {
    console.error('加载今日任务失败:', error);
  }
};

const saveTarget = () => {
  localStorage.setItem(STORAGE_TARGET, String(dailyTargetMinutes.value));
};

const saveDailyData = () => {
  localStorage.setItem(STORAGE_TODOS, JSON.stringify(dailyTodos.value));
  localStorage.setItem(STORAGE_TARGET, String(dailyTargetMinutes.value));
};

const toggleToday = (item: any) => {
  const learnedRaw = localStorage.getItem(STORAGE_LEARNED);
  const learned: Record<number, number> = learnedRaw ? JSON.parse(learnedRaw) : {};
  const delta = item.todayTargetMinutes || (item.targetHours ? item.targetHours * 60 : 0);
  const current = learned[item.taskId || 0] || 0;

  if (item.done) {
    learned[item.taskId || 0] = current + delta;
    updateWeekMinutesByDelta(delta);
  } else {
    learned[item.taskId || 0] = Math.max(0, current - delta);
    updateWeekMinutesByDelta(-delta);
  }
  localStorage.setItem(STORAGE_LEARNED, JSON.stringify(learned));

  if (item.taskId && item.targetHours && learned[item.taskId] >= item.targetHours * 60) {
    try {
      updateTask(item.taskId, { status: 'DONE' });
    } catch (error) {
      // 忽略错误
    }
  }

  todayMinutes.value = dailyTodos.value
    .filter((t: any) => t.done)
    .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);

  saveDailyData();
};

const removeToday = (id: string) => {
  const removed = dailyTodos.value.find((t: any) => t.id === id);
  if (removed && removed.done) {
    const delta = removed.todayTargetMinutes || (removed.targetHours ? removed.targetHours * 60 : 0);
    updateWeekMinutesByDelta(-delta);
  }

  dailyTodos.value = dailyTodos.value.filter((t: any) => t.id !== id);
  todayMinutes.value = dailyTodos.value
    .filter((t: any) => t.done)
    .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);
  saveDailyData();
  ElMessage.success('已删除');
};

onMounted(() => {
  loadDailyData();
});
</script>

<style scoped>
.today-tasks {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.target-setting {
  margin-bottom: 16px;
}

.today-summary {
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.2s;
}

.task-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.task-title {
  font-size: 14px;
  color: #303133;
  margin-left: 8px;
}

.task-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
</style>

