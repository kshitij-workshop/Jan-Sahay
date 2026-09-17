import { useState } from 'react';
import { User, MapPin, GraduationCap, Briefcase, Users, DollarSign, Shield, Save, Loader2 } from 'lucide-react';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Select } from '../components/common/Select';
import { useAuth } from '../context/AuthContext';

const genders = [
  { value: '', label: 'Select Gender' },
  { value: 'male', label: 'Male' },
  { value: 'female', label: 'Female' },
  { value: 'other', label: 'Other' },
];

const castes = [
  { value: '', label: 'Select Category' },
  { value: 'general', label: 'General' },
  { value: 'obc', label: 'OBC' },
  { value: 'sc', label: 'SC' },
  { value: 'st', label: 'ST' },
  { value: 'ebc', label: 'EBC' },
];

const incomeCategories = [
  { value: '', label: 'Select Income Category' },
  { value: 'bpl', label: 'Below Poverty Line (BPL)' },
  { value: 'apl', label: 'Above Poverty Line (APL)' },
  { value: 'antyodaya', label: 'Antyodaya Anna Yojana' },
];

const educationLevels = [
  { value: '', label: 'Select Education Level' },
  { value: 'illiterate', label: 'Illiterate' },
  { value: 'primary', label: 'Primary' },
  { value: 'secondary', label: 'Secondary' },
  { value: 'higher_secondary', label: 'Higher Secondary' },
  { value: 'graduate', label: 'Graduate' },
  { value: 'post_graduate', label: 'Post Graduate' },
  { value: 'diploma', label: 'Diploma' },
  { value: 'phd', label: 'PhD' },
];

const occupations = [
  { value: '', label: 'Select Occupation' },
  { value: 'farmer', label: 'Farmer' },
  { value: 'student', label: 'Student' },
  { value: 'govt_employee', label: 'Government Employee' },
  { value: 'private_employee', label: 'Private Employee' },
  { value: 'self_employed', label: 'Self Employed' },
  { value: 'unemployed', label: 'Unemployed' },
  { value: 'homemaker', label: 'Homemaker' },
  { value: 'retired', label: 'Retired' },
];

const states = [
  { value: '', label: 'Select State' },
  { value: 'bihar', label: 'Bihar' },
  { value: 'up', label: 'Uttar Pradesh' },
  { value: 'jharkhand', label: 'Jharkhand' },
  { value: 'wb', label: 'West Bengal' },
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

const initialFormData = {
  fullName: '',
  dateOfBirth: '',
  gender: '',
  nationality: 'Indian',
  state: '',
  district: '',
  block: '',
  village: '',
  pincode: '',
  ruralUrban: 'rural',
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
  maritalStatus: 'unmarried',
  dependents: '',
};

export default function Profile() {
  const { user, refreshUser } = useAuth();
  const [activeSection, setActiveSection] = useState('personal');
  const [formData, setFormData] = useState(initialFormData);
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const completeness = calculateCompleteness(formData);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    setFormData({ ...formData, [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value });
    if (saved) setSaved(false);
  };

  const handleSave = async () => {
    setSaving(true);
    await new Promise(r => setTimeout(r, 1000));
    setSaving(false);
    setSaved(true);
    refreshUser();
  };

  function calculateCompleteness(data: typeof formData): number {
    const fields = Object.values(data).filter(v => v !== '' && v !== false && v !== null);
    return Math.round((fields.length / Object.keys(data).length) * 100);
  }

  function renderSection(section: string, formData: typeof initialFormData, setFormData: React.Dispatch<React.SetStateAction<typeof initialFormData>>, handleChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => void) {
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
              <Select label="State" name="state" value={formData.state} onChange={handleChange} options={states} required />
              <Input label="District" name="district" value={formData.district} onChange={handleChange} required />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input label="Block" name="block" value={formData.block} onChange={handleChange} />
              <Input label="Village/Town" name="village" value={formData.village} onChange={handleChange} required />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input label="Pincode" name="pincode" type="text" value={formData.pincode} onChange={handleChange} maxLength={6} />
              <Select label="Area Type" name="ruralUrban" value={formData.ruralUrban} onChange={handleChange} options={[
                { value: 'rural', label: 'Rural' },
                { value: 'urban', label: 'Urban' },
              ]} />
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
            <Input label="Annual Income (₹)" name="annualIncome" type="number" value={formData.annualIncome} onChange={handleChange} placeholder="e.g., 150000" />
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
            <Select label="Employment Status" name="employmentStatus" value={formData.employmentStatus} onChange={handleChange} options={[
              { value: '', label: 'Select Status' },
              { value: 'employed', label: 'Employed' },
              { value: 'unemployed', label: 'Unemployed' },
              { value: 'self_employed', label: 'Self Employed' },
            ]} />
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
            <Select label="Marital Status" name="maritalStatus" value={formData.maritalStatus} onChange={handleChange} options={[
              { value: '', label: 'Select Status' },
              { value: 'unmarried', label: 'Unmarried' },
              { value: 'married', label: 'Married' },
              { value: 'widowed', label: 'Widowed' },
              { value: 'divorced', label: 'Divorced' },
            ]} />
            <Input label="Number of Dependents" name="dependents" type="number" value={formData.dependents} onChange={handleChange} min={0} />
          </div>
        );
      default:
        return <div>Select a section</div>;
    }
  }

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
                style={{ width: `${completeness}%` }}
              />
            </div>
            <span className="font-medium">{completeness}%</span>
          </div>
          <Button onClick={handleSave} loading={saving} disabled={saving}>
            {saving ? <Loader2 className="h-4 w-4" /> : <Save className="h-4 w-4" />}
            {saved ? 'Saved!' : 'Save Changes'}
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <aside className="lg:col-span-1">
          <Card padding="md">
            <CardHeader title="Profile Sections" />
            <CardContent>
              <nav className="space-y-1" aria-label="Profile sections">
                {sections.map((section) => {
                  const Icon = section.icon;
                  const isActive = activeSection === section.id;
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
                      {section.title}
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
            <CardHeader title={sections.find(s => s.id === activeSection)?.title} />
            <CardContent>
              {renderSection(activeSection, formData, setFormData, handleChange)}
            </CardContent>
          </Card>
        </main>
      </div>
    </div>
  );
}

function calculateCompleteness(data: typeof initialFormData): number {
  const fields = Object.values(data).filter(v => v !== '' && v !== false && v !== null);
  return Math.round((fields.length / Object.keys(data).length) * 100);
}