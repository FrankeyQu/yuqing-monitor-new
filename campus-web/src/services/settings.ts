import { apiGet, apiPost } from './http';
import type {
  CampusAuditLog,
  CampusDepartment,
  CampusDictItem,
  CampusDictType,
  PageResult
} from '../types/api';

export interface DepartmentQuery {
  pageNum: number;
  pageSize: number;
  departmentName?: string;
  parentId?: number;
  status?: number;
}

export interface DictTypeQuery {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  status?: number;
}

export interface DictItemQuery {
  pageNum: number;
  pageSize: number;
  dictType?: string;
  keyword?: string;
  status?: number;
}

export interface AuditQuery {
  pageNum: number;
  pageSize: number;
  moduleName?: string;
  operationType?: string;
  objectType?: string;
  objectId?: string;
  operatorName?: string;
}

export function listDepartments(params: DepartmentQuery) {
  return apiGet<PageResult<CampusDepartment>>('/campus/department/list', params);
}

export function saveDepartment(data: CampusDepartment) {
  return apiPost<CampusDepartment>('/campus/department/save', data);
}

export function deleteDepartment(departmentId: number) {
  return apiPost<void>('/campus/department/delete', undefined, { departmentId });
}

export function listDictTypes(params: DictTypeQuery) {
  return apiGet<PageResult<CampusDictType>>('/campus/dict/type/list', params);
}

export function saveDictType(data: CampusDictType) {
  return apiPost<CampusDictType>('/campus/dict/type/save', data);
}

export function deleteDictType(dictType: string) {
  return apiPost<void>('/campus/dict/type/delete', undefined, { dictType });
}

export function listDictItems(params: DictItemQuery) {
  return apiGet<PageResult<CampusDictItem>>('/campus/dict/item/list', params);
}

export function saveDictItem(data: CampusDictItem) {
  return apiPost<CampusDictItem>('/campus/dict/item/save', data);
}

export function deleteDictItem(dictType: string, itemCode: string) {
  return apiPost<void>('/campus/dict/item/delete', undefined, { dictType, itemCode });
}

export function listAuditLogs(params: AuditQuery) {
  return apiGet<PageResult<CampusAuditLog>>('/campus/audit/list', params);
}
