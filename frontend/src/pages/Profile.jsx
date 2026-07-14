import React, { useEffect, useState } from 'react';
import { profileApi, extractErrorMessage } from '../api/services.js';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [profileForm, setProfileForm] = useState({ fullName: '', email: '' });
  const [kycForm, setKycForm] = useState({ panNumber: '', aadhaarNumber: '', documentProvided: true });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingKyc, setSavingKyc] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await profileApi.get();
      setProfile(data);
      setProfileForm({ fullName: data.fullName, email: data.email });
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setSavingProfile(true);
    try {
      const data = await profileApi.update(profileForm);
      setProfile(data);
      setSuccess('Profile updated.');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSavingProfile(false);
    }
  };

  const handleKycSubmit = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setSavingKyc(true);
    try {
      const data = await profileApi.submitKyc(kycForm);
      setProfile(data);
      setSuccess('KYC submitted — pending admin verification.');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSavingKyc(false);
    }
  };

  if (loading) return <div className="page"><div className="wrap"><div className="loader">Loading profile…</div></div></div>;

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 06 · Identity</span>
          <h1>Profile & KYC</h1>
          <p>Manage your account details and identity verification status.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <div className="tags" style={{ marginBottom: 20 }}>
            <span className="tag">Role: {profile.role === 'ROLE_ADMIN' ? 'Admin' : 'Customer'}</span>
            <span className="tag">KYC: {profile.kycStatus.replace('_', ' ')}</span>
            {profile.panNumber && <span className="tag">PAN {profile.panNumber}</span>}
            {profile.aadhaarLast4 && <span className="tag">Aadhaar ****{profile.aadhaarLast4}</span>}
          </div>

          <form className="form" onSubmit={handleProfileSave}>
            <div className="field">
              <label>Full Name</label>
              <input value={profileForm.fullName} onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })} />
            </div>
            <div className="field">
              <label>Email</label>
              <input type="email" value={profileForm.email} onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })} />
            </div>
            <button className="btn btn-primary" type="submit" disabled={savingProfile}>
              {savingProfile ? 'Saving…' : 'Save Profile'}
            </button>
          </form>
        </div>

        {profile.kycStatus !== 'VERIFIED' && (
          <div className="card" style={{ padding: 24 }}>
            <h3 style={{ marginBottom: 6, fontSize: '1.1rem', color: 'var(--paper)' }}>Submit KYC</h3>
            <p style={{ color: 'var(--paper-dim)', fontSize: '0.9rem', marginBottom: 18 }}>
              Required before opening loans, credit cards, or fixed deposits in a real bank. This demo doesn't store real documents — just PAN/Aadhaar numbers for verification simulation.
            </p>
            <form className="form" onSubmit={handleKycSubmit}>
              <div className="field">
                <label>PAN Number</label>
                <input
                  placeholder="ABCDE1234F"
                  value={kycForm.panNumber}
                  onChange={(e) => setKycForm({ ...kycForm, panNumber: e.target.value.toUpperCase() })}
                  required
                />
              </div>
              <div className="field">
                <label>Aadhaar Number</label>
                <input
                  placeholder="12-digit number"
                  value={kycForm.aadhaarNumber}
                  onChange={(e) => setKycForm({ ...kycForm, aadhaarNumber: e.target.value })}
                  maxLength={12}
                  required
                />
              </div>
              <button className="btn btn-primary" type="submit" disabled={savingKyc}>
                {savingKyc ? 'Submitting…' : 'Submit KYC'}
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
