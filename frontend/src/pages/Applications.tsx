import { Card, CardHeader, CardContent, CardFooter } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { FileCheck, Clock, CheckCircle, AlertCircle, Edit, Download, ExternalLink } from 'lucide-react';

const applications = [
  {
    id: '1',
    scheme: 'PM Kisan Samman Nidhi',
    status: 'ready_for_review',
    createdAt: '2024-01-10',
    updatedAt: '2024-01-15',
    readiness: 100,
    missingFields: [],
  },
  {
    id: '2',
    scheme: 'Bihar Student Credit Card',
    status: 'draft',
    createdAt: '2024-01-12',
    updatedAt: '2024-01-12',
    readiness: 60,
    missingFields: ['Course Name', 'Institution', 'Fee Structure'],
  },
  {
    id: '3',
    scheme: 'Mukhyamantri Kanya Utthan Yojana',
    status: 'draft',
    createdAt: '2024-01-14',
    updatedAt: '2024-01-14',
    readiness: 80,
    missingFields: ['Bank Account Details'],
  },
];

const statusConfig = {
  draft: { label: 'Draft', className: 'bg-gray-100 text-gray-700', icon: FileCheck },
  ready_for_review: { label: 'Ready for Review', className: 'bg-blue-100 text-blue-700', icon: Clock },
  reviewed: { label: 'Reviewed', className: 'bg-green-100 text-green-700', icon: CheckCircle },
};

export default function Applications() {
  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Applications</h1>
          <p className="text-gray-500 mt-1">Manage your scheme application drafts</p>
        </div>
        <Button variant="primary" className="gap-2">
          <FileCheck className="h-4 w-4" />
          New Application
        </Button>
      </div>

      <div className="space-y-4">
        {applications.map((app) => {
          const status = statusConfig[app.status as keyof typeof statusConfig];
          return (
            <ApplicationCard key={app.id} app={app} status={status} />
          );
        })}
      </div>

      {applications.length === 0 && (
        <Card padding="lg" className="text-center">
          <CardContent>
            <FileCheck className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No applications yet</h3>
            <p className="text-gray-500 mb-4">Create your first application draft using the Smart Form Assistant</p>
            <Button variant="primary" className="gap-2">
              <FileCheck className="h-4 w-4" />
              Create Application
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

interface ApplicationCardProps {
  app: typeof applications[0];
  status: { label: string; className: string; icon: React.ElementType };
}

function ApplicationCard({ app, status }: ApplicationCardProps) {
  return (
    <Card padding="md">
      <CardContent>
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-3 mb-2">
              <h3 className="font-semibold text-gray-900">{app.scheme}</h3>
              <span className={`px-2 py-1 text-xs font-medium rounded-full ${status.className} flex items-center gap-1`}>
                <status.icon className="h-3 w-3" />
                {status.label}
              </span>
            </div>
            <div className="flex items-center gap-4 text-sm text-gray-500 mb-3">
              <span>Created {app.createdAt}</span>
              <span>Updated {app.updatedAt}</span>
            </div>
            <div className="mb-3">
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600">Application Readiness</span>
                <span className="font-medium text-gray-900">{app.readiness}%</span>
              </div>
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full transition-all"
                  style={{
                    width: `${app.readiness}%`,
                    backgroundColor: app.readiness === 100 ? '#059669' : app.readiness >= 70 ? '#d97706' : '#dc2626',
                  }}
                />
              </div>
            </div>
            {app.missingFields.length > 0 && (
              <div className="text-sm text-yellow-600">
                <AlertCircle className="h-4 w-4 inline mr-1" />
                Missing: {app.missingFields.join(', ')}
              </div>
            )}
          </div>
          <CardFooter>
            <Button variant="outline" size="sm" className="gap-1">
              <Edit className="h-4 w-4" /> Edit
            </Button>
            <Button variant="outline" size="sm" className="gap-1">
              <Download className="h-4 w-4" /> Download
            </Button>
            {app.status === 'ready_for_review' && (
              <Button variant="primary" size="sm" className="gap-1">
                <ExternalLink className="h-4 w-4" /> Open Portal
              </Button>
            )}
          </CardFooter>
        </div>
      </CardContent>
    </Card>
  );
}