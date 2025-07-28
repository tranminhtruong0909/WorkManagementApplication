import React, { useState } from 'react';
import { register as registerApi } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Register = () => {
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    avatarUrl: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    // Kiểm tra dữ liệu đầu vào
    if (!form.name || !form.email || !form.password) {
      setError('Vui lòng nhập đầy đủ thông tin');
      setLoading(false);
      return;
    }

    if (form.password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      setLoading(false);
      return;
    }

    if (form.password !== form.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      setLoading(false);
      return;
    }

    try {
      const response = await registerApi({
        name: form.name,
        email: form.email,
        password: form.password,
        avatarUrl: form.avatarUrl,
      });
      // Lưu token nếu backend trả về
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
      }
      setSuccess('Đăng ký thành công! Đang chuyển đến trang đăng nhập...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (error) {
      setError(error?.response?.data || 'Đăng ký thất bại');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-200 to-purple-300">
      <div className="max-w-md w-full p-8 bg-white rounded-2xl shadow-2xl border border-blue-200">
        <h1 className="text-3xl font-bold mb-6 text-center text-blue-800">Đăng ký tài khoản</h1>
        {error && (
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 mb-6 rounded-r-lg animate-pulse">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-100 border-l-4 border-green-500 text-green-700 p-4 mb-6 rounded-r-lg animate-pulse">
            {success}
          </div>
        )}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="relative">
            <input
              type="text"
              name="name"
              placeholder="Tên của bạn"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={form.name}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>
          <div className="relative">
            <input
              type="email"
              name="email"
              placeholder="Email"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={form.email}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>
          <div className="relative">
            <input
              type="password"
              name="password"
              placeholder="Mật khẩu (tối thiểu 6 ký tự)"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={form.password}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>
          <div className="relative">
            <input
              type="password"
              name="confirmPassword"
              placeholder="Xác nhận mật khẩu"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={form.confirmPassword}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </div>
          <div className="relative">
            <input
              type="text"
              name="avatarUrl"
              placeholder="Avatar URL (tùy chọn)"
              className="w-full border p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-lg transition-all duration-200"
              value={form.avatarUrl}
              onChange={handleChange}
              disabled={loading}
            />
          </div>
          <button
            className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white py-3 rounded-lg font-semibold text-lg transition duration-200 flex items-center justify-center"
            type="submit"
            disabled={loading}
          >
            {loading ? (
              <svg className="animate-spin h-5 w-5 mr-2 text-white" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8h8a8 8 0 01-8 8 8 8 0 01-8-8z" />
              </svg>
            ) : (
              'Đăng ký'
            )}
          </button>
          <div className="text-center mt-4 text-base">
            Đã có tài khoản?{' '}
            <span
              className="text-blue-600 hover:underline cursor-pointer font-semibold"
              onClick={() => navigate('/login')}
            >
              Đăng nhập
            </span>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Register;