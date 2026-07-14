import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { accountApi, transactionApi, statementApi, extractErrorMessage } from '../api/services.js';

export default function AccountDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [busy, setBusy] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [acc, txns] = await Promise.all([
        accountApi.getById(id),
        transactionApi.history(id)
      ]);
      setAccount(acc);
      setHistory(txns);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const doAction = async (action) => {
    setError('');
    setSuccess('');
    if (!amount || Number(amount) <= 0) {
      setError('Enter an amount greater than zero.');
      return;
    }
    setBusy(true);
    try {
      const payload = { amount: parseFloat(amount), description };
      if (action === 'deposit') await accountApi.deposit(id, payload);
      else await accountApi.withdraw(id, payload);
      setSuccess(`${action === 'deposit' ? 'Deposit' : 'Withdrawal'} successful.`);
      setAmount('');
      setDescription('');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const handleClose = async () => {
    if (!window.confirm('Close this account? Balance must be zero.')) return;
    setError('');
    try {
      await accountApi.close(id);
      navigate('/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleDownloadStatement = async () => {
    setError('');
    try {
      await statementApi.download(id);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  if (loading) return <div className="page"><div className="wrap"><div className="loader">Loading account…</div></div></div>;
  if (!account) return <div className="page"><div className="wrap"><div className="alert">{error || 'Account not found.'}</div></div></div>;

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 02 · Account Detail</span>
          <h1>{account.accountNumber}</h1>
          <p>{account.accountType} account · owner {account.ownerUsername}</p>
          <div style={{ marginTop: 14 }}>
            <button className="btn btn-ghost btn-sm" onClick={handleDownloadStatement}>Download Statement (PDF) →</button>
          </div>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        <div className="card" style={{ padding: 24, marginBottom: 28 }}>
          <div className="balance">₹{Number(account.balance).toFixed(2)} <small>current balance</small></div>
          <div className="tags">
            <span className="tag">{account.accountType}</span>
            <span className="tag">{account.status}</span>
            <span className="tag">IFSC {account.ifscCode}</span>
            <span className="tag">MICR {account.micrCode}</span>
            <span className="tag">{account.branchName}, {account.city}</span>
          </div>

          <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginTop: 20, alignItems: 'flex-end' }}>
            <div className="field" style={{ marginBottom: 0, minWidth: 160 }}>
              <label>Amount</label>
              <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
            </div>
            <div className="field" style={{ marginBottom: 0, minWidth: 220, flex: 1 }}>
              <label>Description (optional)</label>
              <input value={description} onChange={(e) => setDescription(e.target.value)} placeholder="e.g. Salary credit" />
            </div>
            <button className="btn btn-primary" onClick={() => doAction('deposit')} disabled={busy || account.status !== 'ACTIVE'}>
              Deposit
            </button>
            <button className="btn btn-ghost" onClick={() => doAction('withdraw')} disabled={busy || account.status !== 'ACTIVE'}>
              Withdraw
            </button>
          </div>

          {account.status === 'ACTIVE' && Number(account.balance) === 0 && (
            <div style={{ marginTop: 20 }}>
              <button className="btn btn-danger btn-sm" onClick={handleClose}>Close Account</button>
            </div>
          )}
        </div>

        <div className="page-head" style={{ marginBottom: 20 }}>
          <span className="eyebrow">§ 03 · Transaction History</span>
          <h1 style={{ fontSize: '1.4rem' }}>Recent Activity</h1>
        </div>

        {history.length === 0 ? (
          <div className="empty-state">No transactions yet.</div>
        ) : (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>Reference</th>
                  <th>Type</th>
                  <th>From</th>
                  <th>To</th>
                  <th>Amount</th>
                  <th>Mode</th>
                  <th>Status</th>
                  <th>When</th>
                </tr>
              </thead>
              <tbody>
                {history.map((t) => {
                  const isCredit = t.toAccountNumber === account.accountNumber;
                  return (
                    <tr key={t.id}>
                      <td className="mono">{t.reference}</td>
                      <td>{t.type}</td>
                      <td className="mono">{t.fromAccountNumber || '—'}</td>
                      <td className="mono">{t.toAccountNumber || '—'}</td>
                      <td className={`mono txn-amount ${isCredit ? 'credit' : 'debit'}`}>
                        {isCredit ? '+' : '−'}₹{Number(t.amount).toFixed(2)}
                      </td>
                      <td className="mono">{t.mode || '—'}</td>
                      <td>{t.status}</td>
                      <td className="mono">{new Date(t.timestamp).toLocaleString()}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
