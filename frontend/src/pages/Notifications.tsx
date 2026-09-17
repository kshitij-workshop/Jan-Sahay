import { useState } from 'react';
import { Bell, CheckCircle, AlertTriangle, Info, Clock, X, Mail, MessageSquare } from 'lucide-react';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';

const notifications = [
  {
    id: '1',
    type: 'mismatch',
    title: 'Income Certificate Mismatch',
    message: 'Your Income Certificate shows ₹2,00,000 but your profile has ₹1,50,000. Please review before applying.',
    time: '2 hours ago',
    read: false,
    severity: 'warning',
  },
  {
    id: '2',
    type: 'match',
    title: 'New Scheme Match: BSCC',
    message: 'You may be eligible for Bihar Student Credit Card Scheme based on your updated profile.',
    time: '1 day ago',
    read: false,
    severity: 'info',
  },
  {
    id: '3',
    type: 'document',
    title: 'Caste Certificate Required',
    message: '3 eligible schemes require a Caste Certificate. Upload it to improve your readiness.',
    time: '2 days ago',
    read: true,
    severity: 'warning',
  },
  {
    id: '4',
    type: 'system',
    title: 'Scheme Data Updated',
    message: '50 new schemes have been synchronized from myScheme.gov.in.',
    time: '3 days ago',
    read: true,
    severity: 'info',
  },
  {
    id: '5',
    type: 'application',
    title: 'Application Ready for Review',
    message: 'Your PM-KISAN application draft is complete and ready for submission.',
    time: '5 days ago',
    read: true,
    severity: 'success',
  },
];

const typeConfig = {
  mismatch: { icon: AlertTriangle, color: 'text-yellow-600 bg-yellow-100' },
  match: { icon: CheckCircle, color: 'text-green-600 bg-green-100' },
  document: { icon: Mail, color: 'text-blue-600 bg-blue-100' },
  system: { icon: Info, color: 'text-gray-600 bg-gray-100' },
  application: { icon: MessageSquare, color: 'text-purple-600 bg-purple-100' },
};

const severityConfig = {
  warning: 'text-yellow-600',
  info: 'text-blue-600',
  success: 'text-green-600',
};

export default function Notifications() {
  const [filter, setFilter] = useState<'all' | 'unread'>('all');

  const filtered = filter === 'unread'
    ? notifications.filter(n => !n.read)
    : notifications;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          <p className="text-gray-500 mt-1">Stay updated on your schemes and documents</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant={filter === 'all' ? 'primary' : 'outline'}
            size="sm"
            onClick={() => setFilter('all')}
          >
            All ({notifications.length})
          </Button>
          <Button
            variant={filter === 'unread' ? 'primary' : 'outline'}
            size="sm"
            onClick={() => setFilter('unread')}
          >
            Unread ({notifications.filter(n => !n.read).length})
          </Button>
        </div>
      </div>

      <div className="space-y-3">
        {filtered.map((notification) => (
          <NotificationCard key={notification.id} notification={notification} />
        ))}
      </div>

      {filtered.length === 0 && (
        <Card padding="lg" className="text-center">
          <CardContent>
            <Bell className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No notifications</h3>
            <p className="text-gray-500">You're all caught up!</p>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

interface NotificationCardProps {
  notification: typeof notifications[0];
}

function NotificationCard({ notification }: NotificationCardProps) {
  const type = typeConfig[notification.type as keyof typeof typeConfig];
  const Icon = type.icon;

  return (
    <Card
      padding="md"
      className={!notification.read ? 'bg-blue-50 border-blue-200' : ''}
    >
      <CardContent>
        <div className="flex items-start gap-3">
          <div className={`p-2 rounded-lg ${type.color} flex-shrink-0`}>
            <Icon className="h-5 w-5" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-2">
              <h3 className="font-medium text-gray-900">{notification.title}</h3>
              <span className="text-xs text-gray-500 whitespace-nowrap">{notification.time}</span>
            </div>
            <p className="text-gray-600 text-sm mt-1">{notification.message}</p>
            {!notification.read && (
              <Button
                variant="ghost"
                size="sm"
                className="mt-2 text-primary-600 hover:bg-primary-50"
                onClick={() => {}}
              >
                Mark as read
              </Button>
            )}
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="text-gray-400 hover:text-gray-600"
            onClick={() => {}}
            aria-label="Dismiss"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}