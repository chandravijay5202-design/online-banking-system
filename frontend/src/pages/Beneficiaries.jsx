import React, { useEffect, useState } from 'react';
import { beneficiaryApi, extractErrorMessage } from '../api/services.js';

export default function Beneficiaries() {
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ nickname: '', beneficiaryName: '', accountNumber: '', ifscCode: '' });
  const [submitting, setSubmitting] = useState(false);
  const [pendingOtp, setPendingOtp] = useState(null); // { beneficiaryId, devOnlyOtp }
  const [otpInput, setOtpInput] = useState('');
  const [verifying, setVerifying] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setBeneficiaries(await beneficiaryApi.list());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setSubmitting(true);
    try {
      const res = await beneficiaryApi.add(form);
      const fresh = await beneficiaryApi.list();
      const newest = fresh.filter((b) => !b.active).sort((a, b2) => b2.id - a.id)[0];
      setPendingOtp({ beneficiaryId: newest?.id, devOnlyOtp: res.devOnlyOtp, message: res.message });
      setBeneficiaries(fresh);
      setShowForm(false);
      setForm({ nickname: '', beneficiaryName: '', accountNumber: '', ifscCode: '' });
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setVerifying(true);
    try {
      await beneficiaryApi.verify(pendingOtp.beneficiaryId, otpInput);
      setSuccess('Beneficiary verified and activated.');
      setPendingOtp(null);
      setOtpInput('');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setVerifying(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this beneficiary?')) return;
    try {
      await beneficiaryApi.remove(id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 07 · Payees</span>
          <h1>Beneficiaries</h1>
          <p>Save frequent transfer recipients. New beneficiaries need OTP approval before you can send them funds.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        {pendingOtp && (
          <div className="card" style={{ padding: 24, marginBottom: 24, borderColor: 'var(--gold)' }}>
            <h3 style={{ color: 'var(--gold)', marginBottom: 6, fontSize: '1.05rem' }}>OTP Verification Required</h3>
            <p style={{ color: 'var(--paper-dim)', fontSize: '0.88rem', marginBottom: 4 }}>{pendingOtp.message}</p>
            <p className="mono" style={{ color: 'var(--paper-dim)', fontSize: '0.8rem', marginBottom: 16 }}>
              Demo mode — real OTP: <strong style={{ color: 'var(--gold)' }}>{pendingOtp.devOnlyOtp}</strong> (would be sent via SMS in production)
            </p>
            <form onSubmit={handleVerify} style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 160 }}>
                <label>Enter OTP</label>
                <input value={otpInput} onChange={(e) => setOtpInput(e.target.value)} maxLength={6} required />
              </div>
              <button className="btn btn-primary" type="submit" disabled={verifying}>
                {verifying ? 'Verifying…' : 'Verify'}
              </button>
            </form>
          </div>
        )}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ Add Beneficiary'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleAdd}>
              <div className="field">
                <label>Nickname</label>
                <input value={form.nickname} onChange={(e) => setForm({ ...form, nickname: e.target.value })} required />
              </div>
              <div className="field">
                <label>Beneficiary Name</label>
                <input value={form.beneficiaryName} onChange={(e) => setForm({ ...form, beneficiaryName: e.target.value })} required />
              </div>
              <div className="field">
                <label>Account Number</label>
                <input value={form.accountNumber} onChange={(e) => setForm({ ...form, accountNumber: e.target.value })} required />
              </div>
              <div className="field">
                <label>IFSC Code</label>
                <input value={form.ifscCode} onChange={(e) => setForm({ ...form, ifscCode: e.target.value.toUpperCase() })} required />
              </div>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Adding…' : 'Add & Request OTP'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading beneficiaries…</div>
        ) : beneficiaries.length === 0 ? (
          <div className="empty-state">No beneficiaries yet. Add one above.</div>
        ) : (
          beneficiaries.map((b) => (
            <div className="ledger-entry" key={b.id}>
              <div className="ledger-head">
                <span className="ledger-id">{b.nickname}</span>
                <span className={`status-badge ${b.active ? '' : 'frozen'}`}>{b.active ? 'ACTIVE' : 'PENDING OTP'}</span>
              </div>
              <div className="ledger-body">
                <h3>{b.beneficiaryName}</h3>
                <div className="tags">
                  <span className="tag mono">{b.accountNumber}</span>
                  <span className="tag mono">IFSC {b.ifscCode}</span>
                </div>
                <div className="entry-actions">
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}>Remove</button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
