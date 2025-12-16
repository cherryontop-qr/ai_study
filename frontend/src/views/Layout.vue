<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-icon">📚</div>
        <div class="logo-text">智能学习助手</div>
      </div>
      <el-menu :default-active="activeMenu" class="menu" router>
        <el-menu-item index="/">
          <span>主页</span>
        </el-menu-item>
        <el-menu-item index="/today">
          <span>今日任务</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <span>学习任务</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <span>已完成任务</span>
        </el-menu-item>
        <el-menu-item index="/ai">
          <span>AI 学习教练</span>
        </el-menu-item>
        <el-menu-item index="/achievements">
          <span>学习成就</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">欢迎，{{ nickname }}</div>
        <div class="header-right">
          <el-button type="text" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';

const route = useRoute();
const router = useRouter();
const store = useStore();

const activeMenu = computed(() => route.path || '/');
const nickname = computed(() => store.state.user.nickname || '同学');

const onLogout = () => {
  store.dispatch('user/logout');
  router.push('/login');
};
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background-color: #1f2933;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  background: linear-gradient(135deg, rgba(76, 111, 255, 0.2), rgba(115, 180, 255, 0.2));
  padding: 0 16px;
  transition: all 0.3s ease;
  cursor: default;
}

.logo:hover {
  background: linear-gradient(135deg, rgba(76, 111, 255, 0.3), rgba(115, 180, 255, 0.3));
}

.logo-icon {
  font-size: 28px;
  line-height: 1;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-5px);
  }
}

.logo-text {
  background: linear-gradient(135deg, #ffffff, #e0e7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.menu {
  border-right: none;
  flex: 1;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.main {
  padding: 16px;
  background-color: #f5f5f5;
}
</style>







