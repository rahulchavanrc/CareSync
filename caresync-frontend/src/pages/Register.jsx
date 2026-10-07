import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import PulseLine from '../components/PulseLine';

const BLOOD_GROUPS = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

export default function Register() {
  const { register, loading, error } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: '',
    password: '',
    fullName: '',
    dob: '',
    gender: '',
    bloodGroup: '',
  });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await register({ ...form, dob: form.dob || null });
      navigate('/doctors');
    } catch {
      // error already surfaced via auth context
    }
  };

  return (
    <div className="page auth-page">
      <div className="container auth-container">
        <div className="auth-panel card">
          <PulseLine className="auth-pulse" />
          <h2>Create your account</h2>
          <p className="text-soft">Patients can register directly. Doctor accounts are set up by an admin.</p>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="fullName">Full name</label>
              <input id="fullName" name="fullName" required value={form.fullName} onChange={handleChange} placeholder="Jordan Lee" />
            </div>
            <div className="field">
              <label htmlFor="email">Email</label>
              <input id="email" name="email" type="email" required autoComplete="email" value={form.email} onChange={handleChange} placeholder="you@example.com" />
            </div>
            <div className="field">
              <label htmlFor="password">Password</label>
              <input id="password" name="password" type="password" required minLength={8} autoComplete="new-password" value={form.password} onChange={handleChange} placeholder="At least 8 characters" />
            </div>

            <div className="field-row">
              <div className="field">
                <label htmlFor="dob">Date of birth</label>
                <input id="dob" name="dob" type="date" value={form.dob} onChange={handleChange} />
              </div>
              <div className="field">
                <label htmlFor="gender">Gender</label>
                <input id="gender" name="gender" value={form.gender} onChange={handleChange} placeholder="Optional" />
              </div>
            </div>

            <div className="field">
              <label htmlFor="bloodGroup">Blood group</label>
              <select id="bloodGroup" name="bloodGroup" value={form.bloodGroup} onChange={handleChange}>
                <option value="">Prefer not to say</option>
                {BLOOD_GROUPS.map((bg) => <option key={bg} value={bg}>{bg}</option>)}
              </select>
            </div>

            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <p className="auth-switch text-soft">
            Already registered? <Link to="/login">Log in</Link>
          </p>
        </div>
      </div>

      <style>{`
        .auth-page { display: flex; align-items: center; padding-bottom: 60px; }
        .auth-container { display: flex; justify-content: center; }
        .auth-panel { width: 100%; max-width: 460px; padding: 36px; }
        .auth-pulse { width: 100%; height: 44px; margin-bottom: 8px; }
        .auth-switch { margin-top: 18px; margin-bottom: 0; font-size: 0.88rem; text-align: center; }
        .field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
      `}</style>
    </div>
  );
}
