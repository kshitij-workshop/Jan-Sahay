import api from './api';

export interface SchemeSummary {
  id: string;
  slug: string;
  name?: string;
  nameEng?: string;
  shortTitle?: string;
  category?: string;
  state?: string;
  level?: string;
  benefitType?: string;
  description?: string;
}

export interface SchemeDetail {
  id: string;
  slug: string;
  name?: string;
  nameEng?: string;
  shortTitle?: string;
  category?: string;
  categories: { category: string; kind: string }[];
  state?: string;
  states: string[];
  level?: string;
  schemeFor?: string;
  beneficiaries: string[];
  ministry?: string;
  department?: string;
  description?: string;
  detailedDescription?: string;
  benefits?: string;
  eligibility?: string;
  exclusions?: string;
  documentsText?: string;
  benefitType?: string;
  schemeType?: string;
  tags: string[];
  faqs: { question: string; answer?: string }[];
  documents: { name: string; required?: boolean }[];
  applicationProcess: { stepNo: number; description: string }[];
  references: { title: string; url: string }[];
  source?: string;
  sourceUrl?: string;
  lastSyncedAt?: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface SchemeFilters {
  q?: string;
  category?: string;
  state?: string;
  level?: string;
  page?: number;
  size?: number;
}

export const schemeService = {
  async list(filters: SchemeFilters = {}): Promise<Page<SchemeSummary>> {
    const params: Record<string, string | number> = { page: filters.page ?? 0, size: filters.size ?? 12 };
    if (filters.q?.trim()) params.q = filters.q.trim();
    if (filters.category) params.category = filters.category;
    if (filters.state) params.state = filters.state;
    if (filters.level) params.level = filters.level;
    const response = await api.get<{ data: Page<SchemeSummary> }>('/schemes', { params });
    return response.data.data;
  },

  async getById(id: string): Promise<SchemeDetail> {
    const response = await api.get<{ data: SchemeDetail }>(`/schemes/${id}`);
    return response.data.data;
  },

  async categories(): Promise<string[]> {
    const response = await api.get<{ data: string[] }>('/schemes/categories');
    return response.data.data;
  },
};
