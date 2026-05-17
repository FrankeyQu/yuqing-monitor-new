import { apiGet } from './http';

export interface CompareData {
  radarData: { dimension: string; self: number; competitor: number }[];
  volumeTrend: { date: string; self: number; competitor: number }[];
  selfSentiment: { name: string; value: number }[];
  competitorSentiment: { name: string; value: number }[];
  selfMediaDistribution: { name: string; value: number }[];
  competitorMediaDistribution: { name: string; value: number }[];
}

export async function fetchCompareData(selfSubject: string, competitorSubject: string): Promise<CompareData> {
  return apiGet<CompareData>('/campus/compare/data', { selfSubject, competitorSubject });
}
