<template>
  <div class="record-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>已完成任务</span>
        </div>
      </template>

      <el-table :data="completedTasks" v-loading="loading">
        <el-table-column prop="title" label="任务标题" width="200" />
        <el-table-column label="描述" min-width="200">
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
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="targetHours" label="目标时长" width="100">
          <template #default="{ row }">{{ row.targetHours }} 小时</template>
        </el-table-column>
        <el-table-column label="完成进度" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getTaskProgress(row)"
              status="success"
            />
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="120" />
        <el-table-column prop="createTime" label="完成时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && completedTasks.length === 0" description="暂无已完成任务" />
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getTaskPage } from '@/api/task';
import type { Task, TaskProgress } from '@/types/api';
import { getTaskProgress as getTaskProgressApi } from '@/api/record';

const loading = ref(false);
const completedTasks = ref<Task[]>([]);

// 由后端记录表统计得到的学习进度
const learnedMap = ref<Record<number, number>>({});

const getTaskProgress = (task: Task): number => {
  const learnedMinutes = learnedMap.value[task.id] || 0;
  const targetMinutes = task.targetHours * 60;
  if (targetMinutes === 0) return 0;
  const progress = Math.round((learnedMinutes / targetMinutes) * 100);
  return Math.min(100, Math.max(0, progress));
};

const formatDate = (dateStr: string): string => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};

const loadCompletedTasks = async () => {
  loading.value = true;
  try {
    // 先加载学习进度
    const progressRes = await getTaskProgressApi();
    if (progressRes.code === 0) {
      const map: Record<number, number> = {};
      (progressRes.data as TaskProgress[]).forEach(item => {
        map[item.taskId] = item.totalMinutes || 0;
      });
      learnedMap.value = map;
    }
    // 取出较多任务，然后在前端根据进度筛选出真正 100% 完成的
    const res = await getTaskPage({
      pageNum: 1,
      pageSize: 500,
      keyword: '',
      status: ''
    });
    if (res.code === 0) {
      const list: Task[] = res.data.list;
      completedTasks.value = list.filter(task => getTaskProgress(task) === 100);
    } else {
      ElMessage.error(res.message || '获取已完成任务失败');
    }
  } catch (error: any) {
    console.error('加载已完成任务失败:', error);
    ElMessage.error('加载已完成任务失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadCompletedTasks();
});
</script>

<style scoped>
.record-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description-cell {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>

