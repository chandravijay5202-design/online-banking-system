import React, { useEffect, useState } from 'react';
import { creditCardApi, accountApi, extractErrorMessage } from '../api/services.js';

export default function CreditCards() {
  const [cards, setCards] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [requestedLimit, setRequestedLimit] = useState('50000');
  const [submitting, setSubmitting] = useState(false);
  const [payForm, setPayForm] = useState({}); // { [cardId]: { amount, accountNumber } }

  const load = async () => {
    setLoading(true);
    try {
      const [c, accs] = await Promise.all([creditCardApi.myCards(), accountApi.myAccounts()]);
      setCards(c);
      setAccounts(accs);
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
      await creditCardApi.apply({ requestedLimit: parseFloat(requestedLimit) });
      setShowForm(false);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const toggleBlock = async (card) => {
    setError(''); setSuccess('');
    try {
      if (card.status === 'BLOCKED') await creditCardApi.unblock(card.id);
      else await creditCardApi.block(card.id);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handlePay = async (cardId) => {
    const details = payForm[cardId];
    if (!details?.amount || !details?.accountNumber) {
      setError('Enter an amount and choose a source account.');
      return;
    }
    setError(''); setSuccess('');
    try {
      await creditCardApi.payBill(cardId, { amount: parseFloat(details.amount), fromAccountNumber: details.accountNumber });
      setSuccess('Bill payment successful.');
      setPayForm({ ...payForm, [cardId]: {} });
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 10 · Cards</span>
          <h1>Credit Cards</h1>
          <p>Apply for a card, pay down your balance from any of your accounts, or block/unblock instantly.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ Apply for a Card'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleApply} style={{ maxWidth: '100%', display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <div className="field" style={{ marginBottom: 0, minWidth: 200 }}>
                <label>Requested Credit Limit</label>
                <input type="number" min="5000" step="1000" value={requestedLimit} onChange={(e) => setRequestedLimit(e.target.value)} required />
              </div>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Applying…' : 'Apply'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading cards…</div>
        ) : cards.length === 0 ? (
          <div className="empty-state">No credit cards yet.</div>
        ) : (
          cards.map((card) => (
            <div className="ledger-entry" key={card.id}>
              <div className="ledger-head">
                <span className="ledger-id mono">{card.maskedCardNumber}</span>
                <span className={`status-badge ${card.status === 'APPROVED' ? '' : 'frozen'}`}>{card.status}</span>
              </div>
              <div className="ledger-body">
                <h3>Limit ₹{Number(card.creditLimit).toLocaleString()}</h3>
                <div className="tags">
                  <span className="tag">Outstanding ₹{Number(card.outstandingBalance).toFixed(2)}</span>
                  <span className="tag">Available ₹{Number(card.availableCredit).toFixed(2)}</span>
                </div>

                {card.status === 'APPROVED' && (
                  <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginTop: 14, alignItems: 'flex-end' }}>
                    <div className="field" style={{ marginBottom: 0, minWidth: 130 }}>
                      <label>Amount</label>
                      <input
                        type="number" min="0.01" step="0.01"
                        value={payForm[card.id]?.amount || ''}
                        onChange={(e) => setPayForm({ ...payForm, [card.id]: { ...payForm[card.id], amount: e.target.value } })}
                      />
                    </div>
                    <div className="field" style={{ marginBottom: 0, minWidth: 180 }}>
                      <label>From Account</label>
                      <select
                        value={payForm[card.id]?.accountNumber || ''}
                        onChange={(e) => setPayForm({ ...payForm, [card.id]: { ...payForm[card.id], accountNumber: e.target.value } })}
                      >
                        <option value="">Select…</option>
                        {accounts.map((a) => (
                          <option value={a.accountNumber} key={a.id}>{a.accountNumber}</option>
                        ))}
                      </select>
                    </div>
                    <button className="btn btn-primary btn-sm" onClick={() => handlePay(card.id)}>Pay Bill</button>
                  </div>
                )}

                {(card.status === 'APPROVED' || card.status === 'BLOCKED') && (
                  <div className="entry-actions">
                    <button className="btn btn-ghost btn-sm" onClick={() => toggleBlock(card)}>
                      {card.status === 'BLOCKED' ? 'Unblock Card' : 'Block Card'}
                    </button>
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
