import { ReactNode } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/common/Sidebar';
import { Header } from '../components/common/Header';

export const MainLayout: React.FC = () => (
  <div className="min-h-screen bg-gray-50 lg:flex lg:items-start">
    <Sidebar />
    <div className="flex-1 min-w-0">
      <Header />
      <main className="p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full">
        <Outlet />
      </main>
    </div>
  </div>
);

export const AuthLayout: React.FC<{ children: ReactNode }> = ({ children }) => (
  <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
    <div className="w-full max-w-md">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Government Scheme Assistant</h1>
        <p className="text-gray-500 mt-2">Discover and apply for government schemes</p>
      </div>
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 md:p-8">
        {children}
      </div>
      <p className="text-center text-sm text-gray-500 mt-6">
        © 2024 Government Scheme Assistant. All rights reserved.
      </p>
    </div>
  </div>
);