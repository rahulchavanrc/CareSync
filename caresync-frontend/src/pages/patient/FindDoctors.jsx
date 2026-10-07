import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as api from "../../api/endpoints";
import Modal from "../../components/Modal";
import Avatar from "../../components/Avatar";
import StarRating from "../../components/StarRating";

export default function FindDoctors() {
  const [doctors, setDoctors] = useState([]);
  const [specialization, setSpecialization] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [bookingDoctor, setBookingDoctor] = useState(null);

  const fetchDoctors = async (spec) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await api.getDoctors(spec);
      setDoctors(data);
    } catch {
      setError("Could not load doctors right now. Try again in a moment.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchDoctors(specialization);
  };

  return (
    <div className="page container">
      <div className="page-header">
        <div>
          <h2>Find a doctor</h2>
          <p className="text-soft">
            Search by specialty, or browse everyone taking new patients.
          </p>
        </div>
        <form className="search-form" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="e.g. Cardiologist, Dentist"
            value={specialization}
            onChange={(e) => setSpecialization(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">
            Search
          </button>
        </form>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="text-faint">Loading doctors…</p>
      ) : doctors.length === 0 ? (
        <div className="empty-state card">
          <h3>No doctors match that search</h3>
          <p>Try a different specialty, or clear the search to see everyone.</p>
        </div>
      ) : (
        <div className="doctor-grid">
          {doctors.map((doc) => (
            <div className="doctor-card card" key={doc.doctorId}>
              <Link
                to={`/doctors/${doc.doctorId}`}
                className="doctor-card-link"
              >
                <Avatar name={doc.fullName} photoUrl={doc.photoUrl} size={52} />
                <h3>{doc.fullName}</h3>
                <p className="specialization">
                  {doc.specialization || "General practice"}
                </p>
                <div className="doctor-rating-row">
                  <StarRating value={doc.averageRating || 0} size={13} />
                  <span className="text-faint">
                    {doc.averageRating ? doc.averageRating.toFixed(1) : "New"}
                    {doc.reviewCount > 0 && ` (${doc.reviewCount})`}
                  </span>
                </div>
                <p className="text-faint doctor-meta">
                  {doc.experienceYears != null && (
                    <span>{doc.experienceYears} yrs experience</span>
                  )}
                  {doc.consultationFee != null && (
                    <span> · ₹{doc.consultationFee} / visit</span>
                  )}
                </p>
              </Link>
              <button
                className="btn btn-primary btn-block"
                onClick={() => setBookingDoctor(doc)}
              >
                Book appointment
              </button>
            </div>
          ))}
        </div>
      )}

      {bookingDoctor && (
        <BookingModal
          doctor={bookingDoctor}
          onClose={() => setBookingDoctor(null)}
        />
      )}

      <style>{`
        .page-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-end;
          gap: 24px;
          flex-wrap: wrap;
          margin-bottom: 28px;
        }
        .search-form { display: flex; gap: 10px; }
        .search-form input {
          padding: 11px 14px;
          border: 1px solid var(--color-border);
          border-radius: var(--radius-sm);
          min-width: 240px;
        }
        .doctor-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
          gap: 20px;
        }
        .doctor-card { display: flex; flex-direction: column; gap: 12px; }
        .doctor-card-link { color: inherit; display: block; }
        .doctor-card-link:hover { text-decoration: none; }
        .doctor-card-link h3 { margin: 12px 0 2px; }
        .specialization { color: var(--color-primary); font-weight: 500; margin-bottom: 6px; }
        .doctor-rating-row { display: flex; align-items: center; gap: 6px; margin-bottom: 6px; }
        .doctor-meta { font-size: 0.82rem; margin-bottom: 0; }
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
