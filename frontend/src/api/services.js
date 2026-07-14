import api from './axios.js';

export const authApi = {
  register: (payload) => api.post('/auth/register', payload).then((r) => r.data),
  login: (payload) => api.post('/auth/login', payload).then((r) => r.data),
  forgotPassword: (username) => api.post('/auth/forgot-password', { username }).then((r) => r.data),
  resetPassword: (payload) => api.post('/auth/reset-password', payload).then((r) => r.data)
};

export const accountApi = {
  create: (payload) => api.post('/accounts', payload).then((r) => r.data),
  myAccounts: () => api.get('/accounts/my').then((r) => r.data),
  getById: (id) => api.get(`/accounts/${id}`).then((r) => r.data),
  deposit: (id, payload) => api.post(`/accounts/${id}/deposit`, payload).then((r) => r.data),
  withdraw: (id, payload) => api.post(`/accounts/${id}/withdraw`, payload).then((r) => r.data),
  close: (id) => api.delete(`/accounts/${id}`).then((r) => r.data)
};

export const transactionApi = {
  transfer: (payload) => api.post('/transactions/transfer', payload).then((r) => r.data),
  history: (accountId) => api.get(`/transactions/account/${accountId}`).then((r) => r.data)
};

export const adminApi = {
  allAccounts: () => api.get('/admin/accounts').then((r) => r.data),
  allUsers: () => api.get('/admin/users').then((r) => r.data),
  unlockUser: (id) => api.put(`/admin/users/${id}/unlock`).then((r) => r.data),
  verifyKyc: (userId) => api.put(`/admin/kyc/${userId}/verify`).then((r) => r.data),
  rejectKyc: (userId, rejectionReason) => api.put(`/admin/kyc/${userId}/reject`, { rejectionReason }).then((r) => r.data),
  allLoans: () => api.get('/admin/loans').then((r) => r.data),
  approveLoan: (id) => api.put(`/admin/loans/${id}/approve`).then((r) => r.data),
  rejectLoan: (id, rejectionReason) => api.put(`/admin/loans/${id}/reject`, { rejectionReason }).then((r) => r.data),
  allTickets: () => api.get('/admin/tickets').then((r) => r.data),
  updateTicket: (id, payload) => api.put(`/admin/tickets/${id}`, payload).then((r) => r.data),
  approveCard: (id) => api.put(`/admin/credit-cards/${id}/approve`).then((r) => r.data),
  allCards: () => api.get('/admin/credit-cards').then((r) => r.data),
  auditLogs: () => api.get('/admin/audit-logs').then((r) => r.data),
  flaggedAuditLogs: () => api.get('/admin/audit-logs/flagged').then((r) => r.data)
};

export const branchApi = {
  list: () => api.get('/branches').then((r) => r.data)
};

export const profileApi = {
  get: () => api.get('/profile').then((r) => r.data),
  update: (payload) => api.put('/profile', payload).then((r) => r.data),
  submitKyc: (payload) => api.post('/profile/kyc', payload).then((r) => r.data)
};

export const beneficiaryApi = {
  add: (payload) => api.post('/beneficiaries', payload).then((r) => r.data),
  verify: (id, otp) => api.post(`/beneficiaries/${id}/verify`, { otp }).then((r) => r.data),
  list: () => api.get('/beneficiaries').then((r) => r.data),
  remove: (id) => api.delete(`/beneficiaries/${id}`).then((r) => r.data)
};

export const loanApi = {
  apply: (payload) => api.post('/loans/apply', payload).then((r) => r.data),
  myLoans: () => api.get('/loans/my').then((r) => r.data)
};

export const fixedDepositApi = {
  create: (payload) => api.post('/fixed-deposits', payload).then((r) => r.data),
  myDeposits: () => api.get('/fixed-deposits/my').then((r) => r.data),
  close: (id) => api.post(`/fixed-deposits/${id}/close`).then((r) => r.data)
};

export const creditCardApi = {
  apply: (payload) => api.post('/credit-cards/apply', payload).then((r) => r.data),
  myCards: () => api.get('/credit-cards/my').then((r) => r.data),
  block: (id) => api.post(`/credit-cards/${id}/block`).then((r) => r.data),
  unblock: (id) => api.post(`/credit-cards/${id}/unblock`).then((r) => r.data),
  payBill: (id, payload) => api.post(`/credit-cards/${id}/pay-bill`, payload).then((r) => r.data)
};

export const notificationApi = {
  list: () => api.get('/notifications').then((r) => r.data),
  markRead: (id) => api.put(`/notifications/${id}/read`).then((r) => r.data)
};

export const ticketApi = {
  raise: (payload) => api.post('/tickets', payload).then((r) => r.data),
  myTickets: () => api.get('/tickets/my').then((r) => r.data)
};

export const statementApi = {
  download: async (accountId) => {
    const response = await api.get(`/accounts/${accountId}/statement`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `statement-account-${accountId}.pdf`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }
};

export function extractErrorMessage(err) {
  if (err.response && err.response.data) {
    const data = err.response.data;
    if (data.fieldErrors) {
      const messages = Object.values(data.fieldErrors);
      if (messages.length) return messages.join(' · ');
    }
    if (data.message) return data.message;
  }
  return err.message || 'Something went wrong. Please try again.';
}
