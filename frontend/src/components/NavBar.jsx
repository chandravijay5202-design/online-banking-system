import React, { useEffect, useRef, useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { notificationApi } from '../api/services.js';

const LINKS = [
  { to: '/dashboard', label: 'Accounts' },
  { to: '/transfer', label: 'Transfer' },
  { to: '/beneficiaries', label: 'Beneficiaries' },
  { to: '/loans', label: 'Loans' },
  { to: '/fixed-deposits', label: 'Fixed Deposits' },
  { to: '/credit-cards', label: 'Credit Cards' },
  { to: '/tickets', label: 'Support' },
  { to: '/notifications', label: 'Notifications', badge: true },
  { to: '/profile', label: 'Profile' }
];

export default function NavBar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    if (!user) return;
    notificationApi.list()
      .then((list) => setUnreadCount(list.filter((n) => !n.read).length))
      .catch(() => {});
  }, [user]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  if (!user) return null;

  const handleLogout = () => {
    setMenuOpen(false);
    logout();
    navigate('/login');
  };

  return (
    <nav className="appnav">
      <div className="inner">
        <div className="brand">Chandravijay <span>Bank</span></div>

        <div className="navdots-wrap" ref={menuRef}>
          <button
            className="navdots-btn"
            aria-label="Open menu"
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((v) => !v)}
          >
            ⋮
            {unreadCount > 0 && <span className="navdots-dot" />}
          </button>

          {menuOpen && (
            <div className="navdots-menu">
              <div className="navdots-user">
                <span className="pill">{user.username} · {isAdmin ? 'Admin' : 'Customer'}</span>
              </div>
              <div className="navdots-divider" />
              {LINKS.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) => `navdots-item ${isActive ? 'active' : ''}`}
                  onClick={() => setMenuOpen(false)}
                >
                  {link.label}
                  {link.badge && unreadCount > 0 ? ` (${unreadCount})` : ''}
                </NavLink>
              ))}
              {isAdmin && (
                <NavLink
                  to="/admin"
                  className={({ isActive }) => `navdots-item ${isActive ? 'active' : ''}`}
                  onClick={() => setMenuOpen(false)}
                >
                  Admin
                </NavLink>
              )}
              <div className="navdots-divider" />
              <button className="navdots-item navdots-logout" onClick={handleLogout}>Logout →</button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
