import api from './api';

export interface SyncJob {
  id: string;
  status: 'RUNNING' | 'SUCCESS' | 'PARTIAL' | 'FAILED';
  fromOffset: number;
  requestedLimit: number;
  fetched: number;
  createdCount: number;
  updatedCount: number;
  failedCount: number;
  errorMessage?: string;
  startedAt: string;
  finishedAt?: string;
}

export interface SyncError {
  id: string;
  jobId: string;
  slug?: string;
  stage: string;
  message: string;
  createdAt: string;
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

export const adminService = {
  async ping(): Promise<string> {
    const response = await api.get<{ message: string }>('/admin/ping');
    return response.data.message;
  },

  async syncSchemes(limit?: number): Promise<SyncJob> {
    const response = await api.post<{ data: SyncJob }>('/admin/schemes/sync', null, {
      params: { limit },
    });
    return response.data.data;
  },

  async importSchemes(): Promise<SyncJob> {
    const response = await api.post<{ data: SyncJob }>('/admin/schemes/import');
    return response.data.data;
  },

  async syncJobs(page = 0, size = 20): Promise<Page<SyncJob>> {
    const response = await api.get<{ data: Page<SyncJob> }>('/admin/sync/jobs', {
      params: { page, size },
    });
    return response.data.data;
  },

  async syncErrors(jobId: string, page = 0, size = 20): Promise<Page<SyncError>> {
    const response = await api.get<{ data: Page<SyncError> }>('/admin/sync/errors', {
      params: { jobId, page, size },
    });
    return response.data.data;
  },
};
