import React, { useEffect, useState } from 'react';
import { loanApi, extractErrorMessage } from '../api/services.js';

const STATUS_CLASS = { PENDING: 'frozen', APPROVED: '', REJECTED: 'closed', CLOSED: 'closed' };

export default function Loans() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ loanType: 'PERSONAL', principal: '', tenureMonths: '12' });
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setLoans(await loanApi.myLoans());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleApply = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await loanApi.apply({
        loanType: form.loanType,
        principal: parseFloat(form.principal),
        tenureMonths: parseInt(form.tenureMonths, 10)
      });
      setShowForm(false);
      setForm({ loanType: 'PERSONAL', principal: '', tenureMonths: '12' });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 08 · Lending</span>
          <h1>Loans</h1>
          <p>Apply for personal, home, or vehicle loans. EMI is calculated instantly using the standard reducing-balance formula.</p>
        </div>

        {error && <div className="alert">{error}</div>}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ Apply for a Loan'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleApply} style={{ maxWidth: '100%', display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 160 }}>
                <label>Loan Type</label>
                <select value={form.loanType} onChange={(e) => setForm({ ...form, loanType: e.target.value })}>
                  <option value="PERSONAL">Personal</option>
                  <option value="HOME">Home</option>
                  <option value="VEHICLE">Vehicle</option>
                </select>
              </div>
              <div className="field" style={{ marginBottom: 0, minWidth: 160 }}>
                <label>Principal Amount</label>
                <input type="number" min="1000" step="100" value={form.principal} onChange={(e) => setForm({ ...form, principal: e.target.value })} required />
              </div>
              <div className="field" style={{ marginBottom: 0, minWidth: 140 }}>
                <label>Tenure (months)</label>
                <input type="number" min="3" value={form.tenureMonths} onChange={(e) => setForm({ ...form, tenureMonths: e.target.value })} required />
              </div>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Apply'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading loans…</div>
        ) : loans.length === 0 ? (
          <div className="empty-state">No loan applications yet.</div>
        ) : (
          loans.map((loan) => (
            <div className="ledger-entry" key={loan.id}>
              <div className="ledger-head">
                <span className="ledger-id">{loan.loanType} LOAN · Applied {new Date(loan.appliedAt).toLocaleDateString()}</span>
                <span className={`status-badge ${STATUS_CLASS[loan.status]}`}>{loan.status}</span>
              </div>
              <div className="ledger-body">
                <h3>₹{Number(loan.principal).toLocaleString()} over {loan.tenureMonths} months</h3>
                <div className="tags">
                  <span className="tag">Rate {loan.interestRate}% p.a.</span>
                  <span className="tag">EMI ₹{Number(loan.emiAmount).toFixed(2)}</span>
                  {loan.status === 'APPROVED' && <span className="tag">Remaining ₹{Number(loan.remainingBalance).toFixed(2)}</span>}
                </div>
                {loan.status === 'REJECTED' && loan.rejectionReason && (
                  <p style={{ color: 'var(--danger)', fontSize: '0.88rem' }}>Reason: {loan.rejectionReason}</p>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
