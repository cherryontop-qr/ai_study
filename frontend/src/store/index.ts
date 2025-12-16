// src/store/index.ts
import { createStore } from 'vuex'
import userModule, { UserState } from './modules/user'

export interface RootState {
  user: UserState
}

const store = createStore({
  modules: {
    user: userModule
  }
})

export default store













