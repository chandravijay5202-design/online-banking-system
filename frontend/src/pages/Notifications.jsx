import React, { useEffect, useState } from 'react';
import { notificationApi, extractErrorMessage } from '../api/services.js';

const TYPE_LABEL = {
  LOGIN_ALERT: 'Login', DEBIT_ALERT: 'Debit', CREDIT_ALERT: 'Credit', LOAN_UPDATE: 'Loan',
  CARD_UPDATE: 'Card', FD_UPDATE: 'Fixed Deposit', SUPPORT_UPDATE: 'Support', SECURITY_ALERT: 'Security'
};

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      setNotifications(await notificationApi.list());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const markRead = async (id) => {
    try {
      await notificationApi.markRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 11 · Alerts</span>
          <h1>Notifications</h1>
          <p>Every deposit, withdrawal, transfer, loan update, and security event lands here.</p>
        </div>

        {error && <div className="alert">{error}</div>}

        {loading ? (
          <div className="loader">Loading notifications…</div>
        ) : notifications.length === 0 ? (
          <div className="empty-state">No notifications yet.</div>
        ) : (
          <div className="card">
            {notifications.map((n, idx) => (
              <div
                key={n.id}
                style={{
                  padding: '16px 22px',
                  borderBottom: idx === notifications.length - 1 ? 'none' : '1px solid var(--line)',
                  display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'flex-start',
                  opacity: n.read ? 0.6 : 1
                }}
              >
                <div>
                  <div className="tags" style={{ marginBottom: 6 }}>
                    <span className="tag">{TYPE_LABEL[n.type] || n.type}</span>
                    {!n.read && <span className="tag" style={{ color: 'var(--gold)', borderColor: 'var(--gold)' }}>NEW</span>}
                  </div>
                  <p style={{ color: 'var(--paper)', fontSize: '0.92rem' }}>{n.message}</p>
                  <p className="mono" style={{ color: 'var(--paper-dim)', fontSize: '0.75rem', marginTop: 4 }}>
                    {new Date(n.createdAt).toLocaleString()}
                  </p>
                </div>
                {!n.read && (
                  <button className="btn btn-ghost btn-sm" onClick={() => markRead(n.id)} style={{ flexShrink: 0 }}>
                    Mark read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
