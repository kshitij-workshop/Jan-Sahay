import { Bell, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const Header: React.FC = () => {
  const { user } = useAuth();

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-gray-200">
      <div className="flex items-center justify-between h-16 px-4 md:px-6">
        <div className="flex items-center gap-4">
          <h2 className="text-lg font-semibold text-gray-900">Government Scheme Assistant</h2>
        </div>

        <div className="flex items-center gap-4">
          <button className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors" aria-label="Notifications">
            <Bell className="h-5 w-5 text-gray-600" />
            <span className="absolute top-1 right-1 h-4 w-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
              3
            </span>
          </button>

          <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
            <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center">
              <User className="h-5 w-5 text-primary-600" />
            </div>
            <div className="hidden md:block text-right">
              <p className="text-sm font-medium text-gray-900">{user?.fullName || 'User'}</p>
              <p className="text-xs text-gray-500">{user?.role === 'ADMIN' ? 'Administrator' : 'Citizen'}</p>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};