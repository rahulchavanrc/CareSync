import { useState } from 'react';
import * as api from '../../api/endpoints';

export default function CreateDoctor() {
  const [form, setForm] = useState({
    email: '', password: '', fullName: '', specialization: '',
    experienceYears: '', consultationFee: '', bio: '', photoUrl: '',
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(false);
    try {
      await api.createDoctor({
        ...form,
        experienceYears: form.experienceYears ? Number(form.experienceYears) : null,
        consultationFee: form.consultationFee ? Number(form.consultationFee) : null,
      });
      setSuccess(true);
      setForm({ email: '', password: '', fullName: '', specialization: '', experienceYears: '', consultationFee: '', bio: '', photoUrl: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create this doctor account.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page container">
      <h2>Onboard a doctor</h2>
      <p className="text-soft">Doctor accounts aren't self-serve — create them here.</p>

      <div className="card form-card">
        {success && <div className="alert alert-success">Doctor account created.</div>}
        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field-row">
            <div className="field">
              <label htmlFor="fullName">Full name</label>
              <input id="fullName" name="fullName" required value={form.fullName} onChange={handleChange} placeholder="Dr. Aisha Khan" />
            </div>
            <div className="field">
              <label htmlFor="specialization">Specialization</label>
              <input id="specialization" name="specialization" required value={form.specialization} onChange={handleChange} placeholder="Cardiologist" />
            </div>
          </div>

          <div className="field-row">
            <div className="field">
              <label htmlFor="email">Email</label>
              <input id="email" name="email" type="email" required value={form.email} onChange={handleChange} placeholder="doctor@example.com" />
            </div>
            <div className="field">
              <label htmlFor="password">Temporary password</label>
              <input id="password" name="password" type="password" required minLength={8} value={form.password} onChange={handleChange} placeholder="At least 8 characters" />
            </div>
          </div>

          <div className="field-row">
            <div className="field">
              <label htmlFor="experienceYears">Years of experience</label>
              <input id="experienceYears" name="experienceYears" type="number" min="0" value={form.experienceYears} onChange={handleChange} />
            </div>
            <div className="field">
              <label htmlFor="consultationFee">Consultation fee</label>
              <input id="consultationFee" name="consultationFee" type="number" min="0" step="0.01" value={form.consultationFee} onChange={handleChange} />
            </div>
          </div>

          <div className="field">
            <label htmlFor="bio">Bio</label>
            <textarea id="bio" name="bio" value={form.bio} onChange={handleChange} placeholder="Short public bio patients will see" />
          </div>

          <div className="field">
            <label htmlFor="photoUrl">Photo URL</label>
            <input id="photoUrl" name="photoUrl" type="url" value={form.photoUrl} onChange={handleChange} placeholder="Optional — leave blank to use a generated avatar" />
            <p className="field-hint">Only use a photo you have the rights to publish. If left blank, patients will see a generated avatar instead.</p>
          </div>

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create doctor account'}
          </button>
        </form>
      </div>

      <style>{`
        .form-card { max-width: 640px; padding: 28px; }
        .field-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
      `}</style>
    </div>
  );
}
