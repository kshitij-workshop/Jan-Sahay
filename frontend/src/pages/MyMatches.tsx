import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle, AlertCircle, AlertTriangle, ChevronRight } from 'lucide-react';
import { Card, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { matchService, Match, MatchStatus, MatchSummary } from '../services/matches';

const statusConfig: Record<MatchStatus, { label: string; className: string; icon: React.ElementType }> = {
  ELIGIBLE: { label: 'Eligible', className: 'bg-green-100 text-green-700', icon: CheckCircle },
  INSUFFICIENT_INFORMATION: { label: 'Need Info', className: 'bg-yellow-100 text-yellow-700', icon: AlertCircle },
  NOT_ELIGIBLE: { label: 'Not Eligible', className: 'bg-red-100 text-red-700', icon: AlertTriangle },
};

type Filter = 'ALL' | MatchStatus;

export default function MyMatches() {
  const [filter, setFilter] = useState<Filter>('ALL');
  const [matches, setMatches] = useState<Match[]>([]);
  const [summary, setSummary] = useState<MatchSummary>({
    total: 0,
    eligible: 0,
    insufficientInformation: 0,
    notEligible: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [data, totals] = await Promise.all([
          matchService.list(filter === 'ALL' ? undefined : filter),
          matchService.summary(),
        ]);
        if (!cancelled) {
          setMatches(data);
          setSummary(totals);
        }
      } catch {
        if (!cancelled) {
          setError('Could not load your matches. Complete your profile first, then try again.');
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
  }, [filter]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Scheme Matches</h1>
          <p className="text-gray-500 mt-1">Stored verdicts, recalculated whenever your profile changes</p>
        </div>
        <div className="flex gap-2">
          {(['ALL', 'ELIGIBLE', 'INSUFFICIENT_INFORMATION', 'NOT_ELIGIBLE'] as Filter[]).map((f) => (
            <Button
              key={f}
              variant={filter === f ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setFilter(f)}
            >
              {f === 'ALL' ? 'All' : statusConfig[f].label}
            </Button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total Matches" value={summary.total} />
        <StatCard label="Eligible" value={summary.eligible} tone="text-green-600" />
        <StatCard label="Need Information" value={summary.insufficientInformation} tone="text-yellow-600" />
        <StatCard label="Not Eligible" value={summary.notEligible} tone="text-red-600" />
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner size="lg" />
        </div>
      ) : error ? (
        <Card padding="lg" className="text-center">
          <CardContent>
            <p className="text-red-600">{error}</p>
          </CardContent>
        </Card>
      ) : matches.length === 0 ? (
        <Card padding="lg" className="text-center">
          <CardContent>
            <p className="text-gray-500">No matches in this view yet.</p>
            <p className="text-sm text-gray-400 mt-1">
              Complete your profile or run a scheme sync — matches recalculate automatically.
            </p>
            <Link to="/profile">
              <Button variant="outline" className="mt-4" size="sm">
                Complete Profile
              </Button>
            </Link>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {matches.map((match) => {
            const status = statusConfig[match.status];
            return (
              <Card key={`${match.schemeId}`} padding="md" hover>
                <CardContent>
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-3 mb-2 flex-wrap">
                        <h3 className="font-semibold text-gray-900">{match.schemeName || match.schemeSlug}</h3>
                        <span className={`px-2 py-1 text-xs font-medium rounded-full ${status.className} flex items-center gap-1`}>
                          <status.icon className="h-3 w-3" />
                          {status.label}
                        </span>
                      </div>
                      {match.matchReason && <p className="text-sm text-gray-600 mb-2">{match.matchReason}</p>}
                      {match.missingInformation.length > 0 && (
                        <p className="text-sm text-yellow-700 mb-2">
                          Missing: {match.missingInformation.join(', ')}
                        </p>
                      )}
                      <p className="text-xs text-gray-400">
                        First matched {new Date(match.firstMatchedAt).toLocaleDateString()} • Checked{' '}
                        {new Date(match.lastCheckedAt).toLocaleDateString()}
                      </p>
                    </div>
                    <Link to={`/schemes/${match.schemeId}`}>
                      <Button variant="outline" size="sm">
                        View Details <ChevronRight className="h-4 w-4" />
                      </Button>
                    </Link>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}

function StatCard({ label, value, tone }: { label: string; value: number; tone?: string }) {
  return (
    <Card padding="md">
      <CardContent>
        <p className="text-sm text-gray-500">{label}</p>
        <p className={`text-2xl font-bold mt-1 ${tone || 'text-gray-900'}`}>{value}</p>
      </CardContent>
    </Card>
  );
}
