<template>
  <div class="task-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学习任务管理</span>
          <el-button type="primary" @click="showAddDialog">新增任务</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索任务标题"
          style="width: 300px; margin-right: 12px;"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="query.status"
          placeholder="状态筛选"
          style="width: 150px; margin-right: 12px;"
          clearable
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="待办" value="TODO" />
          <el-option label="进行中" value="DOING" />
          <el-option label="已完成" value="DONE" />
        </el-select>
        <el-select
          v-model="query.category"
          placeholder="分类筛选"
          style="width: 150px; margin-right: 12px;"
          clearable
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="taskList" v-loading="loading" style="margin-top: 16px;">
        <el-table-column prop="title" label="任务标题" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="targetHours" label="目标时长" width="100">
          <template #default="{ row }">{{ row.targetHours }} 小时</template>
        </el-table-column>
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getTaskProgress(row)"
              :color="getProgressColor(getTaskProgress(row))"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="addToToday(row)">加入今日</el-button>
            <el-button size="small" type="primary" @click="editTask(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTask(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="目标时长" prop="targetHours">
          <el-input-number
            v-model="form.targetHours"
            :min="1"
            :max="1000"
            placeholder="小时"
          />
          <span style="margin-left: 8px;">小时</span>
        </el-form-item>
        <el-form-item label="学习分类" prop="category">
          <el-input
            v-model="form.category"
            placeholder="如：Java、数据库、英语等"
          />
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="待办" value="TODO" />
            <el-option label="进行中" value="DOING" />
            <el-option label="已完成" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="date"
            placeholder="选择截止日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { getTaskPage, createTask, updateTask, deleteTask as deleteTaskApi } from '@/api/task';
import type { Task } from '@/types/api';

const loading = ref(false);
const taskList = ref<Task[]>([]);
const total = ref(0);
const dialogVisible = ref(false);
const dialogTitle = ref('新增任务');
const formRef = ref();
const editingTaskId = ref<number | null>(null);

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '',
  category: ''
});

const form = reactive({
  title: '',
  description: '',
  targetHours: 1,
  category: '',
  status: 'TODO',
  deadline: ''
});

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  targetHours: [{ required: true, message: '请输入目标时长', trigger: 'blur' }]
};

const STORAGE_LEARNED = 'task_learned_minutes';

const categories = computed(() => {
  const cats = new Set<string>();
  taskList.value.forEach(task => {
    if (task.category) cats.add(task.category);
  });
  return Array.from(cats).sort();
});

const getTaskProgress = (task: Task): number => {
  const learnedRaw = localStorage.getItem(STORAGE_LEARNED);
  const learned: Record<number, number> = learnedRaw ? JSON.parse(learnedRaw) : {};
  const learnedMinutes = learned[task.id] || 0;
  const targetMinutes = task.targetHours * 60;
  if (targetMinutes === 0) return 0;
  const progress = Math.round((learnedMinutes / targetMinutes) * 100);
  return Math.min(100, Math.max(0, progress));
};

const getProgressColor = (percentage: number): string => {
  if (percentage >= 100) return '#67c23a';
  if (percentage >= 50) return '#409eff';
  return '#e6a23c';
};

const getStatusType = (status: string): string => {
  const map: Record<string, string> = {
    TODO: 'info',
    DOING: 'warning',
    DONE: 'success'
  };
  return map[status] || 'info';
};

const getStatusText = (status: string): string => {
  const map: Record<string, string> = {
    TODO: '待办',
    DOING: '进行中',
    DONE: '已完成'
  };
  return map[status] || status;
};

const loadTasks = async () => {
  loading.value = true;
  try {
    const res = await getTaskPage(query);
    if (res.code === 0) {
      taskList.value = res.data.list;
      total.value = res.data.total;
    } else {
      ElMessage.error(res.message || '获取任务列表失败');
    }
  } catch (error: any) {
    console.error('加载任务失败:', error);
    ElMessage.error('加载任务失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  query.pageNum = 1;
  loadTasks();
};

const showAddDialog = () => {
  dialogTitle.value = '新增任务';
  editingTaskId.value = null;
  resetForm();
  dialogVisible.value = true;
};

const editTask = (task: Task) => {
  dialogTitle.value = '编辑任务';
  editingTaskId.value = task.id;
  form.title = task.title;
  form.description = task.description || '';
  form.targetHours = task.targetHours;
  form.category = task.category || '';
  form.status = task.status;
  form.deadline = task.deadline || '';
  dialogVisible.value = true;
};

const resetForm = () => {
  form.title = '';
  form.description = '';
  form.targetHours = 1;
  form.category = '';
  form.status = 'TODO';
  form.deadline = '';
  formRef.value?.clearValidate();
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return;
    try {
      if (editingTaskId.value) {
        const res = await updateTask(editingTaskId.value, form);
        if (res.code === 0) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          loadTasks();
        } else {
          ElMessage.error(res.message || '更新失败');
        }
      } else {
        const res = await createTask(form);
        if (res.code === 0) {
          ElMessage.success('创建成功');
          dialogVisible.value = false;
          loadTasks();
        } else {
          ElMessage.error(res.message || '创建失败');
        }
      }
    } catch (error: any) {
      console.error('提交失败:', error);
      ElMessage.error('操作失败');
    }
  });
};

const deleteTask = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      type: 'warning'
    });
    const res = await deleteTaskApi(id);
    if (res.code === 0) {
      ElMessage.success('删除成功');
      loadTasks();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }
};

const addToToday = (task: Task) => {
  const STORAGE_TODOS = 'dashboard_today_todos';
  const todosRaw = localStorage.getItem(STORAGE_TODOS);
  const todos: any[] = todosRaw ? JSON.parse(todosRaw) : [];
  
  const exists = todos.find(t => t.taskId === task.id);
  if (exists) {
    ElMessage.warning('该任务已在今日任务中');
    return;
  }

  const todayTask = {
    id: `task-${task.id}-${Date.now()}`,
    taskId: task.id,
    text: task.title,
    targetHours: task.targetHours,
    todayTargetMinutes: task.targetHours * 60,
    done: false
  };

  todos.push(todayTask);
  localStorage.setItem(STORAGE_TODOS, JSON.stringify(todos));
  ElMessage.success('已添加到今日任务');
};

onMounted(() => {
  loadTasks();
});
</script>

<style scoped>
.task-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
</style>

