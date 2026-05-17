import { http } from './http';
import type { MailConfig } from '../types/api';

interface LegacyResult<T> {
  status?: number;
  msg?: string;
  data: T;
}

function unwrapLegacy<T>(payload: LegacyResult<T> | string): T {
  if (typeof payload === 'string') {
    throw new Error('登录状态已失效');
  }
  if (payload.status !== undefined && payload.status !== 200) {
    throw new Error(payload.msg || '请求失败');
  }
  return payload.data;
}

export async function getMailConfig() {
  const response = await http.post<LegacyResult<MailConfig | null> | string>('/mail/getMailConfig');
  return unwrapLegacy<MailConfig | null>(response.data);
}

export async function saveMailConfig(data: MailConfig) {
  const response = await http.post<LegacyResult<void> | string>('/mail/saveMailConfig', data);
  return unwrapLegacy<void>(response.data);
}
