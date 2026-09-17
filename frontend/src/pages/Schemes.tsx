import { useState } from 'react';
import { Search, Filter, ChevronDown } from 'lucide-react';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Input } from '../components/common/Input';
import { Select } from '../components/common/Select';
import { Button } from '../components/common/Button';

const categories = [
  { value: '', label: 'All Categories' },
  { value: 'agriculture', label: 'Agriculture' },
  { value: 'education', label: 'Education' },
  { value: 'health', label: 'Health' },
  { value: 'employment', label: 'Employment' },
  { value: 'social_welfare', label: 'Social Welfare' },
  { value: 'women', label: 'Women Empowerment' },
];

const states = [
  { value: '', label: 'All States' },
  { value: 'bihar', label: 'Bihar' },
  { value: 'up', label: 'Uttar Pradesh' },
  { value: 'mp', label: 'Madhya Pradesh' },
];

const schemes = [
  {
    id: '1',
    name: 'PM Kisan Samman Nidhi',
    shortTitle: 'PM-KISAN',
    description: 'Income support of ₹6,000 per year to farmer families',
    category: 'Agriculture',
    state: 'All India',
    benefit: '₹6,000/year',
    eligibility: 'Small and marginal farmers',
    status: 'eligible' as const,
  },
  {
    id: '2',
    name: 'Bihar Student Credit Card Scheme',
    shortTitle: 'BSCC',
    description: 'Education loan up to ₹4 lakh for higher education',
    category: 'Education',
    state: 'Bihar',
    benefit: 'Up to ₹4 lakh',
    eligibility: 'Bihar resident students',
    status: 'needs_info' as const,
  },
  {
    id: '3',
    name: 'Mukhyamantri Kanya Utthan Yojana',
    shortTitle: 'MKUY',
    description: 'Financial assistance for girl child education',
    category: 'Women Empowerment',
    state: 'Bihar',
    benefit: '₹25,000',
    eligibility: 'Girl students in Bihar',
    status: 'eligible' as const,
  },
  {
    id: '4',
    name: 'Mahatma Gandhi NREGA',
    shortTitle: 'MGNREGA',
    description: '100 days guaranteed wage employment',
    category: 'Employment',
    state: 'All India',
    benefit: '₹200-300/day',
    eligibility: 'Rural households',
    status: 'not_eligible' as const,
  },
];

export default function Schemes() {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [state, setState] = useState('');

  const filteredSchemes = schemes.filter((s) => {
    const matchesSearch = s.name.toLowerCase().includes(search.toLowerCase()) ||
      s.description.toLowerCase().includes(search.toLowerCase());
    const matchesCategory = !category || s.category.toLowerCase() === category;
    const matchesState = !state || s.state.toLowerCase() === state || s.state === 'All India';
    return matchesSearch && matchesCategory && matchesState;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Government Schemes</h1>
          <p className="text-gray-500 mt-1">Discover schemes you may be eligible for</p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="gap-2">
            <Filter className="h-4 w-4" />
            Filters
          </Button>
        </div>
      </div>

      <Card padding="md">
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="md:col-span-2">
              <Input
                placeholder="Search schemes by name, keyword..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full"
              />
            </div>
            <div>
              <Select
                placeholder="Category"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                options={categories}
                className="w-full"
              />
            </div>
            <div>
              <Select
                placeholder="State"
                value={state}
                onChange={(e) => setState(e.target.value)}
                options={states}
                className="w-full"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredSchemes.map((scheme) => (
          <SchemeCard key={scheme.id} scheme={scheme} />
        ))}
      </div>

      {filteredSchemes.length === 0 && (
        <Card padding="lg" className="text-center">
          <CardContent>
            <p className="text-gray-500">No schemes found matching your criteria.</p>
            <Button variant="outline" className="mt-4" onClick={() => { setSearch(''); setCategory(''); setState(''); }}>
              Clear filters
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

interface SchemeCardProps {
  scheme: typeof schemes[0];
}

function SchemeCard({ scheme }: SchemeCardProps) {
  const statusConfig = {
    eligible: { label: 'Eligible', className: 'bg-green-100 text-green-700' },
    needs_info: { label: 'Need Info', className: 'bg-yellow-100 text-yellow-700' },
    not_eligible: { label: 'Not Eligible', className: 'bg-red-100 text-red-700' },
  };

  const status = statusConfig[scheme.status];

  return (
    <Card padding="md" hover className="h-full flex flex-col">
      <div className="flex items-start justify-between mb-3">
        <div>
          <p className="text-xs text-gray-500 uppercase tracking-wide">{scheme.category}</p>
          <h3 className="font-semibold text-gray-900 mt-1">{scheme.name}</h3>
          <p className="text-sm text-gray-500 mt-1">{scheme.shortTitle}</p>
        </div>
        <span className={`px-2 py-1 text-xs font-medium rounded-full ${status.className}`}>
          {status.label}
        </span>
      </div>

      <p className="text-gray-600 text-sm mb-3 flex-1">{scheme.description}</p>

      <div className="flex items-center gap-4 text-sm text-gray-500 mb-4">
        <span className="flex items-center gap-1">
          <span className="w-2 h-2 rounded-full bg-gray-300" />
          {scheme.state}
        </span>
        <span className="font-medium text-gray-900">{scheme.benefit}</span>
      </div>

      <div className="pt-3 border-t border-gray-100">
        <Button variant="outline" className="w-full" size="sm">
          View Details
        </Button>
      </div>
    </Card>
  );
}