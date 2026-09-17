import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { CheckCircle, AlertTriangle, Loader2, Mail } from 'lucide-react';
import { authService } from '../services/auth';
import { Button } from '../components/common/Button';
import { Input } from '../components/common/Input';
import { Card } from '../components/common/Card';

type Status = 'pending' | 'verifying' | 'success' | 'error';

export default function VerifyEmail() {
  const [params] = useSearchParams();
  const [status, setStatus] = useState<Status>('pending');
  const [message, setMessage] = useState('');
  const [email, setEmail] = useState('');
  const [resending, setResending] = useState(false);
  const [resendMessage, setResendMessage] = useState('');

  const token = params.get('token') ?? '';

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('No verification token found in the link.');
      return;
    }
    let cancelled = false;
    const verify = async () => {
      setStatus('verifying');
      try {
        const msg = await authService.verifyEmail(token);
        if (!cancelled) {
          setStatus('success');
          setMessage(msg || 'Email verified successfully. You can now log in.');
        }
      } catch (err: any) {
        if (!cancelled) {
          setStatus('error');
          setMessage(
            err.response?.data?.message || 'Verification failed. The link may be invalid or expired.'
          );
        }
      }
    };
    verify();
    return () => {
      cancelled = true;
    };
  }, [token]);

  const handleResend = async (e: React.FormEvent) => {
    e.preventDefault();
    setResendMessage('');
    setResending(true);
    try {
      const msg = await authService.resendVerification(email);
      setResendMessage(msg || 'Verification email sent. Please check your inbox.');
    } catch (err: any) {
      setResendMessage(err.response?.data?.message || 'Could not resend the email. Please try again.');
    } finally {
      setResending(false);
    }
  };

  return (
    <Card padding="lg">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Verify your email</h2>
        <p className="text-gray-500 mt-1">Confirm your account to continue</p>
      </div>

      {status === 'verifying' && (
        <div className="flex flex-col items-center gap-3 py-6" role="status">
          <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
          <p className="text-gray-600">Verifying your email…</p>
        </div>
      )}

      {status === 'success' && (
        <div className="p-4 bg-green-50 border border-green-200 rounded-lg text-green-700 text-sm flex gap-2" role="alert">
          <CheckCircle className="h-5 w-5 flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {status === 'error' && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm flex gap-2" role="alert">
          <AlertTriangle className="h-5 w-5 flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {(status === 'success' || status === 'error') && (
        <div className="mt-6 text-center">
          <Link to="/login" className="text-primary-600 hover:text-primary-700 font-medium text-sm">
            Go to sign in →
          </Link>
        </div>
      )}

      <div className="mt-8 pt-6 border-t border-gray-100">
        <h3 className="text-sm font-medium text-gray-900 mb-3 flex items-center gap-2">
          <Mail className="h-4 w-4" /> Didn&apos;t get the email?
        </h3>
        <form onSubmit={handleResend} className="space-y-3">
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter your registered email"
            required
            autoComplete="email"
          />
          <Button type="submit" variant="outline" className="w-full" loading={resending}>
            Resend verification email
          </Button>
        </form>
        {resendMessage && <p className="mt-3 text-sm text-gray-600">{resendMessage}</p>}
      </div>
    </Card>
  );
}
