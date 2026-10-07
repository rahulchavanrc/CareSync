import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <>
      <header className="topnav">
        <div className="container topnav-inner">
          <Link to="/" className="brand">
            <span className="brand-mark" aria-hidden="true" />
            CareSync
          </Link>

          <nav className="topnav-links">
            {user?.role === 'PATIENT' && (
              <>
                <NavLink to="/doctors" className="navlink">Find a doctor</NavLink>
                <NavLink to="/appointments" className="navlink">My appointments</NavLink>
                <NavLink to="/records" className="navlink">My records</NavLink>
              </>
            )}
            {user?.role === 'DOCTOR' && (
              <NavLink to="/schedule" className="navlink">My schedule</NavLink>
            )}
            {user?.role === 'ADMIN' && (
              <>
                <NavLink to="/admin" className="navlink">Dashboard</NavLink>
                <NavLink to="/admin/doctors" className="navlink">Onboard doctor</NavLink>
              </>
            )}
          </nav>

          <div className="topnav-actions">
            {user ? (
              <>
                <span className="topnav-user text-soft">{user.email}</span>
                <button className="btn btn-secondary btn-sm" onClick={handleLogout}>Log out</button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary btn-sm">Log in</Link>
                <Link to="/register" className="btn btn-primary btn-sm">Sign up</Link>
              </>
            )}
          </div>
        </div>
      </header>

      <Outlet />

      <footer className="site-footer">
        <div className="container">
          <span className="text-faint">CareSync — telehealth &amp; appointment booking, built for calmer care.</span>
        </div>
      </footer>

      <style>{`
        .topnav {
          background: var(--color-surface);
          border-bottom: 1px solid var(--color-border);
          position: sticky;
          top: 0;
          z-index: 50;
        }
        .topnav-inner {
          display: flex;
          align-items: center;
          gap: 32px;
          height: 68px;
        }
        .brand {
          display: flex;
          align-items: center;
          gap: 10px;
          font-family: var(--font-display);
          font-weight: 600;
          font-size: 1.25rem;
          color: var(--color-ink);
        }
        .brand:hover { text-decoration: none; }
        .brand-mark {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          background: var(--color-accent);
          box-shadow: 0 0 0 4px var(--color-accent-tint);
        }
        .topnav-links {
          display: flex;
          gap: 4px;
          flex: 1;
        }
        .navlink {
          padding: 8px 14px;
          border-radius: var(--radius-sm);
          color: var(--color-ink-soft);
          font-weight: 500;
          font-size: 0.9rem;
        }
        .navlink:hover { color: var(--color-primary); text-decoration: none; background: var(--color-surface-sunken); }
        .navlink.active { color: var(--color-primary); background: var(--color-primary-tint); }
        .topnav-actions {
          display: flex;
          align-items: center;
          gap: 12px;
        }
        .topnav-user { font-size: 0.85rem; }
        .site-footer {
          border-top: 1px solid var(--color-border);
          padding: 24px 0;
          font-size: 0.82rem;
        }
        @media (max-width: 820px) {
          .topnav-links { display: none; }
        }
      `}</style>
    </>
  );
}
