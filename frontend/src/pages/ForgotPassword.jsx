import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi, extractErrorMessage } from '../api/services.js';

export default function ForgotPassword() {
  const navigate = useNavigate();
  const [step, setStep] = useState('request'); // 'request' | 'reset'
  const [username, setUsername] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [devOtp, setDevOtp] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRequestOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authApi.forgotPassword(username);
      setDevOtp(res.devOnlyOtp || '');
      setStep('reset');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await authApi.resetPassword({ username, otp, newPassword });
      setSuccess('Password reset. You can now sign in with your new password.');
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-shell">
      <div className="auth-card">
        <span className="pill mono" style={{ marginBottom: 16, display: 'inline-block' }}>§ Account Recovery</span>
        <h1>Forgot password</h1>
        <p className="sub">
          {step === 'request'
            ? "Enter your username and we'll generate a one-time code to reset your password."
            : 'Enter the OTP and choose a new password.'}
        </p>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        {devOtp && step === 'reset' && (
          <div className="alert success" style={{ marginBottom: 20 }}>
            Demo mode — no real SMS/email is sent. Your OTP is: <strong className="mono">{devOtp}</strong>
          </div>
        )}

        {step === 'request' ? (
          <form className="form" onSubmit={handleRequestOtp}>
            <div className="field">
              <label>Username</label>
              <input value={username} onChange={(e) => setUsername(e.target.value)} required autoFocus />
            </div>
            <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
              {loading ? 'Sending…' : 'Send OTP'}
            </button>
          </form>
        ) : (
          <form className="form" onSubmit={handleReset}>
            <div className="field">
              <label>OTP</label>
              <input value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} />
            </div>
            <div className="field">
              <label>New Password</label>
              <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={6} />
              <div className="hint">At least 6 characters.</div>
            </div>
            <button className="btn btn-primary" type="submit" disabled={loading} style={{ width: '100%' }}>
              {loading ? 'Resetting…' : 'Reset Password'}
            </button>
          </form>
        )}

        <p className="auth-switch">
          <Link to="/login">← Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}
