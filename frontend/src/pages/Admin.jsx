import React, { useEffect, useState } from 'react';
import { adminApi, extractErrorMessage } from '../api/services.js';

const TABS = [
  { key: 'accounts', label: 'Accounts' },
  { key: 'users', label: 'Users' },
  { key: 'loans', label: 'Loans' },
  { key: 'cards', label: 'Credit Cards' },
  { key: 'tickets', label: 'Support Tickets' },
  { key: 'audit', label: 'Audit Logs' }
];

export default function Admin() {
  const [tab, setTab] = useState('accounts');
  const [accounts, setAccounts] = useState([]);
  const [users, setUsers] = useState([]);
  const [loans, setLoans] = useState([]);
  const [cards, setCards] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadAll = async () => {
    setLoading(true);
    setError('');
    try {
      const [accs, usrs, lns, tkts, logs, crds] = await Promise.all([
        adminApi.allAccounts(),
        adminApi.allUsers(),
        adminApi.allLoans(),
        adminApi.allTickets(),
        adminApi.auditLogs(),
        adminApi.allCards()
      ]);
      setAccounts(accs);
      setUsers(usrs);
      setLoans(lns);
      setTickets(tkts);
      setAuditLogs(logs);
      setCards(crds);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const withFeedback = async (action, successMessage) => {
    setError('');
    setSuccess('');
    try {
      await action();
      setSuccess(successMessage);
      loadAll();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleUnlock = (userId) => withFeedback(() => adminApi.unlockUser(userId), 'Account unlocked.');
  const handleVerifyKyc = (userId) => withFeedback(() => adminApi.verifyKyc(userId), 'KYC verified.');
  const handleRejectKyc = (userId) => {
    const reason = window.prompt('KYC rejection reason (optional):', '');
    if (reason === null) return;
    withFeedback(() => adminApi.rejectKyc(userId, reason), 'KYC rejected.');
  };
  const handleApproveLoan = (id) => withFeedback(() => adminApi.approveLoan(id), 'Loan approved.');
  const handleRejectLoan = (id) => {
    const reason = window.prompt('Loan rejection reason (optional):', '');
    if (reason === null) return;
    withFeedback(() => adminApi.rejectLoan(id, reason), 'Loan rejected.');
  };
  const handleUpdateTicket = (id, status) =>
    withFeedback(() => adminApi.updateTicket(id, { status }), `Ticket marked ${status}.`);
  const handleApproveCard = (id) => withFeedback(() => adminApi.approveCard(id), 'Card approved.');

  return (
    <div className="page">
      <div className="wrap">
        <div className="page-head">
          <span className="eyebrow">§ 05 · Bank Records</span>
          <h1>Admin Panel</h1>
          <p>Full visibility and control across accounts, users, loans, tickets, and audit trail.</p>
        </div>

        {error && <div className="alert">{error}</div>}
        {success && <div className="alert success">{success}</div>}

        <div style={{ display: 'flex', gap: 12, marginBottom: 24, flexWrap: 'wrap' }}>
          {TABS.map((t) => (
            <button key={t.key} className={`btn ${tab === t.key ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setTab(t.key)}>
              {t.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="loader">Loading records…</div>
        ) : tab === 'accounts' ? (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>Account #</th><th>Owner</th><th>Type</th><th>Branch (IFSC)</th><th>Balance</th><th>Status</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((a) => (
                  <tr key={a.id}>
                    <td className="mono">{a.accountNumber}</td>
                    <td>{a.ownerUsername}</td>
                    <td>{a.accountType}</td>
                    <td className="mono">{a.branchName} ({a.ifscCode})</td>
                    <td className="mono">₹{Number(a.balance).toFixed(2)}</td>
                    <td>{a.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : tab === 'users' ? (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>Username</th><th>Full Name</th><th>Email</th><th>Role</th><th>KYC</th><th>Locked</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td className="mono">{u.username}</td>
                    <td>{u.fullName}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                    <td>{u.kycStatus}</td>
                    <td>{u.locked ? 'Yes' : 'No'}</td>
                    <td style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      {u.locked && (
                        <button className="btn btn-ghost btn-sm" onClick={() => handleUnlock(u.id)}>Unlock</button>
                      )}
                      {u.kycStatus === 'PENDING' && (
                        <>
                          <button className="btn btn-ghost btn-sm" onClick={() => handleVerifyKyc(u.id)}>Verify KYC</button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleRejectKyc(u.id)}>Reject KYC</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : tab === 'loans' ? (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>Borrower</th><th>Type</th><th>Principal</th><th>EMI</th><th>Tenure</th><th>Status</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loans.map((l) => (
                  <tr key={l.id}>
                    <td className="mono">{l.borrowerUsername}</td>
                    <td>{l.loanType}</td>
                    <td className="mono">₹{Number(l.principal).toFixed(2)}</td>
                    <td className="mono">₹{Number(l.emiAmount).toFixed(2)}</td>
                    <td>{l.tenureMonths} mo</td>
                    <td>{l.status}</td>
                    <td style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      {l.status === 'PENDING' && (
                        <>
                          <button className="btn btn-ghost btn-sm" onClick={() => handleApproveLoan(l.id)}>Approve</button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleRejectLoan(l.id)}>Reject</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : tab === 'cards' ? (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>Owner</th><th>Card</th><th>Limit</th><th>Outstanding</th><th>Status</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {cards.map((c) => (
                  <tr key={c.id}>
                    <td className="mono">{c.ownerUsername}</td>
                    <td className="mono">{c.maskedCardNumber}</td>
                    <td className="mono">₹{Number(c.creditLimit).toFixed(2)}</td>
                    <td className="mono">₹{Number(c.outstandingBalance).toFixed(2)}</td>
                    <td>{c.status}</td>
                    <td>
                      {c.status === 'APPLIED' && (
                        <button className="btn btn-ghost btn-sm" onClick={() => handleApproveCard(c.id)}>Approve</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : tab === 'tickets' ? (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>User</th><th>Subject</th><th>Status</th><th>Raised</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map((t) => (
                  <tr key={t.id}>
                    <td className="mono">{t.username}</td>
                    <td>{t.subject}</td>
                    <td>{t.status}</td>
                    <td className="mono">{new Date(t.createdAt).toLocaleDateString()}</td>
                    <td style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      {t.status !== 'RESOLVED' && (
                        <button className="btn btn-ghost btn-sm" onClick={() => handleUpdateTicket(t.id, 'IN_PROGRESS')}>In Progress</button>
                      )}
                      {t.status !== 'RESOLVED' && (
                        <button className="btn btn-ghost btn-sm" onClick={() => handleUpdateTicket(t.id, 'RESOLVED')}>Resolve</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="card">
            <table className="txn-table">
              <thead>
                <tr>
                  <th>User</th><th>Action</th><th>Details</th><th>Flagged</th><th>When</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map((log) => (
                  <tr key={log.id}>
                    <td className="mono">{log.username}</td>
                    <td>{log.action}</td>
                    <td>{log.details}</td>
                    <td>{log.flagged ? '⚠️ Yes' : 'No'}</td>
                    <td className="mono">{new Date(log.timestamp).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
