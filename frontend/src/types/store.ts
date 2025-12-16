// src/types/store.ts
export interface UserState {
  token: string;
  userId: number;
  nickname: string;
  role: string;
}

export interface RootState {
  user: UserState;
}













