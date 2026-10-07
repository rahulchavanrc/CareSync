import { useEffect, useState } from 'react';
import * as api from '../../api/endpoints';
import StatusPill from '../../components/StatusPill';
import Modal from '../../components/Modal';

const NEXT_ACTIONS = {
  PENDING: [
    { status: 'CONFIRMED', label: 'Confirm', cls: 'btn-primary' },
    { status: 'CANCELLED', label: 'Decline', cls: 'btn-danger' },
  ],
  CONFIRMED: [
    { status: 'COMPLETED', label: 'Mark completed', cls: 'btn-primary' },
    { status: 'CANCELLED', label: 'Cancel', cls: 'btn-danger' },
  ],
  COMPLETED: [],
  CANCELLED: [],
};

export default function Schedule() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actioningId, setActioningId] = useState(null);
  const [recordTarget, setRecordTarget] = useState(null);

  const load = () => {
    setLoading(true);
    api.getMySchedule()
      .then(({ data }) => setAppointments(data))
      .catch(() => setError('Could not load your schedule right now.'))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleAction = async (appointmentId, status) => {
    setActioningId(appointmentId);
    setError(null);
    try {
      const { data } = await api.updateAppointmentStatus(appointmentId, status);
      setAppointments((prev) => prev.map((a) => (a.appId === appointmentId ? data : a)));
    } catch (err) {
      setError(err.response?.data?.message || 'Could not update that appointment.');
    } finally {
      setActioningId(null);
    }
  };

  const upcoming = appointments.filter((a) => a.status === 'PENDING' || a.status === 'CONFIRMED');
  const past = appointments.filter((a) => a.status === 'COMPLETED' || a.status === 'CANCELLED');

  return (
    <div className="page container">
      <h2>My schedule</h2>
      <p className="text-soft">Manage requests, confirm visits, and log records once they're done.</p>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="text-faint">Loading…</p>
      ) : appointments.length === 0 ? (
        <div className="empty-state card">
          <h3>No appointments yet</h3>
          <p>Booking requests from patients will show up here.</p>
        </div>
      ) : (
        <>
          <SectionList
            title="Upcoming"
            items={upcoming}
            actioningId={actioningId}
            onAction={handleAction}
            onAddRecord={setRecordTarget}
          />
          <SectionList
            title="Past"
            items={past}
            actioningId={actioningId}
            onAction={handleAction}
            onAddRecord={setRecordTarget}
          />
        </>
      )}

      {recordTarget && (
        <RecordModal
          appointment={recordTarget}
          onClose={() => setRecordTarget(null)}
        />
      )}

      <style>{`
        .schedule-section { margin-bottom: 36px; }
        .schedule-section h3 { margin-bottom: 14px; }
        .appt-list { display: flex; flex-direction: column; gap: 14px; }
        .appt-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 20px;
          flex-wrap: wrap;
        }
        .appt-main h3 { margin-bottom: 2px; font-size: 1.05rem; }
        .appt-spec { margin-bottom: 4px; }
        .appt-reason { margin-bottom: 0; max-width: 380px; }
        .appt-meta { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
        .appt-datetime { font-size: 0.85rem; color: var(--color-ink-soft); }
        .appt-actions { display: flex; gap: 8px; }
      `}</style>
    </div>
  );
}

function SectionList({ title, items, actioningId, onAction, onAddRecord }) {
  if (items.length === 0) return null;
  return (
    <div className="schedule-section">
      <h3>{title}</h3>
      <div className="appt-list">
        {items.map((a) => (
          <div className="appt-row card" key={a.appId}>
            <div className="appt-main">
              <h3>{a.patientName}</h3>
              <p className="text-faint appt-spec mono">Appointment #{a.appId}</p>
              {a.reasonForVisit && <p className="text-faint appt-reason">"{a.reasonForVisit}"</p>}
            </div>
            <div className="appt-meta">
              <span className="mono appt-datetime">{a.appointmentDate} · {a.timeSlot}</span>
              <StatusPill status={a.status} />
              <div className="appt-actions">
                {NEXT_ACTIONS[a.status].map((action) => (
                  <button
                    key={action.status}
                    className={`btn btn-sm ${action.cls}`}
                    disabled={actioningId === a.appId}
                    onClick={() => onAction(a.appId, action.status)}
                  >
                    {action.label}
                  </button>
                ))}
                {a.status === 'COMPLETED' && (
                  <button className="btn btn-sm btn-secondary" onClick={() => onAddRecord(a)}>
                    Add record
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function RecordModal({ appointment, onClose }) {
  const [form, setForm] = useState({ symptoms: '', diagnosis: '', prescription: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.createMedicalRecord({ appointmentId: appointment.appId, ...form });
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save this record.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal title={`Medical record — ${appointment.patientName}`} onClose={onClose} width={560}>
      {success ? (
        <>
          <div className="alert alert-success">Record saved. The patient can now see it in their history.</div>
          <button className="btn btn-primary btn-block" onClick={onClose}>Done</button>
        </>
      ) : (
        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}
          <div className="field">
            <label htmlFor="symptoms">Symptoms</label>
            <textarea id="symptoms" name="symptoms" value={form.symptoms} onChange={handleChange} placeholder="What the patient presented with" />
          </div>
          <div className="field">
            <label htmlFor="diagnosis">Diagnosis</label>
            <textarea id="diagnosis" name="diagnosis" required value={form.diagnosis} onChange={handleChange} placeholder="Your assessment" />
          </div>
          <div className="field">
            <label htmlFor="prescription">Prescription</label>
            <textarea id="prescription" name="prescription" value={form.prescription} onChange={handleChange} placeholder="e.g. Amoxicillin 500mg — 3x/day for 7 days" />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
            {submitting ? 'Saving…' : 'Save record'}
          </button>
        </form>
      )}
    </Modal>
  );
}
