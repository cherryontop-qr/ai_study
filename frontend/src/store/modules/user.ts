// src/store/modules/user.ts
import { Module } from 'vuex'

// 用户模块状态类型
export interface UserState {
  token: string
  userId: number
  nickname: string
  role: string
}

// 登录参数类型
export interface LoginParams {
  username: string
  password: string
}

// 登录响应类型
export interface LoginResponse {
  token: string
  userId: number
  nickname: string
  role: string
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const state: UserState = {
  token: localStorage.getItem('token') || '',
  userId: Number(localStorage.getItem('userId')) || 0,
  nickname: localStorage.getItem('nickname') || '',
  role: localStorage.getItem('role') || ''
}

const userModule: Module<UserState, any> = {
  namespaced: true,
  state,
  mutations: {
    SET_USER(state: UserState, payload: LoginResponse) {
      state.token = payload.token
      state.userId = payload.userId
      state.nickname = payload.nickname
      state.role = payload.role
      localStorage.setItem('token', payload.token)
      localStorage.setItem('userId', String(payload.userId))
      localStorage.setItem('nickname', payload.nickname)
      localStorage.setItem('role', payload.role)
    },
    LOGOUT(state: UserState) {
      state.token = ''
      state.userId = 0
      state.nickname = ''
      state.role = ''
      localStorage.clear()
    }
  },
  actions: {
    async login({ commit }: any, payload: LoginParams) {
      try {
        console.log('发送登录请求到:', '/api/auth/login')
        console.log('请求数据:', payload)

        const response = await fetch('/api/auth/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify(payload)
        })

        console.log('响应状态:', response.status, response.statusText)

        // 检查响应是否成功
        if (!response.ok) {
          throw new Error(`HTTP 错误: ${response.status} ${response.statusText}`)
        }

        // 获取响应文本
        const responseText = await response.text()
        console.log('响应内容:', responseText)

        // 检查是否为空响应
        if (!responseText.trim()) {
          throw new Error('服务器返回空响应')
        }

        // 解析 JSON
        const data: ApiResponse<LoginResponse> = JSON.parse(responseText)
        console.log('解析后的数据:', data)

        if (data.code === 0 && data.data) {
          commit('SET_USER', data.data)
          return data.data
        } else {
          throw new Error(data.message || '登录失败')
        }
      } catch (error: any) {
        console.error('登录请求失败详情:', error)

        // 提供更具体的错误信息
        let errorMessage = '登录失败'

        if (error.message.includes('NetworkError') || error.message.includes('Failed to fetch')) {
          errorMessage = '无法连接到服务器，请检查：\n1. 后端服务是否启动\n2. 网络连接是否正常'
        } else if (error.message.includes('HTTP 错误')) {
          errorMessage = `服务器错误: ${error.message}`
        } else if (error.message.includes('空响应')) {
          errorMessage = '服务器返回空响应，可能原因：\n1. 后端接口未正确返回数据\n2. 跨域配置问题'
        } else if (error.message.includes('Unexpected token') || error.name === 'SyntaxError') {
          errorMessage = '服务器返回的不是有效的 JSON 数据'
        }

        throw new Error(errorMessage)
      }
    },
    logout({ commit }: any) {
      commit('LOGOUT')
    }
  }
}

export default userModule













