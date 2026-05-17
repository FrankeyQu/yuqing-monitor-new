import axios from 'axios';

const LOGIN_FLAG = 'zhuoran_campus_logged_in';
const LOGIN_NAME = 'zhuoran_campus_login_name';

export interface LoginPayload {
  telephone: string;
  password: string;
}

export interface LegacyLoginResponse {
  code: number;
  msg: string;
}

export interface LegacyStatusResponse {
  status: number;
  msg: string;
  data?: unknown;
}

export interface PasswordPayload {
  newPassword: string;
  confirmPassword: string;
}

export function isLoggedIn() {
  return localStorage.getItem(LOGIN_FLAG) === '1';
}

export function markLoggedIn(telephone: string) {
  localStorage.setItem(LOGIN_FLAG, '1');
  localStorage.setItem(LOGIN_NAME, telephone);
}

export function clearLoginState() {
  localStorage.removeItem(LOGIN_FLAG);
  localStorage.removeItem(LOGIN_NAME);
}

export function currentLoginName() {
  return localStorage.getItem(LOGIN_NAME) || '校园值班账号';
}

export async function login(payload: LoginPayload) {
  const form = new URLSearchParams();
  form.set('telephone', payload.telephone);
  form.set('password', payload.password);

  const response = await axios.post<LegacyLoginResponse>('/login', form, {
    withCredentials: true,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-Requested-With': 'XMLHttpRequest'
    }
  });

  if (response.data.code !== 1) {
    throw new Error(response.data.msg || '登录失败');
  }

  markLoggedIn(payload.telephone);
  return response.data;
}

export async function setNewPassword(payload: PasswordPayload) {
  const form = new URLSearchParams();
  form.set('newPassword', payload.newPassword);
  form.set('confirmPassword', payload.confirmPassword);

  const response = await axios.post<LegacyStatusResponse>('/user/setPassword', form, {
    withCredentials: true,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-Requested-With': 'XMLHttpRequest'
    }
  });

  if (response.data.status !== 200) {
    throw new Error(response.data.msg || '设置失败');
  }

  return response.data;
}
