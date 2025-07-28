import React, { useState } from 'react';
import { login as loginApi } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await loginApi(email, password);
      navigate('/home');
    } catch (error) {
      setError(error.response?.data || 'Sai tài khoản hoặc mật khẩu');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-200 to-purple-300">
      <div className="max-w-md w-full p-8 bg-white rounded-2xl shadow-2xl border border-blue-200">
        <h1 className="text-3xl font-bold mb-6 text-center text-blue-800">Đăng nhập</h1>
        {error && (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6 rounded-r-lg animate-pulse">
            {error}
          </div>
        )}
        <form onSubmit={handleLogin} className="flex flex-col gap-4">
          <div className="relative">
            <input
              type="email"
              placeholder="Email"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <div className="relative">
            <input
              type="password"
              placeholder="Mật khẩu"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <button
            className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white py-3 rounded-lg font-semibold text-lg transition duration-200 flex items-center justify-center"
            disabled={loading}
            type="submit"
          >
            {loading ? (
              <svg className="animate-spin h-5 w-5 mr-2 text-white" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8h8a8 8 0 01-8 8 8 8 0 01-8-8z" />
              </svg>
            ) : (
              'Đăng nhập'
            )}
          </button>
          <div className="text-center mt-4 text-base">
            Chưa có tài khoản?{' '}
            <span
              className="text-blue-600 hover:underline cursor-pointer font-semibold"
              onClick={() => navigate('/register')}
            >
              Đăng ký ngay
            </span>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Login;