import React, { useEffect, useState } from 'react';
import { fixedDepositApi, accountApi, extractErrorMessage } from '../api/services.js';

export default function FixedDeposits() {
  const [deposits, setDeposits] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ accountNumber: '', principal: '', tenureMonths: '12' });
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [fds, accs] = await Promise.all([fixedDepositApi.myDeposits(), accountApi.myAccounts()]);
      setDeposits(fds);
      setAccounts(accs);
      if (!form.accountNumber && accs.length) {
        setForm((f) => ({ ...f, accountNumber: accs[0].accountNumber }));
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(''); setSuccess('');
    setSubmitting(true);
    try {
      await fixedDepositApi.create({
        accountNumber: form.accountNumber,
        principal: parseFloat(form.principal),
        tenureMonths: parseInt(form.tenureMonths, 10)
      });
      setShowForm(false);
      setForm((f) => ({ ...f, principal: '' }));
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = async (id) => {
    if (!window.confirm('Close this FD prematurely? A penalty applies to the earned interest.')) return;
    setError(''); setSuccess('');
    try {
      await fixedDepositApi.close(id);
      setSuccess('Fixed deposit closed and payout credited.');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 09 · Savings</span>
          <h1>Fixed Deposits</h1>
          <p>Lock in funds from an account for a fixed term at 7.25% p.a. Premature closure applies a penalty to earned interest.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)} disabled={accounts.length === 0}>
            {showForm ? 'Cancel' : '+ Open Fixed Deposit'}
          </button>
        </div>

        {accounts.length === 0 && !loading && (
          <div className="empty-state">Open a savings/current account first before creating a fixed deposit.</div>
        )}

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleCreate} style={{ maxWidth: '100%', display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 200 }}>
                <label>Source Account</label>
                <select value={form.accountNumber} onChange={(e) => setForm({ ...form, accountNumber: e.target.value })}>
                  {accounts.map((a) => (
                    <option value={a.accountNumber} key={a.id}>{a.accountNumber} (₹{Number(a.balance).toFixed(2)})</option>
                  ))}
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
                {submitting ? 'Opening…' : 'Open FD'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading fixed deposits…</div>
        ) : deposits.length === 0 ? (
          <div className="empty-state">No fixed deposits yet.</div>
        ) : (
          deposits.map((fd) => (
            <div className="ledger-entry" key={fd.id}>
              <div className="ledger-head">
                <span className="ledger-id">FD #{fd.id} · {fd.linkedAccountNumber}</span>
                <span className={`status-badge ${fd.status === 'ACTIVE' ? '' : 'closed'}`}>{fd.status.replace('_', ' ')}</span>
              </div>
              <div className="ledger-body">
                <h3>₹{Number(fd.principal).toLocaleString()} for {fd.tenureMonths} months</h3>
                <div className="tags">
                  <span className="tag">Rate {fd.interestRate}% p.a.</span>
                  <span className="tag">Maturity ₹{Number(fd.maturityAmount).toFixed(2)}</span>
                  <span className="tag">Matures {new Date(fd.maturityDate).toLocaleDateString()}</span>
                </div>
                {fd.status === 'ACTIVE' && (
                  <div className="entry-actions">
                    <button className="btn btn-danger btn-sm" onClick={() => handleClose(fd.id)}>Close Prematurely</button>
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
