import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { accountApi, branchApi, extractErrorMessage } from '../api/services.js';

export default function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ accountType: 'SAVINGS', openingBalance: '0', branch: '' });
  const [submitting, setSubmitting] = useState(false);

  const loadAccounts = async () => {
    setLoading(true);
    try {
      const [accs, branchList] = await Promise.all([accountApi.myAccounts(), branchApi.list()]);
      setAccounts(accs);
      setBranches(branchList);
      if (!form.branch && branchList.length) {
        setForm((f) => ({ ...f, branch: branchList[0].code }));
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await accountApi.create({
        accountType: form.accountType,
        openingBalance: parseFloat(form.openingBalance || '0'),
        branch: form.branch || undefined
      });
      setShowForm(false);
      setForm((f) => ({ ...f, openingBalance: '0' }));
      loadAccounts();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const totalBalance = accounts.reduce((sum, a) => sum + Number(a.balance), 0);
  const activeCount = accounts.filter((a) => a.status === 'ACTIVE').length;

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 01 · Your Ledger</span>
          <h1>Accounts</h1>
          <p>Manage your savings and current accounts, view balances, and access transaction history.</p>
        </div>

        <div className="stat-grid">
          <div className="stat">
            <div className="k">Total Balance</div>
            <div className="v">₹{totalBalance.toFixed(2)}</div>
          </div>
          <div className="stat">
            <div className="k">Total Accounts</div>
            <div className="v">{accounts.length}</div>
          </div>
          <div className="stat">
            <div className="k">Active Accounts</div>
            <div className="v">{activeCount}</div>
          </div>
        </div>

        {error && <div className="alert">{error}</div>}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ Open New Account'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleCreate} style={{ maxWidth: '100%', display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 180 }}>
                <label>Account Type</label>
                <select value={form.accountType} onChange={(e) => setForm({ ...form, accountType: e.target.value })}>
                  <option value="SAVINGS">Savings</option>
                  <option value="CURRENT">Current</option>
                </select>
              </div>
              <div className="field" style={{ marginBottom: 0, minWidth: 180 }}>
                <label>Opening Balance</label>
                <input
                  type="number" min="0" step="0.01"
                  value={form.openingBalance}
                  onChange={(e) => setForm({ ...form, openingBalance: e.target.value })}
                />
              </div>
              <div className="field" style={{ marginBottom: 0, minWidth: 220 }}>
                <label>Branch</label>
                <select value={form.branch} onChange={(e) => setForm({ ...form, branch: e.target.value })}>
                  {branches.map((b) => (
                    <option value={b.code} key={b.code}>{b.branchName}, {b.city}</option>
                  ))}
                </select>
              </div>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Opening…' : 'Open Account'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading accounts…</div>
        ) : accounts.length === 0 ? (
          <div className="empty-state">No accounts yet. Open your first account above.</div>
        ) : (
          accounts.map((acc) => (
            <div className="ledger-entry" key={acc.id}>
              <div className="ledger-head">
                <span className="ledger-id">{acc.accountNumber} · {acc.accountType}</span>
                <span className={`status-badge ${acc.status.toLowerCase()}`}>{acc.status}</span>
              </div>
              <div className="ledger-body">
                <h3>{acc.accountType === 'SAVINGS' ? 'Savings Account' : 'Current Account'}</h3>
                <div className="balance">₹{Number(acc.balance).toFixed(2)} <small>available balance</small></div>
                <div className="tags">
                  <span className="tag">IFSC {acc.ifscCode}</span>
                  <span className="tag">{acc.branchName}, {acc.city}</span>
                  <span className="tag">MICR {acc.micrCode}</span>
                </div>
                <div className="entry-actions">
                  <Link className="btn btn-ghost btn-sm" to={`/accounts/${acc.id}`}>View & Manage →</Link>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
