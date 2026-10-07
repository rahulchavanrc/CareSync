import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import PulseLine from '../components/PulseLine';

export default function Login() {
  const { login, loading, error } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const user = await login(form);
      const dest = user.role === 'DOCTOR' ? '/schedule' : user.role === 'ADMIN' ? '/admin' : '/doctors';
      navigate(dest);
    } catch {
      // error already surfaced via auth context
    }
  };

  return (
    <div className="page auth-page">
      <div className="container auth-container">
        <div className="auth-panel card">
          <PulseLine className="auth-pulse" />
          <h2>Welcome back</h2>
          <p className="text-soft">Log in to manage your appointments and records.</p>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                name="email"
                type="email"
                required
                autoComplete="email"
                value={form.email}
                onChange={handleChange}
                placeholder="you@example.com"
              />
            </div>
            <div className="field">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                name="password"
                type="password"
                required
                autoComplete="current-password"
                value={form.password}
                onChange={handleChange}
                placeholder="••••••••"
              />
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Logging in…' : 'Log in'}
            </button>
          </form>

          <p className="auth-switch text-soft">
            New to CareSync? <Link to="/register">Create a patient account</Link>
          </p>
        </div>
      </div>

      <style>{`
        .auth-page { display: flex; align-items: center; }
        .auth-container { display: flex; justify-content: center; }
        .auth-panel { width: 100%; max-width: 420px; padding: 36px; }
        .auth-pulse { width: 100%; height: 44px; margin-bottom: 8px; }
        .auth-switch { margin-top: 18px; margin-bottom: 0; font-size: 0.88rem; text-align: center; }
      `}</style>
    </div>
  );
}
