import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { extractErrorMessage } from '../api/services.js';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form.username, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <span className="pill mono" style={{ marginBottom: 16, display: 'inline-block' }}>§ Access Ledger</span>
        <h1>Sign in</h1>
        <p className="sub">Enter your credentials to access your accounts.</p>

        {error && <div className="alert">{error}</div>}

        <form className="form" onSubmit={handleSubmit}>
          <div className="field">
            <label>Username</label>
            <input name="username" value={form.username} onChange={handleChange} autoComplete="username" required />
          </div>
          <div className="field">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={handleChange} autoComplete="current-password" required />
            <div className="hint"><Link to="/forgot-password">Forgot password?</Link></div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className="auth-switch">
          New here? <Link to="/register">Create an account →</Link>
        </p>
        <p className="auth-switch mono" style={{ fontSize: '0.75rem', opacity: 0.6 }}>
          Seeded admin: admin / admin123
        </p>
      </div>
    </div>
  );
}
