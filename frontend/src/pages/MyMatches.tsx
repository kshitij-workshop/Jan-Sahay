import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { CheckCircle, AlertCircle, AlertTriangle, Clock, FileText, ChevronRight } from 'lucide-react';

const matches = [
  {
    id: '1',
    scheme: 'PM Kisan Samman Nidhi',
    shortTitle: 'PM-KISAN',
    category: 'Agriculture',
    benefit: '₹6,000/year',
    status: 'eligible',
    matchedAt: '2024-01-10',
    reason: 'All criteria met: age, state, occupation, landholding',
  },
  {
    id: '2',
    scheme: 'Bihar Student Credit Card',
    shortTitle: 'BSCC',
    category: 'Education',
    benefit: 'Up to ₹4 lakh',
    status: 'needs_info',
    matchedAt: '2024-01-12',
    reason: 'Missing: course details, institution name',
  },
  {
    id: '3',
    scheme: 'Mukhyamantri Kanya Utthan Yojana',
    shortTitle: 'MKUY',
    category: 'Women Empowerment',
    benefit: '₹25,000',
    status: 'eligible',
    matchedAt: '2024-01-15',
    reason: 'All criteria met: gender, state, education status',
  },
  {
    id: '4',
    scheme: 'MGNREGA',
    shortTitle: 'MGNREGA',
    category: 'Employment',
    benefit: '100 days employment',
    status: 'not_eligible',
    matchedAt: '2024-01-15',
    reason: 'Urban residence - scheme for rural households only',
  },
];

const statusConfig = {
  eligible: { label: 'Eligible', className: 'bg-green-100 text-green-700', icon: CheckCircle },
  needs_info: { label: 'Need Info', className: 'bg-yellow-100 text-yellow-700', icon: AlertCircle },
  not_eligible: { label: 'Not Eligible', className: 'bg-red-100 text-red-700', icon: AlertTriangle },
};

export default function MyMatches() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">My Scheme Matches</h1>
        <p className="text-gray-500 mt-1">Schemes matched based on your profile</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card padding="md">
          <CardContent>
            <p className="text-sm text-gray-500">Total Matches</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{matches.length}</p>
          </CardContent>
        </Card>
        <Card padding="md">
          <CardContent>
            <p className="text-sm text-gray-500">Eligible</p>
            <p className="text-2xl font-bold text-green-600 mt-1">
              {matches.filter(m => m.status === 'eligible').length}
            </p>
          </CardContent>
        </Card>
        <Card padding="md">
          <CardContent>
            <p className="text-sm text-gray-500">Need Information</p>
            <p className="text-2xl font-bold text-yellow-600 mt-1">
              {matches.filter(m => m.status === 'needs_info').length}
            </p>
          </CardContent>
        </Card>
        <Card padding="md">
          <CardContent>
            <p className="text-sm text-gray-500">Not Eligible</p>
            <p className="text-2xl font-bold text-red-600 mt-1">
              {matches.filter(m => m.status === 'not_eligible').length}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="space-y-4">
        {matches.map((match) => {
          const status = statusConfig[match.status as keyof typeof statusConfig];
          return (
            <Card key={match.id} padding="md" hover>
              <CardContent>
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="font-semibold text-gray-900">{match.scheme}</h3>
                      <span className={`px-2 py-1 text-xs font-medium rounded-full ${status.className} flex items-center gap-1`}>
                        <status.icon className="h-3 w-3" />
                        {status.label}
                      </span>
                    </div>
                    <p className="text-sm text-gray-500 mb-2">{match.shortTitle} • {match.category}</p>
                    <p className="text-sm text-gray-600 mb-3">{match.reason}</p>
                    <div className="flex items-center gap-4 text-sm text-gray-500">
                      <span className="font-medium text-gray-900">{match.benefit}</span>
                      <span><Clock className="h-4 w-4 inline mr-1" /> Matched {match.matchedAt}</span>
                    </div>
                  </div>
                  <Button variant="outline" size="sm">
                    View Details <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}