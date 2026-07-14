import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { extractErrorMessage } from '../api/services.js';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', fullName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
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
        <span className="pill mono" style={{ marginBottom: 16, display: 'inline-block' }}>§ New Ledger Entry</span>
        <h1>Create account</h1>
        <p className="sub">Register to open and manage your bank accounts.</p>

        {error && <div className="alert">{error}</div>}

        <form className="form" onSubmit={handleSubmit}>
          <div className="field">
            <label>Full Name</label>
            <input name="fullName" value={form.fullName} onChange={handleChange} required />
          </div>
          <div className="field">
            <label>Username</label>
            <input name="username" value={form.username} onChange={handleChange} required minLength={4} />
            <div className="hint">At least 4 characters.</div>
          </div>
          <div className="field">
            <label>Email</label>
            <input name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="field">
            <label>Password</label>
            <input name="password" type="password" value={form.password} onChange={handleChange} required minLength={6} />
            <div className="hint">At least 6 characters.</div>
          </div>
          <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Creating…' : 'Create Account'}
          </button>
        </form>

        <p className="auth-switch">
          Already registered? <Link to="/login">Sign in →</Link>
        </p>
      </div>
    </div>
  );
}
