import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';
import { clearLoginState } from './auth';
import type { ApiResponse } from '../types/api';

export const http = axios.create({
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest'
  }
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 403) {
      clearLoginState();
      ElMessage.warning('登录状态已失效');
      router.replace('/login');
    }
    return Promise.reject(error);
  }
);

function unwrap<T>(payload: ApiResponse<T> | string): T {
  if (typeof payload === 'string') {
    throw new Error('登录状态已失效');
  }
  if (payload.code !== 200) {
    throw new Error(payload.msg || '请求失败');
  }
  return payload.data;
}

export async function apiGet<T>(url: string, params?: object) {
  const response = await http.get<ApiResponse<T> | string>(url, { params });
  return unwrap<T>(response.data);
}

export async function apiPost<T>(url: string, data?: unknown, params?: object) {
  const response = await http.post<ApiResponse<T> | string>(url, data, { params });
  return unwrap<T>(response.data);
}
