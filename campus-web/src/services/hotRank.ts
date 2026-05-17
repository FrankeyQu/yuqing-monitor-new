import { apiGet } from './http';

export interface HotRankItem {
  rank: number;
  title: string;
  hot: string;
}

export interface HotRankData {
  weibo: HotRankItem[];
  douyin: HotRankItem[];
  toutiao: HotRankItem[];
}

export async function fetchWeiboHotRank(): Promise<HotRankItem[]> {
  const data = await apiGet<HotRankData>('/campus/hot-rank/list');
  return data.weibo || [];
}

export async function fetchDouyinHotRank(): Promise<HotRankItem[]> {
  const data = await apiGet<HotRankData>('/campus/hot-rank/list');
  return data.douyin || [];
}

export async function fetchToutiaoHotRank(): Promise<HotRankItem[]> {
  const data = await apiGet<HotRankData>('/campus/hot-rank/list');
  return data.toutiao || [];
}
