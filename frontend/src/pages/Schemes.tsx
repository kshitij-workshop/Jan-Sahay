import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Filter } from 'lucide-react';
import { Card, CardContent } from '../components/common/Card';
import { Input } from '../components/common/Input';
import { Select } from '../components/common/Select';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { schemeService, SchemeSummary } from '../services/schemes';

const states = [
  { value: '', label: 'All States' },
  { value: 'Bihar', label: 'Bihar' },
  { value: 'Uttar Pradesh', label: 'Uttar Pradesh' },
  { value: 'Jharkhand', label: 'Jharkhand' },
  { value: 'West Bengal', label: 'West Bengal' },
  { value: 'Madhya Pradesh', label: 'Madhya Pradesh' },
];

const levels = [
  { value: '', label: 'All Levels' },
  { value: 'Central', label: 'Central' },
  { value: 'State', label: 'State' },
];

const PAGE_SIZE = 12;

export default function Schemes() {
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [category, setCategory] = useState('');
  const [state, setState] = useState('');
  const [level, setLevel] = useState('');
  const [page, setPage] = useState(0);
  const [categories, setCategories] = useState<{ value: string; label: string }[]>([
    { value: '', label: 'All Categories' },
  ]);
  const [schemes, setSchemes] = useState<SchemeSummary[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(search);
      setPage(0);
    }, 400);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    schemeService
      .categories()
      .then((list) =>
        setCategories([
          { value: '', label: 'All Categories' },
          ...list.map((c) => ({ value: c, label: c })),
        ])
      )
      .catch(() => {});
  }, []);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const result = await schemeService.list({
          q: debouncedSearch || undefined,
          category: category || undefined,
          state: state || undefined,
          level: level || undefined,
          page,
          size: PAGE_SIZE,
        });
        if (!cancelled) {
          setSchemes(result.content);
          setTotalElements(result.totalElements);
          setTotalPages(result.totalPages);
        }
      } catch {
        if (!cancelled) {
          setError('Could not load schemes. Please try again.');
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
  }, [debouncedSearch, category, state, level, page, attempt]);

  const clearFilters = () => {
    setSearch('');
    setCategory('');
    setState('');
    setLevel('');
    setPage(0);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Government Schemes</h1>
          <p className="text-gray-500 mt-1">
            {totalElements > 0 ? `${totalElements} schemes in the catalog` : 'Discover schemes you may be eligible for'}
          </p>
        </div>
        <div className="flex gap-2 items-center text-sm text-gray-500">
          <Filter className="h-4 w-4" />
          Eligibility checking arrives in Phase 6
        </div>
      </div>

      <Card padding="md">
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
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
                onChange={(e) => { setCategory(e.target.value); setPage(0); }}
                options={categories}
                className="w-full"
              />
            </div>
            <div>
              <Select
                placeholder="State"
                value={state}
                onChange={(e) => { setState(e.target.value); setPage(0); }}
                options={states}
                className="w-full"
              />
            </div>
            <div>
              <Select
                placeholder="Level"
                value={level}
                onChange={(e) => { setLevel(e.target.value); setPage(0); }}
                options={levels}
                className="w-full"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <div className="flex justify-center py-12">
          <LoadingSpinner size="lg" />
        </div>
      ) : error ? (
        <Card padding="lg" className="text-center">
          <CardContent>
            <p className="text-red-600">{error}</p>
            <Button variant="outline" className="mt-4" onClick={() => setAttempt((a) => a + 1)}>
              Retry
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {schemes.map((scheme) => (
              <SchemeCard key={scheme.id} scheme={scheme} />
            ))}
          </div>

          {schemes.length === 0 && (
            <Card padding="lg" className="text-center">
              <CardContent>
                <p className="text-gray-500">No schemes found matching your criteria.</p>
                <Button variant="outline" className="mt-4" onClick={clearFilters}>
                  Clear filters
                </Button>
              </CardContent>
            </Card>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-4">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
                Previous
              </Button>
              <span className="text-sm text-gray-600">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                disabled={page + 1 >= totalPages}
                onClick={() => setPage(page + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

function SchemeCard({ scheme }: { scheme: SchemeSummary }) {
  return (
    <Card padding="md" hover className="h-full flex flex-col">
      <div className="mb-3">
        <p className="text-xs text-gray-500 uppercase tracking-wide">{scheme.category || 'General'}</p>
        <h3 className="font-semibold text-gray-900 mt-1">{scheme.name || scheme.slug}</h3>
        {scheme.shortTitle && <p className="text-sm text-gray-500 mt-1">{scheme.shortTitle}</p>}
      </div>

      {scheme.description && (
        <p className="text-gray-600 text-sm mb-3 flex-1 line-clamp-3">{scheme.description}</p>
      )}

      <div className="flex items-center gap-4 text-sm text-gray-500 mb-4">
        {scheme.state && (
          <span className="flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-gray-300" />
            {scheme.state}
          </span>
        )}
        {scheme.benefitType && <span className="font-medium text-gray-900">{scheme.benefitType}</span>}
      </div>

      <div className="pt-3 border-t border-gray-100 mt-auto">
        <Link to={`/schemes/${scheme.id}`}>
          <Button variant="outline" className="w-full" size="sm">
            View Details
          </Button>
        </Link>
      </div>
    </Card>
  );
}
