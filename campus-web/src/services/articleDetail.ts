import { apiGet } from './http';

export interface ArticleDetailData {
  id: number;
  title: string;
  content: string;
  sentiment: 'positive' | 'neutral' | 'negative';
  sourcePlatform: string;
  publishTime: string;
  discoverTime: string;
  matchedKeywords: string[];
  riskLevel: string;
  similarArticles: SimilarArticle[];
}

export interface SimilarArticle {
  id: number;
  title: string;
  sentiment: 'positive' | 'neutral' | 'negative';
  sourcePlatform: string;
  riskLevel: string;
}

export async function fetchArticleDetail(id: number): Promise<ArticleDetailData> {
  // 调用真实后端 API
  const data = await apiGet<Record<string, unknown>>('/campus/clue/detail', { clueId: id });

  // 处理后端返回的 keywords（逗号分隔字符串 -> 字符串数组）
  const keywords = data.keywords;
  const matchedKeywords: string[] = typeof keywords === 'string' && keywords.length > 0
    ? keywords.split(',').map((k) => k.trim()).filter(Boolean)
    : [];

  // 处理相似文章列表（后端可能不返回该字段）
  const rawSimilar = data.similarArticles;
  const similarArticles: SimilarArticle[] = Array.isArray(rawSimilar)
    ? rawSimilar.map((item: Record<string, unknown>) => ({
        id: Number(item.id ?? item.clueId ?? 0),
        title: String(item.title ?? item.clueTitle ?? ''),
        sentiment: (item.sentiment as SimilarArticle['sentiment']) || 'neutral',
        sourcePlatform: String(item.sourcePlatform ?? item.clueSource ?? ''),
        riskLevel: String(item.riskLevel ?? 'low'),
      }))
    : [];

  return {
    id: Number(data.clueId ?? data.id ?? id),
    title: String(data.clueTitle ?? data.title ?? ''),
    content: String(data.clueContent ?? data.content ?? ''),
    sentiment: (data.sentiment as ArticleDetailData['sentiment']) || 'neutral',
    sourcePlatform: String(data.sourcePlatform ?? ''),
    publishTime: String(data.publishTime ?? ''),
    discoverTime: String(data.discoverTime ?? data.createTime ?? ''),
    matchedKeywords,
    riskLevel: String(data.riskLevel ?? 'low'),
    similarArticles,
  };
}
