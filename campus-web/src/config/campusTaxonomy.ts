export type CampusRiskCode = 'normal' | 'concern' | 'major' | 'urgent';
export type CampusRiskTagType = 'danger' | 'warning' | 'primary' | 'success' | 'info' | '';

export const CAMPUS_RISK_OPTIONS: Array<{
  value: CampusRiskCode;
  label: string;
  tagType: CampusRiskTagType;
}> = [
  { value: 'normal', label: '普通关注', tagType: 'info' },
  { value: 'concern', label: '一般预警', tagType: 'warning' },
  { value: 'major', label: '重大预警', tagType: 'danger' },
  { value: 'urgent', label: '特别重大', tagType: 'danger' }
];

const RISK_LABELS = CAMPUS_RISK_OPTIONS.reduce<Record<string, string>>((acc, item) => {
  acc[item.value] = item.label;
  return acc;
}, {});

const RISK_TAG_TYPES = CAMPUS_RISK_OPTIONS.reduce<Record<string, CampusRiskTagType>>((acc, item) => {
  acc[item.value] = item.tagType;
  return acc;
}, {});

const LEGACY_RISK_VALUES: Record<string, CampusRiskCode> = {
  一般: 'normal',
  普通: 'normal',
  一般关注: 'normal',
  普通关注: 'normal',
  关注: 'concern',
  一般预警: 'concern',
  higher: 'major',
  较大: 'major',
  较大风险: 'major',
  重大: 'major',
  重大风险: 'major',
  重大预警: 'major',
  紧急: 'urgent',
  紧急事件: 'urgent',
  特别重大: 'urgent'
};

export function normalizeCampusRiskLevel(value?: string | null): CampusRiskCode | '' {
  if (!value) {
    return '';
  }
  const trimmed = String(value).trim();
  if (trimmed in RISK_LABELS) {
    return trimmed as CampusRiskCode;
  }
  return LEGACY_RISK_VALUES[trimmed] || '';
}

export function campusRiskLabel(value?: string | null, fallback = '普通关注'): string {
  const normalized = normalizeCampusRiskLevel(value);
  if (normalized) {
    return RISK_LABELS[normalized];
  }
  return value || fallback;
}

export function campusRiskTagType(value?: string | null): CampusRiskTagType {
  const normalized = normalizeCampusRiskLevel(value);
  if (normalized) {
    return RISK_TAG_TYPES[normalized];
  }
  return 'info';
}

export const CAMPUS_TOPIC_LABELS: Record<string, string> = {
  food_safety: '食品安全',
  dormitory: '宿舍管理',
  campus_safety: '校园安全',
  bullying_conflict: '欺凌冲突',
  teacher_ethics: '师德师风',
  fee_dispute: '收费争议',
  admission_employment: '招生就业',
  exam_teaching: '考试教学',
  logistics_service: '后勤服务',
  public_incident: '公共事件',
  rumor: '谣言不实信息',
  other: '其他'
};

export function campusTopicLabel(value?: string | null): string {
  if (!value) {
    return '-';
  }
  return CAMPUS_TOPIC_LABELS[value] || value;
}
