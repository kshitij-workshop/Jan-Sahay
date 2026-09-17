import api from './api';

export interface AddressDto {
  id?: string;
  addressType?: 'PERMANENT' | 'CURRENT';
  primary?: boolean;
  state?: string;
  district?: string;
  block?: string;
  villageTown?: string;
  pincode?: string;
  areaType?: 'RURAL' | 'URBAN';
}

export interface ProfileCompleteness {
  overallPercent: number;
  sectionPercents: Record<string, number>;
  missingFields: string[];
}

export interface ProfileData {
  userId: string;
  fullName?: string;
  dateOfBirth?: string;
  gender?: string;
  nationality?: string;
  educationLevel?: string;
  institution?: string;
  studentStatus?: boolean;
  course?: string;
  annualIncome?: number;
  incomeCategory?: string;
  bplStatus?: boolean;
  economicDistress?: boolean;
  casteCategory?: string;
  minorityStatus?: boolean;
  disabilityStatus?: boolean;
  disabilityPercentage?: number;
  occupation?: string;
  employmentStatus?: string;
  govtEmployee?: boolean;
  maritalStatus?: string;
  dependents?: number;
  additionalAttributes?: Record<string, string>;
  addresses: AddressDto[];
  completeness: ProfileCompleteness;
}

export type ProfileUpdate = Partial<Omit<ProfileData, 'userId' | 'completeness' | 'addresses'>> & {
  addresses?: AddressDto[];
};

export const profileService = {
  async getProfile(): Promise<ProfileData> {
    const response = await api.get<{ data: ProfileData }>('/profile');
    return response.data.data;
  },

  async updateProfile(data: ProfileUpdate): Promise<ProfileData> {
    const response = await api.put<{ data: ProfileData }>('/profile', data);
    return response.data.data;
  },
};
