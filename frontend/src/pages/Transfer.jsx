import React, { useEffect, useState } from 'react';
import { transactionApi, beneficiaryApi, extractErrorMessage } from '../api/services.js';

const MODE_HINTS = {
  SAME_BANK: 'Instant, no limit — within this bank.',
  IMPS: 'Instant, capped at ₹5,00,000 per transaction.',
  NEFT: 'Batch settlement, no strict limit for this demo.',
  RTGS: 'For large transfers — minimum ₹2,00,000.'
};

export default function Transfer() {
  const [form, setForm] = useState({ fromAccountNumber: '', toAccountNumber: '', amount: '', mode: 'SAME_BANK', description: '' });
  const [beneficiaries, setBeneficiaries] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    beneficiaryApi.list().then((list) => setBeneficiaries(list.filter((b) => b.active))).catch(() => {});
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const pickBeneficiary = (id) => {
    const b = beneficiaries.find((x) => String(x.id) === id);
    if (b) setForm({ ...form, toAccountNumber: b.accountNumber });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess(null);
    setSubmitting(true);
    try {
      const txn = await transactionApi.transfer({
        ...form,
        amount: parseFloat(form.amount)
      });
      setSuccess(txn);
      setForm({ fromAccountNumber: '', toAccountNumber: '', amount: '', mode: 'SAME_BANK', description: '' });
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
          <span className="eyebrow">§ 04 · Move Funds</span>
          <h1>Transfer</h1>
          <p>Send funds from an account you own to any account by its account number, via SAME BANK, NEFT, RTGS, or IMPS.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && (
          <div className="alert success">
            Transfer complete via {success.mode} — reference <strong className="mono">{success.reference}</strong>, ₹{Number(success.amount).toFixed(2)} moved to {success.toAccountNumber}.
          </div>
        )}

        <div className="card" style={{ padding: 28, maxWidth: 520 }}>
          <form className="form" style={{ maxWidth: '100%' }} onSubmit={handleSubmit}>
            <div className="field">
              <label>From Account Number</label>
              <input name="fromAccountNumber" value={form.fromAccountNumber} onChange={handleChange} required />
              <div className="hint">Must be an account you own.</div>
            </div>

            {beneficiaries.length > 0 && (
              <div className="field">
                <label>Quick-pick a Beneficiary (optional)</label>
                <select onChange={(e) => pickBeneficiary(e.target.value)} defaultValue="">
                  <option value="">— Choose a saved beneficiary —</option>
                  {beneficiaries.map((b) => (
                    <option value={b.id} key={b.id}>{b.nickname} ({b.accountNumber})</option>
                  ))}
                </select>
              </div>
            )}

            <div className="field">
              <label>To Account Number</label>
              <input name="toAccountNumber" value={form.toAccountNumber} onChange={handleChange} required />
            </div>

            <div className="field">
              <label>Transfer Mode</label>
              <select name="mode" value={form.mode} onChange={handleChange}>
                <option value="SAME_BANK">Same Bank</option>
                <option value="NEFT">NEFT</option>
                <option value="RTGS">RTGS</option>
                <option value="IMPS">IMPS</option>
              </select>
              <div className="hint">{MODE_HINTS[form.mode]}</div>
            </div>

            <div className="field">
              <label>Amount</label>
              <input name="amount" type="number" min="0.01" step="0.01" value={form.amount} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>Description (optional)</label>
              <input name="description" value={form.description} onChange={handleChange} placeholder="e.g. Rent" />
            </div>
            <button className="btn btn-primary" type="submit" disabled={submitting} style={{ width: '100%' }}>
              {submitting ? 'Transferring…' : 'Send Transfer'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
