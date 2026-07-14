import React, { useEffect, useState } from 'react';
import { ticketApi, extractErrorMessage } from '../api/services.js';

const STATUS_CLASS = { OPEN: 'frozen', IN_PROGRESS: 'frozen', RESOLVED: '', CLOSED: 'closed' };

export default function SupportTickets() {
  const [tickets, setTickets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ subject: '', description: '' });
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      setTickets(await ticketApi.myTickets());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleRaise = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await ticketApi.raise(form);
      setShowForm(false);
      setForm({ subject: '', description: '' });
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
          <span className="eyebrow">§ 12 · Help</span>
          <h1>Support Tickets</h1>
          <p>Raise an issue and track its status here.</p>
        </div>

        {error && <div className="alert">{error}</div>}

        <div style={{ marginBottom: 24, display: 'flex', justifyContent: 'flex-end' }}>
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : '+ Raise a Ticket'}
          </button>
        </div>

        {showForm && (
          <div className="card" style={{ padding: 24, marginBottom: 28 }}>
            <form className="form" onSubmit={handleRaise}>
              <div className="field">
                <label>Subject</label>
                <input value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} required maxLength={150} />
              </div>
              <div className="field">
                <label>Description</label>
                <textarea
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  required
                  maxLength={2000}
                  rows={5}
                  style={{ width: '100%', padding: '11px 14px', background: 'var(--surface-2)', border: '1px solid var(--line)', color: 'var(--paper)', borderRadius: 'var(--radius)', fontFamily: 'inherit', fontSize: '0.95rem', resize: 'vertical' }}
                />
              </div>
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Raise Ticket'}
              </button>
            </form>
          </div>
        )}

        {loading ? (
          <div className="loader">Loading tickets…</div>
        ) : tickets.length === 0 ? (
          <div className="empty-state">No support tickets yet.</div>
        ) : (
          tickets.map((t) => (
            <div className="ledger-entry" key={t.id}>
              <div className="ledger-head">
                <span className="ledger-id">Ticket #{t.id} · {new Date(t.createdAt).toLocaleDateString()}</span>
                <span className={`status-badge ${STATUS_CLASS[t.status]}`}>{t.status.replace('_', ' ')}</span>
              </div>
              <div className="ledger-body">
                <h3>{t.subject}</h3>
                <p>{t.description}</p>
                {t.resolutionNote && (
                  <p style={{ color: 'var(--success)', fontSize: '0.88rem' }}>Resolution: {t.resolutionNote}</p>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
