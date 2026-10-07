import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
  Briefcase,
  DollarSign,
  MessageSquareText,
  ArrowLeft,
} from "lucide-react";
import * as api from "../../api/endpoints";
import Avatar from "../../components/Avatar";
import StarRating from "../../components/StarRating";
import Modal from "../../components/Modal";

export default function DoctorDetail() {
  const { doctorId } = useParams();
  const [doctor, setDoctor] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [booking, setBooking] = useState(false);

  useEffect(() => {
    setLoading(true);
    Promise.all([api.getDoctor(doctorId), api.getDoctorReviews(doctorId)])
      .then(([docRes, reviewRes]) => {
        setDoctor(docRes.data);
        setReviews(reviewRes.data);
      })
      .catch(() => setError("Could not load this doctor right now."))
      .finally(() => setLoading(false));
  }, [doctorId]);

  if (loading)
    return (
      <div className="page container">
        <p className="text-faint">Loading…</p>
      </div>
    );
  if (error || !doctor)
    return (
      <div className="page container">
        <div className="alert alert-error">{error || "Doctor not found."}</div>
      </div>
    );

  return (
    <div className="page container">
      <Link to="/doctors" className="back-link">
        <ArrowLeft size={15} /> Back to search
      </Link>

      <div className="detail-layout">
        <div className="detail-main card">
          <div className="detail-header">
            <Avatar
              name={doctor.fullName}
              photoUrl={doctor.photoUrl}
              size={84}
            />
            <div>
              <h2>{doctor.fullName}</h2>
              <p className="detail-spec">
                {doctor.specialization || "General practice"}
              </p>
              <div className="detail-rating-row">
                <StarRating value={doctor.averageRating || 0} />
                <span className="text-faint">
                  {doctor.averageRating
                    ? doctor.averageRating.toFixed(1)
                    : "No ratings yet"}
                  {doctor.reviewCount > 0 &&
                    ` · ${doctor.reviewCount} review${doctor.reviewCount === 1 ? "" : "s"}`}
                </span>
              </div>
            </div>
          </div>

          <div className="detail-stats">
            {doctor.experienceYears != null && (
              <div className="stat">
                <Briefcase size={16} /> {doctor.experienceYears} years
                experience
              </div>
            )}
            {doctor.consultationFee != null && (
              <div className="stat">
                <DollarSign size={16} /> ₹{doctor.consultationFee} per visit
              </div>
            )}
          </div>

          {doctor.bio && <p className="detail-bio">{doctor.bio}</p>}

          <button className="btn btn-primary" onClick={() => setBooking(true)}>
            Book appointment
          </button>
        </div>

        <div className="detail-reviews card">
          <h3>
            <MessageSquareText size={18} /> Patient reviews
          </h3>
          {reviews.length === 0 ? (
            <p className="text-faint">
              No reviews yet — be the first after your visit.
            </p>
          ) : (
            <div className="review-list">
              {reviews.map((r) => (
                <div className="review-item" key={r.reviewId}>
                  <div className="review-item-header">
                    <span className="review-author">
                      {r.reviewerDisplayName}
                    </span>
                    <StarRating value={r.rating} size={13} />
                  </div>
                  {r.comment && <p className="review-comment">{r.comment}</p>}
                  <span className="text-faint review-date">
                    {new Date(r.createdAt).toLocaleDateString()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {booking && (
        <BookingModal doctor={doctor} onClose={() => setBooking(false)} />
      )}

      <style>{`
        .back-link {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          font-size: 0.85rem;
          color: var(--color-ink-soft);
          margin-bottom: 20px;
        }
        .detail-layout {
          display: grid;
          grid-template-columns: 1.4fr 1fr;
          gap: 24px;
          align-items: start;
        }
        .detail-main { padding: 28px; }
        .detail-header { display: flex; gap: 18px; align-items: center; margin-bottom: 18px; }
        .detail-header h2 { margin-bottom: 2px; }
        .detail-spec { color: var(--color-primary); font-weight: 500; margin-bottom: 6px; }
        .detail-rating-row { display: flex; align-items: center; gap: 8px; }
        .detail-stats { display: flex; gap: 20px; margin: 16px 0; flex-wrap: wrap; }
        .stat { display: flex; align-items: center; gap: 6px; font-size: 0.88rem; color: var(--color-ink-soft); }
        .detail-bio { margin-bottom: 20px; }
        .detail-reviews { padding: 24px; }
        .detail-reviews h3 { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
        .review-list { display: flex; flex-direction: column; gap: 16px; }
        .review-item { padding-bottom: 16px; border-bottom: 1px solid var(--color-border); }
        .review-item:last-child { border-bottom: none; padding-bottom: 0; }
        .review-item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
        .review-author { font-weight: 600; font-size: 0.88rem; }
        .review-comment { margin: 4px 0; font-size: 0.9rem; color: var(--color-ink); }
        .review-date { font-size: 0.76rem; }
        @media (max-width: 860px) {
          .detail-layout { grid-template-columns: 1fr; }
        }
      `}</style>
    </div>
  );
}

function BookingModal({ doctor, onClose }) {
  const [form, setForm] = useState({
    appointmentDate: "",
    timeSlot: "",
    reasonForVisit: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await api.bookAppointment({ doctorId: doctor.doctorId, ...form });
      setSuccess(true);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "That slot is not available. Try a different time.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const today = new Date().toISOString().split("T")[0];

  return (
    <Modal title={`Book with ${doctor.fullName}`} onClose={onClose}>
      {success ? (
        <>
          <div className="alert alert-success">
            Request sent — {doctor.fullName} will confirm your slot shortly.
          </div>
          <button className="btn btn-primary btn-block" onClick={onClose}>
            Done
          </button>
        </>
      ) : (
        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}
          <div className="field">
            <label htmlFor="appointmentDate">Date</label>
            <input
              id="appointmentDate"
              name="appointmentDate"
              type="date"
              required
              min={today}
              value={form.appointmentDate}
              onChange={handleChange}
            />
          </div>
          <div className="field">
            <label htmlFor="timeSlot">Time</label>
            <input
              id="timeSlot"
              name="timeSlot"
              type="time"
              required
              value={form.timeSlot}
              onChange={handleChange}
            />
          </div>
          <div className="field">
            <label htmlFor="reasonForVisit">Reason for visit</label>
            <textarea
              id="reasonForVisit"
              name="reasonForVisit"
              placeholder="Briefly describe what you'd like to discuss"
              value={form.reasonForVisit}
              onChange={handleChange}
            />
          </div>
          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={submitting}
          >
            {submitting ? "Booking…" : "Confirm request"}
          </button>
        </form>
      )}
    </Modal>
  );
}
