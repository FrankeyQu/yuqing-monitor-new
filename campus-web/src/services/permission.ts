import { apiGet, apiPost } from './http';
import type {
  CampusCurrentUser,
  CampusPermissionApi,
  CampusPermissionMenu,
  CampusPermissionRole,
  PageResult
} from '../types/api';

export interface RoleQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  roleType?: string;
  status?: number | '';
}

export interface ApiPermissionQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  moduleName?: string;
  status?: number | '';
}

export function getCurrentCampusUser() {
  return apiGet<CampusCurrentUser>('/campus/system/current-user');
}

export function getCampusMenuTree() {
  return apiGet<CampusPermissionMenu[]>('/campus/system/menu-tree');
}

export function listRoles(params: RoleQuery) {
  return apiGet<PageResult<CampusPermissionRole>>('/campus/system/role/list', params);
}

export function saveRole(data: CampusPermissionRole) {
  return apiPost<CampusPermissionRole>('/campus/system/role/save', data);
}

export function deleteRole(roleId: number) {
  return apiPost<void>('/campus/system/role/delete', undefined, { roleId });
}

export function listMenus() {
  return apiGet<CampusPermissionMenu[]>('/campus/system/menu/list');
}

export function listApis(params: ApiPermissionQuery) {
  return apiGet<PageResult<CampusPermissionApi>>('/campus/system/api/list', params);
}

export function listRoleMenuIds(roleId: number) {
  return apiGet<number[]>('/campus/system/role/menu-ids', { roleId });
}

export function listRoleApiIds(roleId: number) {
  return apiGet<number[]>('/campus/system/role/api-ids', { roleId });
}

export function assignRoleMenus(roleId: number, menuIds: number[]) {
  return apiPost<void>('/campus/system/role/assign-menus', menuIds, { roleId });
}

export function assignRoleApis(roleId: number, apiIds: number[]) {
  return apiPost<void>('/campus/system/role/assign-apis', apiIds, { roleId });
}
