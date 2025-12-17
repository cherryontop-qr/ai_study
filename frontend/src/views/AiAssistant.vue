<template>
  <div class="ai-assistant">
    <el-row :gutter="16">
      <!-- 学习建议 -->
      <el-col :span="12">
        <el-card class="suggestion-card">
          <template #header>
            <div class="card-header">
              <span>AI 学习建议</span>
              <div class="header-actions">
                <el-button size="small" @click="showHistory = !showHistory">
                  {{ showHistory ? '隐藏历史' : '查看历史' }}
                </el-button>
                <el-button size="small" @click="loadSuggestion" :loading="suggestionLoading">
                  重新生成
                </el-button>
              </div>
            </div>
          </template>

          <!-- 建议历史记录 -->
          <div v-if="showHistory && suggestionHistory.length > 0" class="history-list">
            <div
              v-for="(item, index) in suggestionHistory"
              :key="index"
              class="history-item"
              :class="{ active: index === currentHistoryIndex }"
              @click="loadHistorySuggestion(index)"
            >
              <div class="history-time">{{ item.time }}</div>
              <div class="history-preview">{{ item.content.substring(0, 50) }}...</div>
            </div>
          </div>

          <div class="suggestion-content" :class="{ 'with-history': showHistory && suggestionHistory.length > 0 }">
            <div v-if="suggestion" class="suggestion-text">{{ suggestion }}</div>
            <el-empty v-else description="点击重新生成获取学习建议" />
          </div>
        </el-card>
      </el-col>

      <!-- 任务拆分计划 -->
      <el-col :span="12">
        <el-card class="plan-card">
          <template #header>
            <div class="card-header">
              <span>AI 任务拆分计划</span>
              <div class="header-actions">
                <el-button size="small" @click="showPlanHistory = !showPlanHistory">
                  {{ showPlanHistory ? '隐藏历史' : '计划历史' }}
                </el-button>
              </div>
            </div>
          </template>

          <!-- 计划历史列表 -->
          <div v-if="showPlanHistory && planHistory.length > 0" class="history-list plan-history">
            <div
              v-for="(item, index) in planHistory"
              :key="index"
              class="history-item"
              :class="{ active: index === currentPlanHistoryIndex }"
              @click="loadPlanFromHistory(index)"
            >
              <div class="history-time">{{ item.time }}</div>
              <div class="history-preview">任务数：{{ item.tasksCount }} ｜ {{ item.summary || '无摘要' }}</div>
            </div>
          </div>

          <el-form :model="planForm" :rules="planRules" ref="planFormRef" label-width="100px">
            <el-form-item label="学习目标" prop="goalDescription">
              <el-input
                v-model="planForm.goalDescription"
                type="textarea"
                :rows="3"
                placeholder="例如：我想学习 Spring Boot 框架，掌握 RESTful API 开发"
              />
            </el-form-item>
            <el-form-item label="期望任务数" prop="taskCount">
              <el-input-number
                v-model="planForm.taskCount"
                :min="1"
                :max="20"
                placeholder="任务数量"
              />
            </el-form-item>
            <el-form-item label="个性偏好">
              <el-input
                v-model="planForm.preference"
                placeholder="例如：擅长后端开发，喜欢实践项目"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="generatePlan" :loading="planLoading">
                生成任务计划
              </el-button>
              <el-button @click="clearPlan">清除</el-button>
            </el-form-item>
          </el-form>

          <el-divider v-if="planResult" />

          <div v-if="planResult" class="plan-result">
            <div class="plan-summary" v-if="planResult.summary">
              <h4>计划总结</h4>
              <p>{{ planResult.summary }}</p>
            </div>
            <div class="plan-tasks">
              <h4>任务列表</h4>
              <el-table :data="planResult.tasks" style="margin-top: 12px;">
                <el-table-column prop="title" label="任务标题" width="200" />
                <el-table-column label="任务描述" min-width="200">
                  <template #default="{ row }">
                    <el-tooltip
                      effect="dark"
                      :content="row.description"
                      placement="top"
                      :popper-style="{ maxWidth: '300px' }"
                    >
                      <div class="description-cell">
                        {{ row.description }}
                      </div>
                    </el-tooltip>
                  </template>
                </el-table-column>
                <el-table-column prop="suggestedHours" label="建议时长" width="100">
                  <template #default="{ row }">{{ row.suggestedHours }} 小时</template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button size="small" type="primary" @click="createTaskFromPlan(row)">
                      创建任务
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div style="margin-top: 12px;">
                <el-button type="success" @click="createAllTasks">
                  一键创建所有任务
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { getWeeklySuggestion, generateTaskPlan } from '@/api/ai';
import { createTask } from '@/api/task';
import type { AiTaskPlanRequest, AiTaskPlanResponse, AiPlannedTask } from '@/types/api';

// 本地存储 key
const STORAGE_SUGGESTION_HISTORY = 'ai_suggestion_history';
const STORAGE_CURRENT_SUGGESTION = 'ai_current_suggestion';
const STORAGE_PLAN_CURRENT = 'ai_plan_current';
const STORAGE_PLAN_HISTORY = 'ai_plan_history';
const STORAGE_PLAN_FORM = 'ai_plan_form';

const suggestion = ref('');
const suggestionLoading = ref(false);
const planLoading = ref(false);
const planResult = ref<AiTaskPlanResponse | null>(null);
const planFormRef = ref<InstanceType<typeof ElForm>>();
const showHistory = ref(false);
const currentHistoryIndex = ref(-1);
const showPlanHistory = ref(false);
const currentPlanHistoryIndex = ref(-1);

interface SuggestionHistoryItem {
  time: string;
  content: string;
}

const suggestionHistory = ref<SuggestionHistoryItem[]>([]);

interface PlanHistoryItem {
  time: string;
  summary?: string;
  tasksCount: number;
  data: AiTaskPlanResponse;
}

const planHistory = ref<PlanHistoryItem[]>([]);

const planForm = reactive<AiTaskPlanRequest>({
  goalDescription: '',
  taskCount: 5,
  preference: ''
});

const planRules = {
  goalDescription: [
    { required: true, message: '请输入学习目标', trigger: 'blur' }
  ],
  taskCount: [
    { required: true, message: '请输入期望任务数', trigger: 'blur' }
  ]
};

const saveSuggestionHistory = () => {
  const history: SuggestionHistoryItem[] = [
    {
      time: new Date().toLocaleString(),
      content: suggestion.value
    },
    ...suggestionHistory.value
  ];
  suggestionHistory.value = history.slice(0, 20);
  localStorage.setItem(STORAGE_SUGGESTION_HISTORY, JSON.stringify(suggestionHistory.value));
  localStorage.setItem(STORAGE_CURRENT_SUGGESTION, suggestion.value);
  currentHistoryIndex.value = 0;
};

const loadHistorySuggestion = (index: number) => {
  if (index < 0 || index >= suggestionHistory.value.length) return;
  currentHistoryIndex.value = index;
  suggestion.value = suggestionHistory.value[index].content;
  localStorage.setItem(STORAGE_CURRENT_SUGGESTION, suggestion.value);
};

const loadSuggestion = async () => {
  suggestionLoading.value = true;
  try {
    const res = await getWeeklySuggestion();
    if (res.code === 0) {
      suggestion.value = res.data;
      saveSuggestionHistory();
      ElMessage.success('学习建议生成成功');
    } else {
      ElMessage.error(res.message || '获取学习建议失败');
    }
  } catch (error: any) {
    console.error('获取学习建议失败:', error);
    ElMessage.error('获取学习建议失败');
  } finally {
    suggestionLoading.value = false;
  }
};

const savePlanState = () => {
  if (planResult.value) {
    localStorage.setItem(
      STORAGE_PLAN_CURRENT,
      JSON.stringify(planResult.value)
    );
    const historyItem: PlanHistoryItem = {
      time: new Date().toLocaleString(),
      summary: planResult.value.summary,
      tasksCount: planResult.value.tasks?.length || 0,
      data: planResult.value
    };
    planHistory.value = [historyItem, ...planHistory.value].slice(0, 20);
    localStorage.setItem(STORAGE_PLAN_HISTORY, JSON.stringify(planHistory.value));
    currentPlanHistoryIndex.value = 0;
  }
};

const loadPlanFromHistory = (index: number) => {
  if (index < 0 || index >= planHistory.value.length) return;
  currentPlanHistoryIndex.value = index;
  planResult.value = planHistory.value[index].data;
  localStorage.setItem(STORAGE_PLAN_CURRENT, JSON.stringify(planResult.value));
};

const generatePlan = async () => {
  if (!planFormRef.value) return;
  await planFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    planLoading.value = true;
    try {
      const res = await generateTaskPlan(planForm);
      if (res.code === 0) {
        planResult.value = res.data;
        savePlanState();
        ElMessage.success('任务计划生成成功');
      } else {
        ElMessage.error(res.message || '生成任务计划失败');
      }
    } catch (error: any) {
      console.error('生成任务计划失败:', error);
      ElMessage.error('生成任务计划失败');
    } finally {
      planLoading.value = false;
    }
  });
};

const clearPlan = () => {
  planForm.goalDescription = '';
  planForm.taskCount = 5;
  planForm.preference = '';
  planResult.value = null;
  planFormRef.value?.clearValidate();
  localStorage.removeItem(STORAGE_PLAN_CURRENT);
};

const extractCategory = (title: string, description: string): string => {
  // 从学习目标 + 标题 + 描述中提取分类
  const base = planForm.goalDescription || '';
  const text = (base + ' ' + title + ' ' + description).toLowerCase();
  const categories = ['java', 'spring', 'vue', 'react', '数据库', 'mysql', 'redis', 'docker', 'kubernetes'];
  for (const cat of categories) {
    if (text.includes(cat.toLowerCase())) {
      return cat;
    }
  }
  // 如果没有匹配关键词，用学习目标前 8 个字作为默认分类
  if (base.trim()) {
    return base.trim().slice(0, 8);
  }
  return '其他';
};

const createTaskFromPlan = async (plannedTask: AiPlannedTask) => {
  try {
    const category = extractCategory(plannedTask.title, plannedTask.description);
    const res = await createTask({
      title: plannedTask.title,
      description: plannedTask.description,
      targetHours: plannedTask.suggestedHours,
      category
    });
    if (res.code === 0) {
      ElMessage.success('任务创建成功');
    } else {
      ElMessage.error(res.message || '创建任务失败');
    }
  } catch (error: any) {
    console.error('创建任务失败:', error);
    ElMessage.error('创建任务失败');
  }
};

const createAllTasks = async () => {
  if (!planResult.value || !planResult.value.tasks || planResult.value.tasks.length === 0) {
    ElMessage.warning('没有可创建的任务');
    return;
  }

  try {
    await ElMessageBox.confirm(
      `确定要创建 ${planResult.value.tasks.length} 个任务吗？`,
      '提示',
      { type: 'warning' }
    );

    let successCount = 0;
    for (const plannedTask of planResult.value.tasks) {
      try {
        const category = extractCategory(plannedTask.title, plannedTask.description);
        await createTask({
          title: plannedTask.title,
          description: plannedTask.description,
          targetHours: plannedTask.suggestedHours,
          category
        });
        successCount++;
      } catch (error) {
        console.error('创建任务失败:', error);
      }
    }

    ElMessage.success(`成功创建 ${successCount} 个任务`);
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量创建任务失败:', error);
    }
  }
};

// 加载本地历史与当前状态（保证页面切换后保留）
onMounted(() => {
  // 建议历史
  const historyRaw = localStorage.getItem(STORAGE_SUGGESTION_HISTORY);
  if (historyRaw) {
    try {
      suggestionHistory.value = JSON.parse(historyRaw);
    } catch {
      suggestionHistory.value = [];
    }
  }
  const currentSuggestion = localStorage.getItem(STORAGE_CURRENT_SUGGESTION);
  if (currentSuggestion) {
    suggestion.value = currentSuggestion;
  }

  // 计划当前结果
  const planCurrentRaw = localStorage.getItem(STORAGE_PLAN_CURRENT);
  if (planCurrentRaw) {
    try {
      planResult.value = JSON.parse(planCurrentRaw);
    } catch {
      planResult.value = null;
    }
  }

  // 计划历史
  const planHistoryRaw = localStorage.getItem(STORAGE_PLAN_HISTORY);
  if (planHistoryRaw) {
    try {
      planHistory.value = JSON.parse(planHistoryRaw);
    } catch {
      planHistory.value = [];
    }
  }

  // 表单持久化
  const formRaw = localStorage.getItem(STORAGE_PLAN_FORM);
  if (formRaw) {
    try {
      const saved = JSON.parse(formRaw) as AiTaskPlanRequest;
      planForm.goalDescription = saved.goalDescription || '';
      planForm.taskCount = saved.taskCount || 5;
      planForm.preference = saved.preference || '';
    } catch {
      // ignore
    }
  }
});

// 实时保存表单
watch(
  () => ({ ...planForm }),
  (val) => {
    localStorage.setItem(STORAGE_PLAN_FORM, JSON.stringify(val));
  },
  { deep: true }
);
</script>

<style scoped>
.ai-assistant {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.suggestion-card,
.plan-card {
  height: 100%;
}

.suggestion-content {
  min-height: 260px;
  max-height: 420px;
  overflow-y: auto;
  padding: 16px 18px;
  border-radius: 14px;
  background: radial-gradient(circle at top left, rgba(129, 140, 248, 0.25), rgba(56, 189, 248, 0.08)),
    linear-gradient(135deg, rgba(76, 111, 255, 0.12), rgba(145, 101, 255, 0.2));
  box-shadow:
    0 12px 30px rgba(15, 23, 42, 0.15),
    inset 0 0 0 1px rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  color: #111827;
  border: 1px solid rgba(129, 140, 248, 0.35);
  position: relative;
}

.suggestion-content::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(circle at top right, rgba(244, 244, 245, 0.3), transparent 60%);
  pointer-events: none;
  opacity: 0.9;
}

.suggestion-content.with-history {
  margin-top: 8px;
}

.suggestion-text {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 14px;
  color: #111827;
  text-shadow: 0 1px 1px rgba(255, 255, 255, 0.6);
}

.history-list {
  max-height: 140px;
  overflow-y: auto;
  margin-bottom: 8px;
  padding: 8px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.7);
  color: #e5e7eb;
}

.plan-history {
  margin-bottom: 12px;
}

.history-item {
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.history-item + .history-item {
  margin-top: 4px;
}

.history-item:hover {
  background: rgba(148, 163, 184, 0.3);
}

.history-item.active {
  background: rgba(59, 130, 246, 0.5);
}

.history-time {
  font-size: 11px;
  opacity: 0.75;
}

.history-preview {
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plan-result {
  margin-top: 16px;
}

.plan-summary {
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.plan-summary h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.plan-summary p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.plan-tasks h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.description-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

