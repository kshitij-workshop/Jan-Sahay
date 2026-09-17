import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, FileText, CheckCircle, AlertCircle, Clock, MapPin, User, AlertTriangle } from 'lucide-react';
import { Card, CardHeader, CardContent, CardFooter } from '../components/common/Card';
import { Button } from '../components/common/Button';

export default function SchemeDetail() {
  const { id } = useParams();

  const scheme = {
    id: '1',
    name: 'PM Kisan Samman Nidhi',
    shortTitle: 'PM-KISAN',
    description: 'The PM-KISAN scheme aims to supplement the financial needs of small and marginal farmers in procuring various inputs to ensure proper crop health and appropriate yields, commensurate with the anticipated farm income at the end of each crop cycle.',
    category: 'Agriculture',
    state: 'All India',
    level: 'Central',
    ministry: 'Ministry of Agriculture & Farmers Welfare',
    benefit: '₹6,000 per year in three equal installments',
    eligibility: [
      'Small and marginal farmers holding cultivable land',
      'Farmer families with combined landholding up to 2 hectares',
      'Must be Indian citizen',
      'Land ownership records must be updated',
    ],
    documents: [
      'Aadhaar Card',
      'Land ownership documents',
      'Bank account passbook',
      'Mobile number linked to Aadhaar',
    ],
    process: [
      'Visit nearest CSC or PM-KISAN portal',
      'Register with Aadhaar and land details',
      'Submit required documents',
      'Verification by state authorities',
      'Amount credited directly to bank account',
    ],
    faqs: [
      { q: 'Who is eligible for PM-KISAN?', a: 'All small and marginal farmer families having combined landholding up to 2 hectares.' },
      { q: 'How is the benefit paid?', a: '₹6,000 per year in three equal installments of ₹2,000 each, directly to bank account.' },
      { q: 'Can tenant farmers apply?', a: 'No, only land owners with valid land records are eligible.' },
    ],
    officialUrl: 'https://pmkisan.gov.in/',
    lastSynced: '2024-01-15',
    status: 'eligible',
  };

  const statusConfig = {
    eligible: { label: 'Eligible', className: 'bg-green-100 text-green-700', icon: CheckCircle },
    needs_info: { label: 'Need More Information', className: 'bg-yellow-100 text-yellow-700', icon: AlertCircle },
    not_eligible: { label: 'Not Eligible', className: 'bg-red-100 text-red-700', icon: AlertTriangle },
  };

  const status = statusConfig[scheme.status as keyof typeof statusConfig];

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Link to="/schemes" className="p-2 rounded-lg hover:bg-gray-100 transition-colors" aria-label="Back to schemes">
          <ArrowLeft className="h-5 w-5 text-gray-600" />
        </Link>
        <div>
          <p className="text-sm text-gray-500">Schemes / {scheme.category}</p>
          <h1 className="text-2xl font-bold text-gray-900">{scheme.name}</h1>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card padding="md">
            <CardHeader
              title={scheme.name}
              subtitle={scheme.shortTitle}
              action={
                <span className={`px-3 py-1 text-sm font-medium rounded-full ${status.className} flex items-center gap-1`}>
                  <status.icon className="h-4 w-4" />
                  {status.label}
                </span>
              }
            />
            <CardContent>
              <p className="text-gray-600 mb-6">{scheme.description}</p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <FileText className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="text-xs text-gray-500">Category</p>
                    <p className="font-medium text-gray-900">{scheme.category}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <MapPin className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="text-xs text-gray-500">State</p>
                    <p className="font-medium text-gray-900">{scheme.state}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <User className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="text-xs text-gray-500">Level</p>
                    <p className="font-medium text-gray-900">{scheme.level}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <Clock className="h-5 w-5 text-primary-600" />
                  <div>
                    <p className="text-xs text-gray-500">Last Synced</p>
                    <p className="font-medium text-gray-900">{scheme.lastSynced}</p>
                  </div>
                </div>
              </div>

              <div className="space-y-6">
                <section>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Benefits</h3>
                  <p className="text-gray-600">{scheme.benefit}</p>
                </section>

                <section>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Eligibility Criteria</h3>
                  <ul className="space-y-2">
                    {scheme.eligibility.map((item, i) => (
                      <li key={i} className="flex items-start gap-2 text-gray-600">
                        <CheckCircle className="h-5 w-5 text-green-600 flex-shrink-0 mt-0.5" />
                        {item}
                      </li>
                    ))}
                  </ul>
                </section>

                <section>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Required Documents</h3>
                  <ul className="space-y-2">
                    {scheme.documents.map((doc, i) => (
                      <li key={i} className="flex items-center gap-2 text-gray-600">
                        <FileText className="h-5 w-5 text-primary-600 flex-shrink-0" />
                        {doc}
                      </li>
                    ))}
                  </ul>
                </section>

                <section>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Application Process</h3>
                  <ol className="space-y-3">
                    {scheme.process.map((step, i) => (
                      <li key={i} className="flex gap-3">
                        <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-100 text-primary-700 text-sm font-medium flex items-center justify-center">
                          {i + 1}
                        </span>
                        <p className="text-gray-600 mt-0.5">{step}</p>
                      </li>
                    ))}
                  </ol>
                </section>

                <section>
                  <h3 className="text-lg font-semibold text-gray-900 mb-3">Frequently Asked Questions</h3>
                  <div className="space-y-3">
                    {scheme.faqs.map((faq, i) => (
                      <details key={i} className="group border border-gray-200 rounded-lg">
                        <summary className="p-4 font-medium text-gray-900 cursor-pointer list-none">
                          {faq.q}
                        </summary>
                        <div className="px-4 pb-4 text-gray-600 border-t border-gray-200">
                          {faq.a}
                        </div>
                      </details>
                    ))}
                  </div>
                </section>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <Card padding="md">
            <CardHeader title="Quick Actions" />
            <CardContent className="space-y-3">
              <Button variant="primary" className="w-full" size="lg">
                Apply with Smart Form
              </Button>
              <Button variant="outline" className="w-full" size="lg">
                Check Eligibility
              </Button>
              <Button variant="ghost" className="w-full justify-start" size="lg">
                <a href={scheme.officialUrl} target="_blank" rel="noopener noreferrer" className="w-full flex items-center gap-2">
                  <ArrowLeft className="h-4 w-4" />
                  Official Portal
                </a>
              </Button>
            </CardContent>
          </Card>

          <Card padding="md">
            <CardHeader title="Eligibility Summary" />
            <CardContent>
              <div className="space-y-3">
                <div className="flex items-center gap-2 text-green-600">
                  <CheckCircle className="h-5 w-5" /> Age: 28 years ✓
                </div>
                <div className="flex items-center gap-2 text-green-600">
                  <CheckCircle className="h-5 w-5" /> State: Bihar ✓
                </div>
                <div className="flex items-center gap-2 text-green-600">
                  <CheckCircle className="h-5 w-5" /> Occupation: Farmer ✓
                </div>
                <div className="flex items-center gap-2 text-yellow-600">
                  <AlertCircle className="h-5 w-5" /> Land records: Pending verification
                </div>
              </div>
              <p className="text-sm text-gray-500 mt-4">
                Based on your profile information. Final eligibility determined by authorities.
              </p>
            </CardContent>
          </Card>

          <Card padding="md" className="bg-blue-50 border-blue-200">
            <CardHeader title="Important" />
            <CardContent>
              <p className="text-sm text-blue-800">
                This information is based on data from myScheme.gov.in. Always verify details on the official portal before applying.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}