import React, { createContext, useContext, useState, useCallback } from 'react';
import { authApi } from '../api/services.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('obs_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (username, password) => {
    const data = await authApi.login({ username, password });
    persist(data);
    return data;
  }, []);

  const register = useCallback(async (payload) => {
    const data = await authApi.register(payload);
    persist(data);
    return data;
  }, []);

  const persist = (data) => {
    localStorage.setItem('obs_token', data.token);
    const userInfo = { username: data.username, role: data.role };
    localStorage.setItem('obs_user', JSON.stringify(userInfo));
    setUser(userInfo);
  };

  const logout = useCallback(() => {
    localStorage.removeItem('obs_token');
    localStorage.removeItem('obs_user');
    setUser(null);
  }, []);

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
