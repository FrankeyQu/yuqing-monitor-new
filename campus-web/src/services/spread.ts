import { apiGet } from './http';

export interface SpreadData {
  source: { media: string; time: string; title: string };
  timeline: { time: string; count: number }[];
  mediaRanking: { name: string; articles: number }[];
  relationNodes: { id: number; name: string; category: number }[];
  relationLinks: { source: number; target: number }[];
}

export async function fetchSpreadData(eventId: number): Promise<SpreadData> {
  return apiGet<SpreadData>('/campus/spread/data', { eventId });
}
