import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import * as api from '../api/endpoints';

const AuthContext = createContext(null);

// Role name comes back as e.g. "ROLE_PATIENT" — normalize to "PATIENT" for UI use.
function simplifyRole(role) {
  return role?.replace('ROLE_', '') ?? null;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('caresync_user');
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (user) {
      localStorage.setItem('caresync_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('caresync_user');
    }
  }, [user]);

  const persistSession = (authResponse) => {
    const { token, email, role, profileId } = authResponse;
    localStorage.setItem('caresync_token', token);
    const sessionUser = { email, role: simplifyRole(role), profileId };
    setUser(sessionUser);
    return sessionUser;
  };

  const doLogin = useCallback(async (credentials) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.login(credentials);
      return persistSession(data);
    } catch (err) {
      const message = err.response?.data?.message || 'Invalid email or password';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const doRegister = useCallback(async (payload) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.registerPatient(payload);
      return persistSession(data);
    } catch (err) {
      const message = err.response?.data?.message || 'Could not create your account';
      setError(message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('caresync_token');
    localStorage.removeItem('caresync_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, error, login: doLogin, register: doRegister, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}
