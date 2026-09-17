import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, FileText, Clock, MapPin, Building2, ExternalLink, AlertTriangle } from 'lucide-react';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { schemeService, SchemeDetail as Detail } from '../services/schemes';

export default function SchemeDetail() {
  const { id } = useParams();
  const [scheme, setScheme] = useState<Detail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      if (!id) {
        setError('No scheme selected.');
        setLoading(false);
        return;
      }
      try {
        const detail = await schemeService.getById(id);
        if (!cancelled) {
          setScheme(detail);
        }
      } catch (err: any) {
        if (!cancelled) {
          setError(
            err.response?.status === 404
              ? 'Scheme not found.'
              : 'Could not load this scheme. Please try again.'
          );
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
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-[40vh] flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error || !scheme) {
    return (
      <div className="space-y-6">
        <Link to="/schemes" className="inline-flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700">
          <ArrowLeft className="h-4 w-4" /> Back to schemes
        </Link>
        <Card padding="lg" className="text-center">
          <CardContent>
            <p className="text-gray-600">{error || 'Scheme not found.'}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link to="/schemes" className="p-2 rounded-lg hover:bg-gray-100 transition-colors" aria-label="Back to schemes">
          <ArrowLeft className="h-5 w-5 text-gray-600" />
        </Link>
        <div>
          <p className="text-sm text-gray-500">Schemes / {scheme.category || 'General'}</p>
          <h1 className="text-2xl font-bold text-gray-900">{scheme.name || scheme.slug}</h1>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card padding="md">
            <CardHeader title={scheme.name || scheme.slug} subtitle={scheme.shortTitle} />
            <CardContent>
              {scheme.description && <p className="text-gray-600 mb-6">{scheme.description}</p>}

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <Fact icon={FileText} label="Category" value={scheme.category} />
                <Fact icon={MapPin} label="State" value={scheme.state} />
                <Fact icon={Building2} label="Ministry" value={scheme.ministry} />
                <Fact
                  icon={Clock}
                  label="Last Synced"
                  value={scheme.lastSyncedAt ? new Date(scheme.lastSyncedAt).toLocaleDateString() : undefined}
                />
              </div>

              {scheme.tags.length > 0 && (
                <div className="flex flex-wrap gap-2 mb-6">
                  {scheme.tags.map((tag) => (
                    <span key={tag} className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded-full">
                      {tag}
                    </span>
                  ))}
                </div>
              )}

              <div className="space-y-6">
                {scheme.detailedDescription && (
                  <Section title="About this scheme">
                    <Markdown text={scheme.detailedDescription} />
                  </Section>
                )}

                {scheme.benefits && (
                  <Section title="Benefits">
                    <Markdown text={scheme.benefits} />
                  </Section>
                )}

                {scheme.eligibility && (
                  <Section title="Eligibility (as published)">
                    <Markdown text={scheme.eligibility} />
                    <p className="text-sm text-gray-500 mt-3">
                      Automated eligibility matching arrives in Phase 6. Final eligibility is
                      always determined by the authorities.
                    </p>
                  </Section>
                )}

                {scheme.exclusions && (
                  <Section title="Exclusions">
                    <Markdown text={scheme.exclusions} />
                  </Section>
                )}

                {scheme.documents.length > 0 && (
                  <section>
                    <h3 className="text-lg font-semibold text-gray-900 mb-3">Required Documents</h3>
                    <ul className="space-y-2">
                      {scheme.documents.map((doc, i) => (
                        <li key={i} className="flex items-center gap-2 text-gray-600">
                          <FileText className="h-5 w-5 text-primary-600 flex-shrink-0" />
                          {doc.name}
                        </li>
                      ))}
                    </ul>
                  </section>
                )}
                {scheme.documents.length === 0 && scheme.documentsText && (
                  <Section title="Required Documents">
                    <Markdown text={scheme.documentsText} />
                  </Section>
                )}

                {scheme.applicationProcess.length > 0 && (
                  <section>
                    <h3 className="text-lg font-semibold text-gray-900 mb-3">Application Process</h3>
                    <ol className="space-y-3">
                      {scheme.applicationProcess.map((step) => (
                        <li key={step.stepNo} className="flex gap-3">
                          <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-100 text-primary-700 text-sm font-medium flex items-center justify-center">
                            {step.stepNo}
                          </span>
                          <p className="text-gray-600 mt-0.5">{step.description}</p>
                        </li>
                      ))}
                    </ol>
                  </section>
                )}

                {scheme.faqs.length > 0 && (
                  <section>
                    <h3 className="text-lg font-semibold text-gray-900 mb-3">Frequently Asked Questions</h3>
                    <div className="space-y-3">
                      {scheme.faqs.map((faq, i) => (
                        <details key={i} className="group border border-gray-200 rounded-lg">
                          <summary className="p-4 font-medium text-gray-900 cursor-pointer list-none">
                            {faq.question}
                          </summary>
                          {faq.answer && (
                            <div className="px-4 pb-4 text-gray-600 border-t border-gray-200 whitespace-pre-wrap">
                              {faq.answer}
                            </div>
                          )}
                        </details>
                      ))}
                    </div>
                  </section>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card padding="md">
            <CardHeader title="Official sources" />
            <CardContent className="space-y-3">
              {scheme.sourceUrl && (
                <a href={scheme.sourceUrl} target="_blank" rel="noopener noreferrer" className="block">
                  <Button variant="primary" className="w-full gap-1" size="lg">
                    <ExternalLink className="h-4 w-4" /> Open Official Portal
                  </Button>
                </a>
              )}
              {scheme.references.map((ref, i) => (
                <a key={i} href={ref.url} target="_blank" rel="noopener noreferrer" className="block">
                  <Button variant="outline" className="w-full justify-start gap-1" size="lg">
                    <ExternalLink className="h-4 w-4" /> {ref.title}
                  </Button>
                </a>
              ))}
              {!scheme.sourceUrl && scheme.references.length === 0 && (
                <p className="text-sm text-gray-500">No official links stored for this scheme yet.</p>
              )}
            </CardContent>
          </Card>

          <Card padding="md" className="bg-blue-50 border-blue-200">
            <CardHeader title="About this data" />
            <CardContent>
              <p className="text-sm text-blue-800">
                Shown exactly as synchronized from {scheme.source || 'the source catalog'}
                {scheme.lastSyncedAt && ` on ${new Date(scheme.lastSyncedAt).toLocaleDateString()}`}.
                Always verify details on the official portal before applying. This platform
                never submits applications on your behalf.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

function Fact({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value?: string }) {
  if (!value) return null;
  return (
    <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
      <Icon className="h-5 w-5 text-primary-600 flex-shrink-0" />
      <div className="min-w-0">
        <p className="text-xs text-gray-500">{label}</p>
        <p className="font-medium text-gray-900 truncate">{value}</p>
      </div>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section>
      <h3 className="text-lg font-semibold text-gray-900 mb-3">{title}</h3>
      {children}
    </section>
  );
}

function Markdown({ text }: { text: string }) {
  return <p className="text-gray-600 whitespace-pre-wrap">{text}</p>;
}
