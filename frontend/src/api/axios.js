import axios from 'axios';

// In dev, Vite proxies /api -> localhost:8080 (see vite.config.js).
// In production, set VITE_API_BASE_URL to your deployed backend's full URL
// (e.g. https://your-backend.up.railway.app/api) if frontend and backend
// are hosted separately. Leave unset if the frontend is served by the
// same Spring Boot app (Option A in DEPLOYMENT.md).
const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({ baseURL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('obs_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('obs_token');
      localStorage.removeItem('obs_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
