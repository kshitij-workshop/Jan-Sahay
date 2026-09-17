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

export const matchService = {
  async list(status?: MatchStatus): Promise<Match[]> {
    const response = await api.get<{ data: Match[] }>('/matches', {
      params: status ? { status } : {},
    });
    return response.data.data;
  },
};
