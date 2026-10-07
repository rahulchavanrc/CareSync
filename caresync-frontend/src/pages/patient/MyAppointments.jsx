import { useEffect, useState } from 'react';
import * as api from '../../api/endpoints';
import StatusPill from '../../components/StatusPill';
import StarRating from '../../components/StarRating';
import Modal from '../../components/Modal';

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reviewTarget, setReviewTarget] = useState(null);

  const load = () => {
    setLoading(true);
    api.getMyAppointments()
      .then(({ data }) => setAppointments(data))
      .catch(() => setError('Could not load your appointments right now.'))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleReviewed = (appId) => {
    setAppointments((prev) => prev.map((a) => (a.appId === appId ? { ...a, reviewed: true } : a)));
    setReviewTarget(null);
  };

  return (
    <div className="page container">
      <h2>My appointments</h2>
      <p className="text-soft">Everything you've booked, most recent first.</p>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="text-faint">Loading…</p>
      ) : appointments.length === 0 ? (
        <div className="empty-state card">
          <h3>No appointments yet</h3>
          <p>Once you book a visit, it'll show up here.</p>
        </div>
      ) : (
        <div className="appt-list">
          {appointments.map((a) => (
            <div className="appt-row card" key={a.appId}>
              <div className="appt-main">
                <h3>{a.doctorName}</h3>
                <p className="text-soft appt-spec">{a.specialization}</p>
                {a.reasonForVisit && <p className="text-faint appt-reason">"{a.reasonForVisit}"</p>}
              </div>
              <div className="appt-meta">
                <span className="mono appt-datetime">{a.appointmentDate} · {a.timeSlot}</span>
                <StatusPill status={a.status} />
                {a.status === 'COMPLETED' && (
                  a.reviewed
                    ? <span className="text-faint reviewed-tag">Reviewed</span>
                    : <button className="btn btn-secondary btn-sm" onClick={() => setReviewTarget(a)}>Leave a review</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {reviewTarget && (
        <ReviewModal appointment={reviewTarget} onClose={() => setReviewTarget(null)} onSaved={handleReviewed} />
      )}

      <style>{`
        .appt-list { display: flex; flex-direction: column; gap: 14px; }
        .appt-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 20px;
          flex-wrap: wrap;
        }
        .appt-main h3 { margin-bottom: 2px; }
        .appt-spec { margin-bottom: 4px; }
        .appt-reason { margin-bottom: 0; max-width: 420px; }
        .appt-meta { display: flex; align-items: center; gap: 14px; }
        .appt-datetime { font-size: 0.85rem; color: var(--color-ink-soft); }
        .reviewed-tag { font-size: 0.82rem; }
      `}</style>
    </div>
  );
}

function ReviewModal({ appointment, onClose, onSaved }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.createReview({ appointmentId: appointment.appId, rating, comment });
      onSaved(appointment.appId);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save your review.');
      setSubmitting(false);
    }
  };

  return (
    <Modal title={`Review ${appointment.doctorName}`} onClose={onClose}>
      <form onSubmit={handleSubmit}>
        {error && <div className="alert alert-error">{error}</div>}
        <div className="field">
          <label>Rating</label>
          <StarRating value={rating} onChange={setRating} readOnly={false} size={26} />
        </div>
        <div className="field">
          <label htmlFor="comment">Comment (optional)</label>
          <textarea
            id="comment"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="How did the visit go?"
          />
        </div>
        <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
          {submitting ? 'Saving…' : 'Submit review'}
        </button>
      </form>
    </Modal>
  );
}
