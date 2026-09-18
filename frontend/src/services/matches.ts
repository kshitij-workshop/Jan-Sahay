import api from './api';

export type MatchStatus = 'ELIGIBLE' | 'NOT_ELIGIBLE' | 'INSUFFICIENT_INFORMATION';

export interface Match {
  schemeId: string;
  schemeName?: string;
  schemeSlug?: string;
  status: MatchStatus;
  matchReason?: string;
  missingInformation: string[];
  firstMatchedAt: string;
  lastCheckedAt: string;
}

export interface MatchSummary {
  total: number;
  eligible: number;
  insufficientInformation: number;
  notEligible: number;
}

export const matchService = {
  async list(status?: MatchStatus): Promise<Match[]> {
    const response = await api.get<{ data: Match[] }>('/matches', {
      params: status ? { status } : {},
    });
    return response.data.data;
  },

  async summary(): Promise<MatchSummary> {
    const response = await api.get<{ data: MatchSummary }>('/matches/summary');
    return response.data.data;
  },

  async recalculate(): Promise<MatchSummary> {
    const response = await api.post<{ data: MatchSummary }>('/matches/recalculate');
    return response.data.data;
  },
};
