import { useEffect, useState } from 'react';
import { ShieldCheck, AlertTriangle, Loader2, RefreshCw } from 'lucide-react';
import { adminService, SyncJob } from '../services/admin';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';

/**
 * Admin surface: access check plus scheme-sync trigger and job history.
 * Full sync monitoring and mapping approval land in Phase 14.
 */
export default function Admin() {
  const [state, setState] = useState<'loading' | 'ok' | 'denied' | 'error'>('loading');
  const [detail, setDetail] = useState('');
  const [jobs, setJobs] = useState<SyncJob[]>([]);
  const [syncing, setSyncing] = useState(false);
  const [syncMessage, setSyncMessage] = useState('');

  const loadJobs = async () => {
    try {
      const page = await adminService.syncJobs(0, 10);
      setJobs(page.content);
    } catch {
      // Job history is best-effort; the access check above is authoritative.
    }
  };

  useEffect(() => {
    let cancelled = false;
    const check = async () => {
      try {
        const message = await adminService.ping();
        if (!cancelled) {
          setState('ok');
          setDetail(message || 'Admin access confirmed');
          await loadJobs();
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

  const handleSync = async () => {
    setSyncing(true);
    setSyncMessage('');
    try {
      const job = await adminService.syncSchemes();
      setSyncMessage(
        `Sync ${job.status}: fetched ${job.fetched}, created ${job.createdCount}, updated ${job.updatedCount}, failed ${job.failedCount}.`
      );
      await loadJobs();
    } catch (err: any) {
      setSyncMessage(err.response?.data?.message || 'Sync failed. Please try again.');
    } finally {
      setSyncing(false);
    }
  };

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

      {state === 'ok' && (
        <Card padding="md">
          <CardHeader
            title="Scheme synchronization"
            subtitle="Pulls schemes from myScheme.gov.in (dev limit applies)"
            action={
              <Button onClick={handleSync} loading={syncing} disabled={syncing} size="sm">
                <RefreshCw className="h-4 w-4" /> Run sync
              </Button>
            }
          />
          <CardContent>
            {syncMessage && <p className="text-sm text-gray-700 mb-4">{syncMessage}</p>}
            {jobs.length === 0 ? (
              <p className="text-sm text-gray-500">No sync jobs yet. Run a sync to populate the catalog.</p>
            ) : (
              <div className="space-y-2">
                {jobs.map((job) => (
                  <div
                    key={job.id}
                    className="flex flex-wrap items-center gap-x-4 gap-y-1 p-3 border border-gray-200 rounded-lg text-sm"
                  >
                    <span
                      className={`px-2 py-1 text-xs font-medium rounded-full ${
                        job.status === 'SUCCESS'
                          ? 'bg-green-100 text-green-700'
                          : job.status === 'FAILED'
                            ? 'bg-red-100 text-red-700'
                            : 'bg-yellow-100 text-yellow-700'
                      }`}
                    >
                      {job.status}
                    </span>
                    <span className="text-gray-600">
                      fetched {job.fetched} • +{job.createdCount} new • ~{job.updatedCount} updated • !
                      {job.failedCount} failed
                    </span>
                    <span className="text-xs text-gray-400 ml-auto">
                      {new Date(job.startedAt).toLocaleString()}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
