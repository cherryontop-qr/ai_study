<template>
  <div class="ai-assistant">
    <el-row :gutter="16">
      <!-- 学习建议 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI 学习建议</span>
              <el-button size="small" @click="loadSuggestion" :loading="suggestionLoading">
                重新生成
              </el-button>
            </div>
          </template>
          <div class="suggestion-content">
            <div v-if="suggestion" class="suggestion-text">{{ suggestion }}</div>
            <el-empty v-else description="点击重新生成获取学习建议" />
          </div>
        </el-card>
      </el-col>

      <!-- 任务拆分计划 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI 任务拆分计划</span>
            </div>
          </template>

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
                <el-table-column prop="description" label="任务描述" show-overflow-tooltip />
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
import { ref, reactive } from 'vue';
import { ElMessage, ElMessageBox, ElForm } from 'element-plus';
import { getWeeklySuggestion, generateTaskPlan } from '@/api/ai';
import { createTask } from '@/api/task';
import type { AiTaskPlanRequest, AiTaskPlanResponse, AiPlannedTask } from '@/types/api';

const suggestion = ref('');
const suggestionLoading = ref(false);
const planLoading = ref(false);
const planResult = ref<AiTaskPlanResponse | null>(null);
const planFormRef = ref<InstanceType<typeof ElForm>>();

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

const loadSuggestion = async () => {
  suggestionLoading.value = true;
  try {
    const res = await getWeeklySuggestion();
    if (res.code === 0) {
      suggestion.value = res.data;
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

const generatePlan = async () => {
  if (!planFormRef.value) return;
  await planFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    planLoading.value = true;
    try {
      const res = await generateTaskPlan(planForm);
      if (res.code === 0) {
        planResult.value = res.data;
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
};

const extractCategory = (title: string, description: string): string => {
  // 简单的关键词提取逻辑
  const text = (title + ' ' + description).toLowerCase();
  const categories = ['java', 'spring', 'vue', 'react', '数据库', 'mysql', 'redis', 'docker', 'kubernetes'];
  for (const cat of categories) {
    if (text.includes(cat.toLowerCase())) {
      return cat;
    }
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
      category: category,
      status: 'TODO'
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
          category: category,
          status: 'TODO'
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

.suggestion-content {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
}

.suggestion-text {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
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
</style>

