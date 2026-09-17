import { useState } from 'react';
import { Upload, FileText, AlertTriangle, CheckCircle, Eye, Download, Trash2, Loader2 } from 'lucide-react';
import { Card, CardHeader, CardContent } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Select } from '../components/common/Select';

const documentTypes = [
  { value: 'aadhaar', label: 'Aadhaar Card' },
  { value: 'pan', label: 'PAN Card' },
  { value: 'income_certificate', label: 'Income Certificate' },
  { value: 'caste_certificate', label: 'Caste Certificate' },
  { value: 'residence_certificate', label: 'Residence Certificate' },
  { value: 'student_certificate', label: 'Student Certificate' },
  { value: 'disability_certificate', label: 'Disability Certificate' },
  { value: 'other', label: 'Other' },
];

const documents = [
  {
    id: '1',
    name: 'Aadhaar Card',
    type: 'aadhaar',
    fileName: 'aadhaar_kshitij.pdf',
    uploadedAt: '2024-01-10',
    status: 'verified',
    extractedFields: { name: 'Kshitij Kumar', dob: '1998-05-15', address: 'Patna, Bihar' },
    mismatch: null,
  },
  {
    id: '2',
    name: 'Income Certificate',
    type: 'income_certificate',
    fileName: 'income_cert_2024.pdf',
    uploadedAt: '2024-01-12',
    status: 'mismatch',
    extractedFields: { name: 'Kshitij Kumar', income: '200000', financialYear: '2023-24' },
    mismatch: { field: 'income', profile: '150000', document: '200000', severity: 'warning' },
  },
  {
    id: '3',
    name: 'Caste Certificate',
    type: 'caste_certificate',
    fileName: 'caste_cert.pdf',
    uploadedAt: '2024-01-14',
    status: 'pending',
    extractedFields: null,
    mismatch: null,
  },
];

export default function Documents() {
  const [showUpload, setShowUpload] = useState(false);
  const [uploading, setUploading] = useState(false);

  const handleUpload = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setUploading(true);
    await new Promise(r => setTimeout(r, 1000));
    setUploading(false);
    setShowUpload(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Documents</h1>
          <p className="text-gray-500 mt-1">Upload and manage your documents</p>
        </div>
        <Button onClick={() => setShowUpload(true)} className="gap-2">
          <Upload className="h-4 w-4" />
          Upload Document
        </Button>
      </div>

      {showUpload && (
        <Card padding="md">
          <CardHeader title="Upload New Document" />
          <CardContent>
            <form onSubmit={handleUpload} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Select
                  label="Document Type"
                  options={documentTypes}
                  placeholder="Select document type"
                  required
                />
                <Input label="File" type="file" accept=".pdf,.jpg,.jpeg,.png" required />
              </div>
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setShowUpload(false)}>Cancel</Button>
                <Button type="submit" loading={uploading}>Upload</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <div className="space-y-4">
        {documents.map((doc) => (
          <DocumentCard key={doc.id} doc={doc} />
        ))}
      </div>

      {documents.length === 0 && !showUpload && (
        <Card padding="lg" className="text-center">
          <CardContent>
            <Upload className="h-12 w-12 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No documents uploaded</h3>
            <p className="text-gray-500 mb-4">Upload your documents to check eligibility and prepare applications</p>
            <Button onClick={() => setShowUpload(true)} className="gap-2">
              <Upload className="h-4 w-4" />
              Upload First Document
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

interface DocumentCardProps {
  doc: typeof documents[0];
}

function DocumentCard({ doc }: DocumentCardProps) {
  const statusConfig = {
    verified: { label: 'Verified', className: 'bg-green-100 text-green-700', icon: CheckCircle },
    mismatch: { label: 'Mismatch Detected', className: 'bg-yellow-100 text-yellow-700', icon: AlertTriangle },
    pending: { label: 'Pending Review', className: 'bg-blue-100 text-blue-700', icon: Loader2 },
  };

  const status = statusConfig[doc.status as keyof typeof statusConfig];

  return (
    <Card padding="md">
      <CardContent>
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-4 flex-1 min-w-0">
            <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <FileText className="h-6 w-6 text-gray-600" />
            </div>
            <div className="min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <h3 className="font-medium text-gray-900 truncate">{doc.name}</h3>
                <span className={`px-2 py-1 text-xs font-medium rounded-full ${status.className} flex items-center gap-1`}>
                  <status.icon className="h-3 w-3" />
                  {status.label}
                </span>
              </div>
              <p className="text-sm text-gray-500 truncate">{doc.fileName}</p>
              <p className="text-xs text-gray-400">Uploaded {doc.uploadedAt}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {doc.extractedFields && (
              <Button variant="ghost" size="sm" className="gap-1">
                <Eye className="h-4 w-4" /> View Extracted
              </Button>
            )}
            <Button variant="ghost" size="sm" className="gap-1">
              <Download className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="sm" className="gap-1 text-red-600 hover:bg-red-50">
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {doc.mismatch && (
          <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
            <div className="flex items-center gap-2 text-yellow-800 mb-2">
              <AlertTriangle className="h-5 w-5" />
              <span className="font-medium">Information mismatch detected</span>
            </div>
            <div className="grid grid-cols-3 gap-2 text-sm">
              <div className="text-gray-600">Field: <span className="font-medium text-gray-900">{doc.mismatch.field}</span></div>
              <div className="text-gray-600">Profile: <span className="font-medium text-gray-900">{doc.mismatch.profile}</span></div>
              <div className="text-gray-600">Document: <span className="font-medium text-gray-900">{doc.mismatch.document}</span></div>
            </div>
            <Button variant="outline" size="sm" className="mt-2 w-full">
              Review Mismatch
            </Button>
          </div>
        )}

        {doc.extractedFields && (
          <details className="mt-4">
            <summary className="text-sm text-primary-600 hover:text-primary-700 cursor-pointer">
              View Extracted Fields
            </summary>
            <div className="mt-2 p-3 bg-gray-50 rounded-lg text-sm">
              <pre className="text-gray-700">{JSON.stringify(doc.extractedFields, null, 2)}</pre>
            </div>
          </details>
        )}
      </CardContent>
    </Card>
  );
}