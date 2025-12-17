<template>
  <div class="achievements-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习成就</span>
          <el-button size="small" @click="refreshAchievements" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>

      <div class="achievements-grid">
        <div
          v-for="achievement in achievementsList"
          :key="achievement.id"
          class="achievement-card"
          :class="{ achieved: achievement.unlocked }"
          @click="achievement.unlocked && showAchievementDetail(achievement)"
        >
          <div class="achievement-icon">
            <el-icon :size="48" :color="achievement.unlocked ? '#67c23a' : '#909399'">
              <component :is="achievement.icon" />
            </el-icon>
          </div>
          <div class="achievement-content">
            <div class="achievement-title">{{ achievement.title }}</div>
            <div class="achievement-desc">{{ achievement.description }}</div>
            <div class="achievement-progress" v-if="achievement.progress !== undefined">
              <el-progress
                :percentage="achievement.progress"
                :stroke-width="6"
                :color="achievement.unlocked ? '#67c23a' : '#409eff'"
              />
              <div class="progress-text">{{ achievement.progressText }}</div>
            </div>
          </div>
          <div class="achievement-badge">
            <el-tag v-if="achievement.unlocked" type="success" size="large">已解锁</el-tag>
            <el-tag v-else type="info" size="large">待解锁</el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 成就详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="selectedAchievement?.title"
      width="520px"
      class="achievement-detail-dialog"
      destroy-on-close
    >
      <div v-if="selectedAchievement" class="achievement-detail-body">
        <div class="detail-icon-wrapper" :class="{ unlocked: selectedAchievement.unlocked }">
          <div class="detail-icon-glow"></div>
          <el-icon :size="selectedAchievement.unlocked ? 96 : 80">
            <component :is="selectedAchievement.icon" />
          </el-icon>
          <div
            v-if="selectedAchievement.unlocked"
            class="detail-badge"
          >
            已解锁
          </div>
        </div>
        <div class="detail-text">
          <p class="detail-description">{{ selectedAchievement.description }}</p>
          <p v-if="selectedAchievement.unlocked" class="detail-unlock-time">
            解锁时间：{{ selectedAchievement.unlockTime || '未知' }}
          </p>
          <p v-if="selectedAchievement.progress !== undefined" class="detail-progress-text">
            {{ selectedAchievement.progressText }}
          </p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Trophy,
  Medal,
  StarFilled,
  CircleCheck,
  Timer,
  Flag,
  Collection
} from '@element-plus/icons-vue';
import { getTaskPage } from '@/api/task';
import type { Task } from '@/types/api';

const loading = ref(false);
const detailVisible = ref(false);
const selectedAchievement = ref<any>(null);

const STORAGE_TODOS = 'dashboard_today_todos';
const STORAGE_TARGET = 'dashboard_today_target';
const STORAGE_LEARNED = 'task_learned_minutes';
const STORAGE_WEEK = 'dashboard_week_info';
const STORAGE_ACHIEVEMENTS = 'user_achievements';

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: any;
  unlocked: boolean;
  progress?: number;
  progressText?: string;
  unlockTime?: string;
}

const achievementsList = ref<Achievement[]>([]);

const getTodayDateStr = (): string => {
  const d = new Date();
  const m = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  return `${d.getFullYear()}-${m}-${day}`;
};

const loadWeekInfo = () => {
  const raw = localStorage.getItem(STORAGE_WEEK);
  let info: { startDate: string; minutes: number };
  if (raw) {
    try {
      info = JSON.parse(raw);
    } catch {
      info = { startDate: getTodayDateStr(), minutes: 0 };
    }
  } else {
    info = { startDate: getTodayDateStr(), minutes: 0 };
  }
  return info;
};

const calculateAchievements = async () => {
  loading.value = true;
  try {
    // 获取任务数据
    let tasks: Task[] = [];
    try {
      const taskRes = await getTaskPage({ pageNum: 1, pageSize: 200, keyword: '', status: '' });
      if (taskRes.code === 0) {
        tasks = taskRes.data.list;
      }
    } catch (error) {
      console.error('获取任务失败:', error);
    }

    // 获取今日任务数据
    const todosRaw = localStorage.getItem(STORAGE_TODOS);
    const todos: any[] = todosRaw ? JSON.parse(todosRaw) : [];
    const target = localStorage.getItem(STORAGE_TARGET);
    const dailyTargetMinutes = target ? Number(target) : 120;
    const todayMinutes = todos
      .filter((t: any) => t.done)
      .reduce((s: number, t: any) => s + (t.todayTargetMinutes || 0), 0);

    // 获取学习时长数据
    const learnedRaw = localStorage.getItem(STORAGE_LEARNED);
    const learned: Record<number, number> = learnedRaw ? JSON.parse(learnedRaw) : {};
    const totalLearnedMinutes = Object.values(learned).reduce((sum, min) => sum + min, 0);

    // 计算已完成任务数
    const doneTasks = tasks.filter(
      (t: Task) => t.status === 'DONE' || (learned[t.id] || 0) >= t.targetHours * 60
    ).length;
    const totalTasks = tasks.length;
    const taskCompletionPercent = totalTasks > 0 ? Math.round((doneTasks / totalTasks) * 100) : 0;

    // 获取本周学习时长
    const weekInfo = loadWeekInfo();
    const weekMinutes = weekInfo.minutes;

    // 获取已解锁成就记录
    const achievementsRaw = localStorage.getItem(STORAGE_ACHIEVEMENTS);
    const unlockedAchievements: Record<string, string> = achievementsRaw
      ? JSON.parse(achievementsRaw)
      : {};

    // 定义所有成就
    const allAchievements: Achievement[] = [
      {
        id: 'todayGoal',
        title: '今日任务完成',
        description: '完成当日学习目标，获得「坚持之星」勋章',
        icon: Trophy,
        unlocked: todayMinutes >= dailyTargetMinutes && dailyTargetMinutes > 0,
        progress: dailyTargetMinutes > 0 ? Math.min(100, Math.round((todayMinutes / dailyTargetMinutes) * 100)) : 0,
        progressText: `今日完成 ${todayMinutes} / ${dailyTargetMinutes} 分钟`,
        unlockTime: unlockedAchievements.todayGoal
      },
      {
        id: 'weekStreak',
        title: '连续打卡 7 天',
        description: '坚持打卡 7 天，获得「毅力徽章」',
        icon: Medal,
        unlocked: weekMinutes >= 7 * 60, // 假设每天至少 60 分钟
        progress: Math.min(100, Math.round((weekMinutes / (7 * 60)) * 100)),
        progressText: `本周学习 ${weekMinutes} 分钟`,
        unlockTime: unlockedAchievements.weekStreak
      },
      {
        id: 'taskMaster',
        title: '任务达人',
        description: '完成 80% 以上任务，获得「任务达人」勋章',
        icon: StarFilled,
        unlocked: taskCompletionPercent >= 80,
        progress: taskCompletionPercent,
        progressText: `已完成 ${doneTasks} / ${totalTasks} 个任务 (${taskCompletionPercent}%)`,
        unlockTime: unlockedAchievements.taskMaster
      },
      {
        id: 'firstTask',
        title: '首次任务',
        description: '完成 1 个任务，获得「起步者」勋章',
        icon: CircleCheck,
        unlocked: doneTasks >= 1,
        progress: Math.min(100, doneTasks * 100),
        progressText: `已完成 ${doneTasks} 个任务`,
        unlockTime: unlockedAchievements.firstTask
      },
      {
        id: 'fiveTasks',
        title: '五任务成就',
        description: '完成 5 个任务，获得「进阶者」勋章',
        icon: Collection,
        unlocked: doneTasks >= 5,
        progress: Math.min(100, Math.round((doneTasks / 5) * 100)),
        progressText: `已完成 ${doneTasks} / 5 个任务`,
        unlockTime: unlockedAchievements.fiveTasks
      },
      {
        id: 'focusDay',
        title: '专注日',
        description: '今日学习 120 分钟以上，获得「专注之星」勋章',
        icon: Timer,
        unlocked: todayMinutes >= 120,
        progress: Math.min(100, Math.round((todayMinutes / 120) * 100)),
        progressText: `今日学习 ${todayMinutes} / 120 分钟`,
        unlockTime: unlockedAchievements.focusDay
      },
      {
        id: 'learningMarathon',
        title: '学习马拉松',
        description: '累计学习 300 分钟以上，获得「马拉松」勋章',
        icon: Flag,
        unlocked: totalLearnedMinutes >= 300,
        progress: Math.min(100, Math.round((totalLearnedMinutes / 300) * 100)),
        progressText: `累计学习 ${totalLearnedMinutes} / 300 分钟`,
        unlockTime: unlockedAchievements.learningMarathon
      }
    ];

    // 检查新解锁的成就
    const now = new Date().toLocaleString('zh-CN');
    allAchievements.forEach(ach => {
      if (ach.unlocked && !unlockedAchievements[ach.id]) {
        unlockedAchievements[ach.id] = now;
        ElMessage.success(`🎉 恭喜解锁成就：${ach.title}！`);
      }
    });

    // 保存解锁记录
    localStorage.setItem(STORAGE_ACHIEVEMENTS, JSON.stringify(unlockedAchievements));

    // 更新解锁时间
    allAchievements.forEach(ach => {
      ach.unlockTime = unlockedAchievements[ach.id];
    });

    achievementsList.value = allAchievements;
  } catch (error) {
    console.error('计算成就失败:', error);
    ElMessage.error('计算成就失败');
  } finally {
    loading.value = false;
  }
};

const refreshAchievements = () => {
  calculateAchievements();
};

const showAchievementDetail = (achievement: Achievement) => {
  selectedAchievement.value = achievement;
  detailVisible.value = true;
};

onMounted(() => {
  calculateAchievements();
});
</script>

<style scoped>
.achievements-page {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.achievement-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border: 2px solid #ebeef5;
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: #fff;
}

.achievement-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.achievement-card.achieved {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9eb 0%, #ffffff 100%);
}

.achievement-icon {
  margin-bottom: 16px;
}

.achievement-content {
  flex: 1;
  text-align: center;
  width: 100%;
}

.achievement-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.achievement-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  line-height: 1.6;
}

.achievement-progress {
  margin-top: 12px;
  width: 100%;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.achievement-badge {
  margin-top: 16px;
}

.achievement-card.achieved .achievement-title {
  color: #67c23a;
}

.achievement-detail-dialog :deep(.el-dialog__body) {
  padding-top: 12px;
}

.achievement-detail-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.detail-icon-wrapper {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  background: radial-gradient(circle at 30% 20%, #e5f9e7, #e0f2fe);
  box-shadow:
    0 15px 30px rgba(15, 23, 42, 0.25),
    inset 0 0 0 1px rgba(255, 255, 255, 0.6);
}

.detail-icon-wrapper.unlocked {
  background: radial-gradient(circle at 30% 20%, #fef3c7, #bbf7d0);
  animation: float-medal 3s ease-in-out infinite;
}

.detail-icon-glow {
  position: absolute;
  inset: 6px;
  border-radius: inherit;
  background: radial-gradient(circle, rgba(250, 250, 250, 0.7), transparent 65%);
  opacity: 0.9;
  pointer-events: none;
}

.detail-badge {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #fff;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  box-shadow: 0 4px 10px rgba(22, 163, 74, 0.6);
}

.detail-text {
  max-width: 360px;
}

.detail-description {
  font-size: 15px;
  color: #374151;
  margin-bottom: 8px;
}

.detail-unlock-time {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
}

.detail-progress-text {
  font-size: 13px;
  color: #4b5563;
}

@keyframes float-medal {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}
</style>

