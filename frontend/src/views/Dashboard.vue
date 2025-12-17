<template>
  <div class="dashboard">
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card class="stat-card" @click="goToToday" style="cursor: pointer;">
          <el-statistic :value="todayTodoDone">
            <template #title>
              今日任务完成数 / 总数
            </template>
            <template #suffix>
              / {{ todayTodoTotal }}
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" @click="goToTasks" style="cursor: pointer;">
          <el-statistic title="学习任务数（未完成）" :value="stats.totalTasks" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" @click="goToCompleted" style="cursor: pointer;">
          <el-statistic title="已完成任务数" :value="stats.doneTasks" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="本周学习时长" :value="stats.weekMinutes">
            <template #suffix>分钟</template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学习统计</span>
            </div>
          </template>
          <div class="learning-stat">
            <div class="progress-block">
              <div class="progress-title">今日任务进度</div>
              <el-progress type="circle" :percentage="dailyProgress" :width="160" stroke-linecap="round" status="success" />
              <div class="progress-sub">今日完成 {{ todayMinutes }} / {{ dailyTargetMinutes }} 分钟</div>
            </div>
            <div class="progress-block">
              <div class="progress-title">总任务完成率</div>
              <el-progress type="circle" :percentage="taskCompletionPercent" :width="160" stroke-linecap="round" />
              <div class="progress-sub">
                已完成 {{ stats.doneTasks }} / {{ stats.doneTasks + stats.totalTasks }}
              </div>
            </div>
            <div class="stat-details">
              <div class="stat-row">
                <span>已完成任务</span>
                <span class="stat-value">{{ stats.doneTasks }}</span>
              </div>
              <div class="stat-row">
                <span>未完成任务</span>
                <span class="stat-value info">{{ stats.totalTasks }}</span>
              </div>
              <div class="stat-row">
                <span>本周学习时长</span>
                <span class="stat-value">{{ stats.weekMinutes }} 分钟</span>
              </div>
              <div class="stat-actions">
                <el-button type="primary" size="default" @click="goToAi" class="ai-suggestion-btn">
                  <el-icon style="margin-right: 6px;"><ChatDotRound /></el-icon>
                  AI 学习建议
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日任务</span>
            </div>
          </template>
          <div class="today-list scrollable-today">
            <div class="today-summary">
              今日目标：{{ dailyTargetMinutes }} 分钟 ｜ 已完成：{{ todayMinutes }} 分钟
            </div>
            <div
              v-for="item in dailyTodos"
              :key="item.id"
              class="today-item"
            >
              <el-checkbox v-model="item.done" @change="() => toggleToday(item)">
                <div class="today-title">{{ item.text }}</div>
              </el-checkbox>
              <div class="today-meta">
                <span>目标 {{ item.targetHours || 0 }}h</span>
                <span>今日 {{ item.todayTargetMinutes || 0 }}m</span>
              </div>
            </div>
            <div v-if="dailyTodos.length === 0" class="empty-tip">暂无今日任务，请前往"今日任务"页面添加</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学习成就</span>
            </div>
          </template>
          <div class="achievements">
            <div class="achievement" :class="{ achieved: achievements.todayGoal }">
              <el-icon><Trophy /></el-icon>
              <div>
                <div class="ach-title">今日任务完成</div>
                <div class="ach-desc">完成当日学习目标，获得「坚持之星」勋章</div>
              </div>
              <el-tag v-if="achievements.todayGoal" type="success">已解锁</el-tag>
              <el-tag v-else type="info">待解锁</el-tag>
            </div>
            <div class="achievement" :class="{ achieved: achievements.weekStreak }">
              <el-icon><Medal /></el-icon>
              <div>
                <div class="ach-title">连续打卡 7 天</div>
                <div class="ach-desc">坚持打卡 7 天，获得「毅力徽章」</div>
              </div>
              <el-tag v-if="achievements.weekStreak" type="success">已解锁</el-tag>
              <el-tag v-else type="info">待解锁</el-tag>
            </div>
            <div class="achievement" :class="{ achieved: achievements.taskMaster }">
              <el-icon><StarFilled /></el-icon>
              <div>
                <div class="ach-title">任务达人</div>
                <div class="ach-desc">完成 80% 以上任务，获得「任务达人」勋章</div>
              </div>
              <el-tag v-if="achievements.taskMaster" type="success">已解锁</el-tag>
              <el-tag v-else type="info">待解锁</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Trophy, Medal, StarFilled, ChatDotRound } from '@element-plus/icons-vue';
import { getTaskPage, updateTask } from '@/api/task';
import type { Task, TaskProgress } from '@/types/api';
import { createRecord, getTaskProgress as getTaskProgressApi } from '@/api/record';

const router = useRouter();
const loading = ref(false);
const stats = reactive({
  // totalTasks：学习任务数量（未完成任务数）
  totalTasks: 0,
  // doneTasks：已完成任务数
  doneTasks: 0,
  // doingTasks：保留字段，目前不单独展示
  doingTasks: 0,
  weekRecords: 0,
  weekMinutes: 0
});

// 今日任务与目标（本地存储，仅用于展示）
const STORAGE_TODOS = 'dashboard_today_todos';
const STORAGE_TARGET = 'dashboard_today_target';
// 由后端记录表统计得到的学习进度（分钟）
const learnedMap = ref<Record<number, number>>({});
const STORAGE_WEEK = 'dashboard_week_info';
const dailyTodos = ref<Array<any>>([]);
const dailyTargetMinutes = ref<number>(120);
const todayMinutes = ref<number>(0);
const newTodoText = ref('');
const todayTodoTotal = ref<number>(0);
const todayTodoDone = ref<number>(0);

// 成就
const achievements = reactive({
  todayGoal: false,
  weekStreak: false,
  taskMaster: false
});

const taskCompletionPercent = computed(() => {
  const total = stats.totalTasks + stats.doneTasks;
  if (total === 0) return 0;
  const pct = Math.round((stats.doneTasks / total) * 100);
  return Math.min(100, Math.max(0, pct));
});

const dailyProgress = computed(() => {
  if (dailyTargetMinutes.value <= 0) return 0;
  const pct = Math.round((todayMinutes.value / dailyTargetMinutes.value) * 100);
  return Math.min(100, Math.max(0, pct));
});

type WeekInfo = {
  startDate: string; // YYYY-MM-DD
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

  // 判断是否超过 7 天，超过则重新开一周
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

const saveWeekInfo = (info: WeekInfo) => {
  localStorage.setItem(STORAGE_WEEK, JSON.stringify(info));
  stats.weekMinutes = info.minutes;
};

const recomputeWeekMinutes = () => {
  const info = loadWeekInfo();
  stats.weekMinutes = info.minutes;
};

const updateWeekMinutesByDelta = (delta: number) => {
  if (!delta) return;
  const info = loadWeekInfo();
  info.minutes = Math.max(0, (info.minutes || 0) + delta);
  saveWeekInfo(info);
};

// 创建一条学习记录，写入数据库
const createStudyRecord = async (taskId: number, minutes: number) => {
  try {
    if (!taskId || !minutes) return;
    await createRecord({
      taskId,
      studyDate: getTodayDateStr(),
      durationMinutes: minutes,
      comment: '今日任务打卡'
    });
  } catch (error) {
    console.error('创建学习记录失败:', error);
  }
};

const refreshLearnedMap = async () => {
  try {
    const res = await getTaskProgressApi();
    if (res.code === 0) {
      const map: Record<number, number> = {};
      (res.data as TaskProgress[]).forEach(item => {
        map[item.taskId] = item.totalMinutes || 0;
      });
      learnedMap.value = map;
    }
  } catch (error) {
    console.error('加载学习进度失败:', error);
  }
};

const fetchStats = async () => {
  loading.value = true;
  try {
    // 尝试获取任务列表（如果后端接口可用）
    try {
      const taskRes = await getTaskPage({ pageNum: 1, pageSize: 200, keyword: '', status: '' });
      if (taskRes.code === 0) {
        const tasks = taskRes.data.list;

        // 已完成任务数：学习记录累计时长达到目标时长视为完成
        stats.doneTasks = tasks.filter((t: Task) => {
          const learnedMinutes = learnedMap.value[t.id] || 0;
          return learnedMinutes >= t.targetHours * 60;
        }).length;
        // 未完成任务数：总数减去已完成
        stats.totalTasks = Math.max(0, tasks.length - stats.doneTasks);
        // 进行中任务暂不区分，先置 0
        stats.doingTasks = 0;
      }
    } catch (error) {
      // 如果后端接口不可用（任务功能还没实现），使用默认值
      console.log('任务接口暂不可用，使用默认统计数据');
      stats.doneTasks = 0;
      stats.totalTasks = 0;
    }

    // 今日分钟数来自今日任务存储
    const todosRaw = localStorage.getItem(STORAGE_TODOS);
    if (todosRaw) {
      dailyTodos.value = JSON.parse(todosRaw);
      todayMinutes.value = dailyTodos.value
        .filter((t: any) => t.done)
        .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);
      todayTodoTotal.value = dailyTodos.value.length;
      todayTodoDone.value = dailyTodos.value.filter((t: any) => t.done).length;
    } else {
      todayTodoTotal.value = 0;
      todayTodoDone.value = 0;
    }
    const target = localStorage.getItem(STORAGE_TARGET);
    if (target) dailyTargetMinutes.value = Number(target);

    // 其他统计（打卡记录已停用）
    stats.weekRecords = 0;
    // 重新统计累计学习时长
    recomputeWeekMinutes();

    achievements.taskMaster = taskCompletionPercent.value >= 80;
    achievements.weekStreak = stats.weekRecords >= 7;
    achievements.todayGoal = dailyProgress.value >= 100;
  } catch (error) {
    console.error('获取统计数据失败:', error);
  } finally {
    loading.value = false;
  }
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
    todayTodoTotal.value = dailyTodos.value.length;
    todayTodoDone.value = dailyTodos.value.filter((t: any) => t.done).length;
    recomputeWeekMinutes();
  } catch (error) {
    console.error('加载今日任务失败:', error);
  }
};

const saveDailyData = () => {
  localStorage.setItem(STORAGE_TODOS, JSON.stringify(dailyTodos.value));
  localStorage.setItem(STORAGE_TARGET, String(dailyTargetMinutes.value));
  achievements.todayGoal = dailyProgress.value >= 100;
};

const toggleToday = async (item: any) => {
  const delta = item.todayTargetMinutes || (item.targetHours ? item.targetHours * 60 : 0);
  const taskId = item.taskId as number | undefined;

  if (item.done) {
    // 本周学习时长增加
    updateWeekMinutesByDelta(delta);
    // 写一条学习记录到数据库，并更新本地进度
    if (taskId) {
      await createStudyRecord(taskId, delta);
      const prev = learnedMap.value[taskId] || 0;
      learnedMap.value = { ...learnedMap.value, [taskId]: prev + delta };
      // 如果累计学习时长达到目标，尝试把任务标记为已完成
      if (item.targetHours && learnedMap.value[taskId] >= item.targetHours * 60) {
        try {
          await updateTask(taskId, { status: 'DONE' });
        } catch {
          // 忽略错误，任务接口可能暂不可用
        }
      }
    }
  } else {
    // 取消勾选则减少本周学习时长，并回退本地进度
    updateWeekMinutesByDelta(-delta);
    if (taskId) {
      const prev = learnedMap.value[taskId] || 0;
      learnedMap.value = { ...learnedMap.value, [taskId]: Math.max(0, prev - delta) };
    }
  }

  todayMinutes.value = dailyTodos.value
    .filter((t: any) => t.done)
    .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);
  todayTodoTotal.value = dailyTodos.value.length;
  todayTodoDone.value = dailyTodos.value.filter((t: any) => t.done).length;

  saveDailyData();
  // 重新计算任务统计，保证顶部数字和圆形统计图同步
  await fetchStats();
};

const goToTasks = () => {
  router.push('/tasks');
};

const goToCompleted = () => {
  router.push('/records');
};

const goToAi = () => {
  router.push('/ai');
};

const removeToday = (id: string) => {
  const removed = dailyTodos.value.find((t: any) => t.id === id);
  // 如果删除的是已完成任务，需要把对应的分钟数从本周学习时长中扣除
  if (removed && removed.done) {
    const delta = removed.todayTargetMinutes || (removed.targetHours ? removed.targetHours * 60 : 0);
    updateWeekMinutesByDelta(-delta);
  }

  dailyTodos.value = dailyTodos.value.filter((t: any) => t.id !== id);
  todayMinutes.value = dailyTodos.value
    .filter((t: any) => t.done)
    .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);
  todayTodoTotal.value = dailyTodos.value.length;
  todayTodoDone.value = dailyTodos.value.filter((t: any) => t.done).length;
  saveDailyData();
};

const goToToday = () => {
  router.push('/today');
};

onMounted(async () => {
  loadDailyData();
  await refreshLearnedMap();
  await fetchStats();
});
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card {
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.learning-stat {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.progress-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.progress-title {
  font-weight: 600;
  font-size: 14px;
}

.progress-sub {
  font-size: 12px;
  color: #909399;
}

.stat-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-weight: 600;
}

.stat-value.info {
  color: #409eff;
}

.stat-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  justify-content: center;
}

.ai-suggestion-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 12px 24px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.ai-suggestion-btn:hover {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.5);
  transform: translateY(-2px);
}

.today-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.scrollable-today {
  max-height: 280px;
  overflow-y: auto;
  padding-right: 6px;
}

.today-summary {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.today-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f2f2;
}

.today-item:last-child {
  border-bottom: none;
}

.today-title {
  font-size: 14px;
  color: #303133;
}

.today-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 12px 0;
}

.achievements {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.achievement {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.achievement .el-icon {
  font-size: 24px;
  color: #f7ba2a;
}

.achievement.achieved {
  border-color: #67c23a;
  background-color: #f0f9eb;
}

.ach-title {
  font-weight: 600;
  font-size: 14px;
}

.ach-desc {
  font-size: 12px;
  color: #606266;
}
</style>

