import { apiGet } from './http';
import type { ApiId, PageResult, CampusClue } from '../types/api';

export interface SearchResultItem {
  id: ApiId;
  title: string;
  summary: string;
  sentiment: 'positive' | 'neutral' | 'negative';
  sourcePlatform: string;
  publishTime: string;
  discoverTime: string;
  url: string;
}

export interface SearchResult {
  items: SearchResultItem[];
  total: number;
}

export async function searchClues(query: string, type: string, page: number, pageSize: number): Promise<SearchResult> {
  const data = await apiGet<PageResult<CampusClue>>('/campus/clue/list', {
    keyword: query,
    sourcePlatform: type === 'all' ? undefined : type,
    sortBy: 'publish_time',
    pageNum: page,
    pageSize
  });
  return {
    items: data.list.map(item => ({
      id: item.clueId!,
      title: item.clueTitle,
      summary: item.clueContent || '',
      sentiment: (item.sentiment || 'neutral') as SearchResultItem['sentiment'],
      sourcePlatform: item.sourcePlatform || '',
      publishTime: item.publishTime instanceof Date ? item.publishTime.toISOString() : (item.publishTime || ''),
      discoverTime: item.discoverTime instanceof Date ? item.discoverTime.toISOString() : (item.discoverTime || ''),
      url: ''
    })),
    total: data.total
  };
}
