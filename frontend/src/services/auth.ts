import api from './api';
import { setTokens, clearTokens, getAccessToken } from './api';

export interface RegisterRequest {
  email: string;
  phone: string;
  password: string;
  fullName: string;
}

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface UserInfo {
  id: string;
  email: string;
  phone: string;
  fullName: string;
  role: string;
  emailVerified: boolean;
  phoneVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export const authService = {
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<{ data: AuthResponse }>('/auth/register', data);
    setTokens(response.data.data.accessToken, response.data.data.refreshToken);
    return response.data.data;
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<{ data: AuthResponse }>('/auth/login', data);
    setTokens(response.data.data.accessToken, response.data.data.refreshToken);
    return response.data.data;
  },

  async refresh(): Promise<AuthResponse> {
    const response = await api.post<{ data: AuthResponse }>('/auth/refresh');
    setTokens(response.data.data.accessToken, response.data.data.refreshToken);
    return response.data.data;
  },

  async getMe(): Promise<UserInfo> {
    const response = await api.get<{ data: UserInfo }>('/auth/me');
    return response.data.data;
  },

  logout(): void {
    clearTokens();
  },

  isAuthenticated(): boolean {
    return !!getAccessToken();
  },
};