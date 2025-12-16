<!-- src/views/Home.vue -->
<template>
  <div class="home-container">
    <el-container style="height: 100vh;">
      <el-header style="background: linear-gradient(135deg, #4c6fff, #73b4ff); color: white; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <div class="header-left">
          <h2 style="margin: 0;">AI 学习打卡系统</h2>
        </div>
        <div class="header-right" style="display: flex; align-items: center; gap: 15px;">
          <el-avatar :size="40" :src="avatarUrl" />
          <div class="user-info">
            <div class="nickname">{{ nickname }}</div>
            <div class="role">角色: {{ role }}</div>
          </div>
          <el-button type="text" style="color: white;" @click="logout">
            退出登录
          </el-button>
        </div>
      </el-header>

      <el-container>
        <el-aside width="200px" style="background-color: #f5f7fa;">
          <el-menu
              active-text-color="#4c6fff"
              background-color="#f5f7fa"
              :default-active="activeMenu"
              style="border-right: none;"
          >
            <el-menu-item index="1">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="2">
              <el-icon><Calendar /></el-icon>
              <span>学习打卡</span>
            </el-menu-item>
            <el-menu-item index="3">
              <el-icon><Histogram /></el-icon>
              <span>学习统计</span>
            </el-menu-item>
            <el-menu-item index="4" v-if="role === 'admin'">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="5">
              <el-icon><Setting /></el-icon>
              <span>个人设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main style="padding: 20px;">
          <el-card class="welcome-card">
            <template #header>
              <div class="card-header">
                <span>欢迎回来，{{ nickname }}！</span>
              </div>
            </template>

            <div class="user-stats">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-statistic title="用户ID" :value="userId" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="角色" :value="role" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="登录时间" :value="currentTime" />
                </el-col>
                <el-col :span="6">
                  <el-statistic title="系统状态" value="正常" />
                </el-col>
              </el-row>
            </div>

            <div class="dashboard-content" style="margin-top: 30px;">
              <h3>快速开始</h3>
              <el-row :gutter="20" style="margin-top: 20px;">
                <el-col :span="8">
                  <el-card shadow="hover" class="quick-card" @click="goToCheckin">
                    <div style="text-align: center;">
                      <el-icon size="40" color="#4c6fff"><Calendar /></el-icon>
                      <div style="margin-top: 10px; font-weight: bold;">今日打卡</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="quick-card" @click="goToStats">
                    <div style="text-align: center;">
                      <el-icon size="40" color="#4c6fff"><Histogram /></el-icon>
                      <div style="margin-top: 10px; font-weight: bold;">学习统计</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card shadow="hover" class="quick-card" @click="goToSettings">
                    <div style="text-align: center;">
                      <el-icon size="40" color="#4c6fff"><Setting /></el-icon>
                      <div style="margin-top: 10px; font-weight: bold;">个人设置</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessageBox, ElMessage } from 'element-plus';
import {
  House,
  Calendar,
  Histogram,
  User,
  Setting,
} from '@element-plus/icons-vue';

const store = useStore();
const router = useRouter();

const activeMenu = ref('1');
const currentTime = ref(new Date().toLocaleDateString());

// 计算属性
const nickname = computed(() => store.state.user.nickname);
const userId = computed(() => store.state.user.userId);
const role = computed(() => store.state.user.role);
const avatarUrl = computed(() => {
  // 可以根据用户信息生成头像URL，这里使用占位图
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${nickname.value}`;
});

// 方法
const logout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await store.dispatch('user/logout');
    ElMessage.success('退出成功');
    router.push('/login');
  } catch (error) {
    // 用户取消操作
  }
};

const goToCheckin = () => {
  ElMessage.info('前往打卡页面');
  // router.push('/checkin');
};

const goToStats = () => {
  ElMessage.info('前往统计页面');
  // router.push('/stats');
};

const goToSettings = () => {
  ElMessage.info('前往设置页面');
  // router.push('/settings');
};

onMounted(() => {
  // 检查用户是否已登录
  if (!store.state.user.token) {
    router.push('/login');
  }
});
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.header-left h2 {
  font-weight: bold;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.nickname {
  font-weight: bold;
  font-size: 16px;
}

.role {
  font-size: 12px;
  opacity: 0.8;
}

.welcome-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 18px rgba(76, 111, 255, 0.2);
}
</style>