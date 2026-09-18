import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Users, FileText, CheckCircle, FolderOpen, FileCheck, Bell, TrendingUp } from 'lucide-react';

const stats = [
  { name: 'Eligible Schemes', value: '12', icon: FileText, color: 'text-blue-600 bg-blue-100' },
  { name: 'Documents', value: '5', icon: FolderOpen, color: 'text-green-600 bg-green-100' },
  { name: 'Applications', value: '3', icon: FileCheck, color: 'text-purple-600 bg-purple-100' },
  { name: 'Notifications', value: '2', icon: Bell, color: 'text-orange-600 bg-orange-100' },
];

const quickActions = [
  { name: 'Browse Schemes', href: '/schemes', icon: FileText },
  { name: 'My Matches', href: '/matches', icon: CheckCircle },
  { name: 'Upload Documents', href: '/documents', icon: FolderOpen },
  { name: 'View Applications', href: '/applications', icon: FileCheck },
];

export default function Dashboard() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-500 mt-1">Overview of your scheme eligibility and applications</p>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {stats.map((stat) => (
          <Card key={stat.name} padding="md" hover>
            <CardContent>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500">{stat.name}</p>
                  <p className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</p>
                </div>
                <div className={`${stat.color} p-3 rounded-xl`}>
                  <stat.icon className="h-6 w-6" />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card padding="md">
          <CardHeader title="Quick Actions" subtitle="Common tasks you can perform" />
          <CardContent>
            <div className="grid grid-cols-2 gap-3">
              {quickActions.map((action) => (
                <a
                  key={action.name}
                  href={action.href}
                  className="flex flex-col items-center p-4 border border-gray-200 rounded-lg hover:border-primary-300 hover:bg-primary-50 transition-colors"
                >
                  <action.icon className="h-8 w-8 text-primary-600 mb-2" />
                  <span className="text-sm font-medium text-gray-700 text-center">{action.name}</span>
                </a>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card padding="md">
          <CardHeader title="Recommended Schemes" subtitle="Based on your profile" />
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                    <FileText className="h-5 w-5 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">PM Kisan Samman Nidhi</p>
                    <p className="text-sm text-gray-500">Income support for farmers</p>
                  </div>
                </div>
                <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-700 rounded-full">Eligible</span>
              </div>
              <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                    <FileText className="h-5 w-5 text-green-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Student Scholarship Scheme</p>
                    <p className="text-sm text-gray-500">Scholarship for Bihar students</p>
                  </div>
                </div>
                <span className="px-2 py-1 text-xs font-medium bg-yellow-100 text-yellow-700 rounded-full">Need Info</span>
              </div>
              <div className="flex items-center justify-between p-3 border border-gray-200 rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                    <FileText className="h-5 w-5 text-purple-600" />
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">Women Entrepreneurship</p>
                    <p className="text-sm text-gray-500">Support for women entrepreneurs</p>
                  </div>
                </div>
                <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-700 rounded-full">Not Eligible</span>
              </div>
            </div>
            <div className="mt-4 text-center">
              <a href="/schemes" className="text-primary-600 hover:text-primary-700 font-medium text-sm">
                View all schemes →
              </a>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card padding="md">
          <CardHeader title="Important Actions" subtitle="Items requiring your attention" />
          <CardContent>
            <div className="space-y-3">
              <div className="flex items-center gap-3 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                <TrendingUp className="h-5 w-5 text-yellow-600 flex-shrink-0" />
                <div>
                  <p className="font-medium text-yellow-800">Income Certificate mismatch detected</p>
                  <p className="text-sm text-yellow-700">Review document before applying</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-red-50 border border-red-200 rounded-lg">
                <FileText className="h-5 w-5 text-red-600 flex-shrink-0" />
                <div>
                  <p className="font-medium text-red-800">Caste Certificate missing</p>
                  <p className="text-sm text-red-700">Required for 3 eligible schemes</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card padding="md">
          <CardHeader title="Profile Completeness" subtitle="Complete your profile for better matches" />
          <CardContent>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-600">Profile completeness</span>
                  <span className="font-medium text-gray-900">75%</span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div className="h-full bg-primary-600 rounded-full" style={{ width: '75%' }} />
                </div>
              </div>
              <div className="space-y-2 text-sm text-gray-600">
                <p>• Personal information - Complete</p>
                <p>• Address details - Complete</p>
                <p>• Education info - Missing</p>
                <p>• Income details - Complete</p>
                <p>• Social category - Complete</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}