import { useEffect, useState } from 'react';
import { ShieldCheck, AlertTriangle, Loader2 } from 'lucide-react';
import api from '../services/api';
import { Card, CardHeader, CardContent } from '../components/common/Card';

/**
 * Minimal admin surface for Phase 2: proves role-based authorization works
 * end-to-end. The full admin dashboard (sync monitoring, mapping approval)
 * lands in Phase 14.
 */
export default function Admin() {
  const [state, setState] = useState<'loading' | 'ok' | 'denied' | 'error'>('loading');
  const [detail, setDetail] = useState('');

  useEffect(() => {
    let cancelled = false;
    const check = async () => {
      try {
        const response = await api.get('/admin/ping');
        if (!cancelled) {
          setState('ok');
          setDetail(response.data?.message || 'Admin access confirmed');
        }
      } catch (err: any) {
        if (!cancelled) {
          const status = err.response?.status;
          setState(status === 403 ? 'denied' : 'error');
          setDetail(err.response?.data?.message || 'Could not reach the admin endpoint.');
        }
      }
    };
    check();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Admin</h1>
        <p className="text-gray-500 mt-1">Restricted area — administrators only</p>
      </div>

      <Card padding="md">
        <CardHeader title="Access check" subtitle="GET /api/admin/ping with role enforcement" />
        <CardContent>
          {state === 'loading' && (
            <div className="flex items-center gap-2 text-gray-600" role="status">
              <Loader2 className="h-5 w-5 animate-spin" /> Checking admin access…
            </div>
          )}
          {state === 'ok' && (
            <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm flex gap-2" role="alert">
              <ShieldCheck className="h-5 w-5 flex-shrink-0" />
              <span>{detail}</span>
            </div>
          )}
          {state === 'denied' && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm flex gap-2" role="alert">
              <AlertTriangle className="h-5 w-5 flex-shrink-0" />
              <span>Access denied. This area requires the ADMIN role.</span>
            </div>
          )}
          {state === 'error' && (
            <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-700 text-sm" role="alert">
              {detail}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
