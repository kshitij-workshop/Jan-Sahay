import { useEffect, useState } from 'react';
import { User, MapPin, GraduationCap, Briefcase, Users, DollarSign, Shield, Save, Loader2, AlertTriangle } from 'lucide-react';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Select } from '../components/common/Select';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { useAuth } from '../context/AuthContext';
import { profileService, ProfileData, AddressDto } from '../services/profile';

const genders = [
  { value: '', label: 'Select Gender' },
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
];

const castes = [
  { value: '', label: 'Select Category' },
  { value: 'GENERAL', label: 'General' },
  { value: 'OBC', label: 'OBC' },
  { value: 'EBC', label: 'EBC' },
  { value: 'SC', label: 'SC' },
  { value: 'ST', label: 'ST' },
];

const incomeCategories = [
  { value: '', label: 'Select Income Category' },
  { value: 'BPL', label: 'Below Poverty Line (BPL)' },
  { value: 'APL', label: 'Above Poverty Line (APL)' },
  { value: 'ANTYODAYA', label: 'Antyodaya Anna Yojana' },
  { value: 'NONE', label: 'None of these' },
];

const educationLevels = [
  { value: '', label: 'Select Education Level' },
  { value: 'ILLITERATE', label: 'Illiterate' },
  { value: 'PRIMARY', label: 'Primary' },
  { value: 'SECONDARY', label: 'Secondary' },
  { value: 'HIGHER_SECONDARY', label: 'Higher Secondary' },
  { value: 'GRADUATE', label: 'Graduate' },
  { value: 'POST_GRADUATE', label: 'Post Graduate' },
  { value: 'DIPLOMA', label: 'Diploma' },
  { value: 'PHD', label: 'PhD' },
];

const occupations = [
  { value: '', label: 'Select Occupation' },
  { value: 'FARMER', label: 'Farmer' },
  { value: 'STUDENT', label: 'Student' },
  { value: 'GOVT_EMPLOYEE', label: 'Government Employee' },
  { value: 'PRIVATE_EMPLOYEE', label: 'Private Employee' },
  { value: 'SELF_EMPLOYED', label: 'Self Employed' },
  { value: 'UNEMPLOYED', label: 'Unemployed' },
  { value: 'HOMEMAKER', label: 'Homemaker' },
  { value: 'RETIRED', label: 'Retired' },
  { value: 'WEAVER', label: 'Weaver' },
  { value: 'ARTISAN', label: 'Artisan' },
  { value: 'FISHER', label: 'Fisher' },
];

const employmentStatuses = [
  { value: '', label: 'Select Status' },
  { value: 'EMPLOYED', label: 'Employed' },
  { value: 'UNEMPLOYED', label: 'Unemployed' },
  { value: 'SELF_EMPLOYED', label: 'Self Employed' },
];

const maritalStatuses = [
  { value: '', label: 'Select Status' },
  { value: 'UNMARRIED', label: 'Unmarried' },
  { value: 'MARRIED', label: 'Married' },
  { value: 'WIDOWED', label: 'Widowed' },
  { value: 'DIVORCED', label: 'Divorced' },
];

const states = [
  { value: '', label: 'Select State' },
  { value: 'Bihar', label: 'Bihar' },
  { value: 'Uttar Pradesh', label: 'Uttar Pradesh' },
  { value: 'Jharkhand', label: 'Jharkhand' },
  { value: 'West Bengal', label: 'West Bengal' },
];

const areaTypes = [
  { value: '', label: 'Select Area Type' },
  { value: 'RURAL', label: 'Rural' },
  { value: 'URBAN', label: 'Urban' },
];

const sections = [
  { id: 'personal', title: 'Personal Information', icon: User },
  { id: 'address', title: 'Address', icon: MapPin },
  { id: 'education', title: 'Education', icon: GraduationCap },
  { id: 'economic', title: 'Economic', icon: DollarSign },
  { id: 'social', title: 'Social Category', icon: Shield },
  { id: 'employment', title: 'Employment', icon: Briefcase },
  { id: 'family', title: 'Family', icon: Users },
];

interface FormState {
  fullName: string;
  dateOfBirth: string;
  gender: string;
  nationality: string;
  educationLevel: string;
  institution: string;
  studentStatus: boolean;
  course: string;
  annualIncome: string;
  incomeCategory: string;
  bplStatus: boolean;
  economicDistress: boolean;
  casteCategory: string;
  minorityStatus: boolean;
  disabilityStatus: boolean;
  disabilityPercentage: string;
  occupation: string;
  employmentStatus: string;
  govtEmployee: boolean;
  maritalStatus: string;
  dependents: string;
  address: AddressDto;
}

const emptyAddress: AddressDto = { primary: true, addressType: 'PERMANENT' };

const emptyForm: FormState = {
  fullName: '',
  dateOfBirth: '',
  gender: '',
  nationality: 'Indian',
  educationLevel: '',
  institution: '',
  studentStatus: false,
  course: '',
  annualIncome: '',
  incomeCategory: '',
  bplStatus: false,
  economicDistress: false,
  casteCategory: '',
  minorityStatus: false,
  disabilityStatus: false,
  disabilityPercentage: '',
  occupation: '',
  employmentStatus: '',
  govtEmployee: false,
  maritalStatus: '',
  dependents: '',
  address: { ...emptyAddress },
};

function toForm(profile: ProfileData): { form: FormState; otherAddresses: AddressDto[] } {
  const str = (v: unknown) => (v === null || v === undefined ? '' : String(v));
  const primary =
    profile.addresses.find((a) => a.primary) ?? profile.addresses[0] ?? { ...emptyAddress };
  const others = profile.addresses.filter((a) => a !== primary);
  return {
    form: {
      fullName: str(profile.fullName),
      dateOfBirth: str(profile.dateOfBirth),
      gender: str(profile.gender),
      nationality: str(profile.nationality) || 'Indian',
      educationLevel: str(profile.educationLevel),
      institution: str(profile.institution),
      studentStatus: profile.studentStatus ?? false,
      course: str(profile.course),
      annualIncome: str(profile.annualIncome),
      incomeCategory: str(profile.incomeCategory),
      bplStatus: profile.bplStatus ?? false,
      economicDistress: profile.economicDistress ?? false,
      casteCategory: str(profile.casteCategory),
      minorityStatus: profile.minorityStatus ?? false,
      disabilityStatus: profile.disabilityStatus ?? false,
      disabilityPercentage: str(profile.disabilityPercentage),
      occupation: str(profile.occupation),
      employmentStatus: str(profile.employmentStatus),
      govtEmployee: profile.govtEmployee ?? false,
      maritalStatus: str(profile.maritalStatus),
      dependents: str(profile.dependents),
      address: {
        id: primary.id,
        addressType: primary.addressType ?? 'PERMANENT',
        primary: true,
        state: str(primary.state),
        district: str(primary.district),
        block: str(primary.block),
        villageTown: str(primary.villageTown),
        pincode: str(primary.pincode),
        areaType: primary.areaType,
      },
    },
    otherAddresses: others,
  };
}

const opt = (v: string) => (v.trim() === '' ? undefined : v.trim());
const num = (v: string) => (v.trim() === '' ? undefined : Number(v.trim()));

export default function Profile() {
  const { user } = useAuth();
  const [activeSection, setActiveSection] = useState('personal');
  const [formData, setFormData] = useState<FormState>(emptyForm);
  const [otherAddresses, setOtherAddresses] = useState<AddressDto[]>([]);
  const [completeness, setCompleteness] = useState<ProfileData['completeness'] | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [saveError, setSaveError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      try {
        const profile = await profileService.getProfile();
        if (!cancelled) {
          const { form, otherAddresses } = toForm(profile);
          setFormData(form);
          setOtherAddresses(otherAddresses);
          setCompleteness(profile.completeness);
        }
      } catch {
        if (!cancelled) {
          setLoadError('Could not load your profile. Please try again.');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    if (name.startsWith('address.')) {
      const key = name.slice('address.'.length) as keyof AddressDto;
      setFormData({ ...formData, address: { ...formData.address, [key]: value } });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
      });
    }
    if (saved) setSaved(false);
    if (saveError) setSaveError('');
  };

  const handleSave = async () => {
    setSaving(true);
    setSaveError('');
    try {
      const updated = await profileService.updateProfile({
        fullName: opt(formData.fullName),
        dateOfBirth: opt(formData.dateOfBirth),
        gender: opt(formData.gender),
        nationality: opt(formData.nationality),
        educationLevel: opt(formData.educationLevel) || undefined,
        institution: opt(formData.institution),
        studentStatus: formData.studentStatus,
        course: opt(formData.course),
        annualIncome: num(formData.annualIncome),
        incomeCategory: opt(formData.incomeCategory) || undefined,
        bplStatus: formData.bplStatus,
        economicDistress: formData.economicDistress,
        casteCategory: opt(formData.casteCategory) || undefined,
        minorityStatus: formData.minorityStatus,
        disabilityStatus: formData.disabilityStatus,
        disabilityPercentage: num(formData.disabilityPercentage),
        occupation: opt(formData.occupation) || undefined,
        employmentStatus: opt(formData.employmentStatus) || undefined,
        govtEmployee: formData.govtEmployee,
        maritalStatus: opt(formData.maritalStatus) || undefined,
        dependents: num(formData.dependents),
        addresses: [
          {
            ...formData.address,
            state: opt(formData.address.state ?? ''),
            district: opt(formData.address.district ?? ''),
            block: opt(formData.address.block ?? ''),
            villageTown: opt(formData.address.villageTown ?? ''),
            pincode: opt(formData.address.pincode ?? ''),
            primary: true,
          },
          ...otherAddresses,
        ],
      });
      const reloaded = toForm(updated);
      setFormData(reloaded.form);
      setOtherAddresses(reloaded.otherAddresses);
      setCompleteness(updated.completeness);
      setSaved(true);
    } catch (err: any) {
      const details = err.response?.data?.details;
      setSaveError(
        details
          ? Object.entries(details)
              .map(([k, v]) => `${k}: ${v}`)
              .join(' • ')
          : err.response?.data?.message || 'Could not save your profile. Please try again.'
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[40vh] flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm flex gap-2" role="alert">
        <AlertTriangle className="h-5 w-5 flex-shrink-0" />
        <span>{loadError}</span>
      </div>
    );
  }

  const overall = completeness?.overallPercent ?? 0;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Profile</h1>
          <p className="text-gray-500 mt-1">Manage your personal information for better scheme matching</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="hidden sm:flex items-center gap-2 text-sm text-gray-600">
            <span>Profile Completeness</span>
            <div className="w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                className="h-full bg-primary-600 rounded-full transition-all"
                style={{ width: `${overall}%` }}
              />
            </div>
            <span className="font-medium">{overall}%</span>
          </div>
          <Button onClick={handleSave} loading={saving} disabled={saving}>
            {saving ? <Loader2 className="h-4 w-4" /> : <Save className="h-4 w-4" />}
            {saved ? 'Saved!' : 'Save Changes'}
          </Button>
        </div>
      </div>

      {saveError && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm" role="alert">
          {saveError}
        </div>
      )}

      {completeness && completeness.missingFields.length > 0 && (
        <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-sm text-yellow-800">
          <span className="font-medium">Still missing: </span>
          {completeness.missingFields.join(', ')}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <aside className="lg:col-span-1">
          <Card padding="md">
            <CardHeader title="Profile Sections" />
            <CardContent>
              <nav className="space-y-1" aria-label="Profile sections">
                {sections.map((section) => {
                  const Icon = section.icon;
                  const isActive = activeSection === section.id;
                  const sectionScore = completeness?.sectionPercents?.[section.id];
                  return (
                    <button
                      key={section.id}
                      onClick={() => setActiveSection(section.id)}
                      className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                        isActive
                          ? 'bg-primary-50 text-primary-700'
                          : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                      }`}
                      aria-current={isActive ? 'page' : undefined}
                    >
                      <Icon className={`h-5 w-5 ${isActive ? 'text-primary-600' : 'text-gray-400'}`} />
                      <span className="flex-1 text-left">{section.title}</span>
                      {sectionScore !== undefined && (
                        <span className="text-xs text-gray-400">{sectionScore}%</span>
                      )}
                    </button>
                  );
                })}
              </nav>
            </CardContent>
          </Card>

          <Card padding="md" className="mt-4">
            <CardHeader title="Account Info" />
            <CardContent className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Email</span>
                <span className="font-medium text-gray-900">{user?.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Phone</span>
                <span className="font-medium text-gray-900">{user?.phone}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Role</span>
                <span className="font-medium text-gray-900 capitalize">{user?.role?.toLowerCase()}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Email Verified</span>
                <span className={user?.emailVerified ? 'text-green-600' : 'text-yellow-600'}>
                  {user?.emailVerified ? 'Yes' : 'No'}
                </span>
              </div>
            </CardContent>
          </Card>
        </aside>

        <main className="lg:col-span-3">
          <Card padding="md">
            <CardHeader title={sections.find((s) => s.id === activeSection)?.title} />
            <CardContent>{renderSection(activeSection, formData, handleChange)}</CardContent>
          </Card>
        </main>
      </div>
    </div>
  );
}

function renderSection(
  section: string,
  formData: {
    fullName: string;
    dateOfBirth: string;
    gender: string;
    nationality: string;
    educationLevel: string;
    institution: string;
    studentStatus: boolean;
    course: string;
    annualIncome: string;
    incomeCategory: string;
    bplStatus: boolean;
    economicDistress: boolean;
    casteCategory: string;
    minorityStatus: boolean;
    disabilityStatus: boolean;
    disabilityPercentage: string;
    occupation: string;
    employmentStatus: string;
    govtEmployee: boolean;
    maritalStatus: string;
    dependents: string;
    address: AddressDto;
  },
  handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => void
) {
  switch (section) {
    case 'personal':
      return (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Input label="Full Name" name="fullName" value={formData.fullName} onChange={handleChange} required />
          <Input label="Date of Birth" name="dateOfBirth" type="date" value={formData.dateOfBirth} onChange={handleChange} />
          <Select label="Gender" name="gender" value={formData.gender} onChange={handleChange} options={genders} required />
          <Input label="Nationality" name="nationality" value={formData.nationality} onChange={handleChange} />
        </div>
      );
    case 'address':
      return (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Select label="State" name="address.state" value={formData.address.state ?? ''} onChange={handleChange} options={states} required />
            <Input label="District" name="address.district" value={formData.address.district ?? ''} onChange={handleChange} required />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input label="Block" name="address.block" value={formData.address.block ?? ''} onChange={handleChange} />
            <Input label="Village/Town" name="address.villageTown" value={formData.address.villageTown ?? ''} onChange={handleChange} required />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input label="Pincode" name="address.pincode" type="text" value={formData.address.pincode ?? ''} onChange={handleChange} maxLength={6} />
            <Select label="Area Type" name="address.areaType" value={formData.address.areaType ?? ''} onChange={handleChange} options={areaTypes} />
          </div>
        </div>
      );
    case 'education':
      return (
        <div className="space-y-4">
          <Select label="Education Level" name="educationLevel" value={formData.educationLevel} onChange={handleChange} options={educationLevels} />
          <Input label="Institution" name="institution" value={formData.institution} onChange={handleChange} />
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="studentStatus"
                checked={formData.studentStatus}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">Currently a student</span>
            </label>
          </div>
          <Input label="Course" name="course" value={formData.course} onChange={handleChange} />
        </div>
      );
    case 'economic':
      return (
        <div className="space-y-4">
          <Input label="Annual Income (₹)" name="annualIncome" type="number" value={formData.annualIncome} onChange={handleChange} placeholder="e.g., 150000" min={0} />
          <Select label="Income Category" name="incomeCategory" value={formData.incomeCategory} onChange={handleChange} options={incomeCategories} />
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="bplStatus"
                checked={formData.bplStatus}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">BPL Card Holder</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="economicDistress"
                checked={formData.economicDistress}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">Economic Distress</span>
            </label>
          </div>
        </div>
      );
    case 'social':
      return (
        <div className="space-y-4">
          <Select label="Caste/Category" name="casteCategory" value={formData.casteCategory} onChange={handleChange} options={castes} required />
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="minorityStatus"
                checked={formData.minorityStatus}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">Minority Community</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="disabilityStatus"
                checked={formData.disabilityStatus}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">Disability</span>
            </label>
          </div>
          <Input label="Disability Percentage (%)" name="disabilityPercentage" type="number" value={formData.disabilityPercentage} onChange={handleChange} min={0} max={100} />
        </div>
      );
    case 'employment':
      return (
        <div className="space-y-4">
          <Select label="Occupation" name="occupation" value={formData.occupation} onChange={handleChange} options={occupations} required />
          <Select label="Employment Status" name="employmentStatus" value={formData.employmentStatus} onChange={handleChange} options={employmentStatuses} />
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                name="govtEmployee"
                checked={formData.govtEmployee}
                onChange={handleChange}
                className="w-4 h-4 text-primary-600 border-gray-300 rounded focus:ring-primary-500"
              />
              <span className="text-sm text-gray-700">Government Employee</span>
            </label>
          </div>
        </div>
      );
    case 'family':
      return (
        <div className="space-y-4">
          <Select label="Marital Status" name="maritalStatus" value={formData.maritalStatus} onChange={handleChange} options={maritalStatuses} />
          <Input label="Number of Dependents" name="dependents" type="number" value={formData.dependents} onChange={handleChange} min={0} />
        </div>
      );
    default:
      return <div>Select a section</div>;
  }
}
