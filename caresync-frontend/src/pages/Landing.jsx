import { Link } from 'react-router-dom';
import { Search, CalendarCheck, ShieldCheck, Star } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import PulseLine from '../components/PulseLine';
import Avatar from '../components/Avatar';
import StarRating from '../components/StarRating';

export default function Landing() {
  const { user } = useAuth();

  return (
    <div className="page">
      <section className="hero container">
        <div className="hero-copy">
          <span className="eyebrow">Telehealth, without the hold music</span>
          <h1>Book a doctor the way you'd book a table — then actually keep the appointment.</h1>
          <p className="hero-sub">
            Search by specialty, check real reviews, and pick an open slot with a confirmation
            you can trust. Doctors manage their schedule and write up visits in one place;
            patients see only what's theirs.
          </p>
          <div className="hero-actions">
            {user ? (
              <Link
                to={user.role === 'DOCTOR' ? '/schedule' : user.role === 'ADMIN' ? '/admin' : '/doctors'}
                className="btn btn-primary"
              >
                Go to your dashboard
              </Link>
            ) : (
              <>
                <Link to="/register" className="btn btn-primary">Book your first visit</Link>
                <Link to="/login" className="btn btn-secondary">I already have an account</Link>
              </>
            )}
          </div>

          <div className="trust-row">
            <div className="trust-avatars">
              <Avatar name="Jordan Lee" size={32} />
              <Avatar name="Alex Rivera" size={32} />
              <Avatar name="Sam Patel" size={32} />
            </div>
            <div className="trust-text">
              <StarRating value={4.8} size={13} />
              <span className="text-faint">Rated by patients after every visit</span>
            </div>
          </div>
        </div>

        <div className="hero-visual card">
          <div className="vitals-row">
            <span className="text-faint mono">LIVE STATUS</span>
            <span className="pill pill-confirmed">Confirmed</span>
          </div>
          <PulseLine className="pulse-visual" />
          <div className="vitals-row">
            <div>
              <div className="vitals-label">Next available</div>
              <div className="vitals-value">Today, 2:30 PM</div>
            </div>
            <div>
              <div className="vitals-label">Specialist</div>
              <div className="vitals-value">Cardiology</div>
            </div>
          </div>
        </div>
      </section>

      <section className="container features">
        <div className="feature card">
          <span className="feature-icon"><Search size={18} /></span>
          <h3>Find the right specialist</h3>
          <p>Filter doctors by specialization, compare experience and fees, and read reviews from real patients before you commit to a time.</p>
        </div>
        <div className="feature card">
          <span className="feature-icon"><CalendarCheck size={18} /></span>
          <h3>No double-bookings</h3>
          <p>Every slot is checked against the doctor's existing schedule the moment you book — pending, confirmed, or cancelled, always in sync.</p>
        </div>
        <div className="feature card">
          <span className="feature-icon"><ShieldCheck size={18} /></span>
          <h3>Records that stay private</h3>
          <p>After your visit, diagnoses and prescriptions land in your record — visible only to you and your doctor, downloadable as a PDF.</p>
        </div>
        <div className="feature card">
          <span className="feature-icon"><Star size={18} /></span>
          <h3>Reviews you can trust</h3>
          <p>Only patients with a completed visit can leave a rating, so every review on a doctor's profile is from someone who was actually seen.</p>
        </div>
      </section>

      <style>{`
        .hero {
          display: grid;
          grid-template-columns: 1.1fr 0.9fr;
          gap: 56px;
          align-items: center;
          padding: 56px 24px 72px;
        }
        .eyebrow {
          display: inline-block;
          font-family: var(--font-mono);
          font-size: 0.78rem;
          letter-spacing: 0.04em;
          text-transform: uppercase;
          color: var(--color-accent);
          margin-bottom: 16px;
        }
        .hero-copy h1 {
          font-size: 2.6rem;
          max-width: 640px;
        }
        .hero-sub {
          font-size: 1.02rem;
          max-width: 520px;
        }
        .hero-actions {
          display: flex;
          gap: 12px;
          margin-top: 8px;
        }
        .trust-row {
          display: flex;
          align-items: center;
          gap: 14px;
          margin-top: 28px;
        }
        .trust-avatars { display: flex; }
        .trust-avatars .avatar-img { margin-left: -10px; }
        .trust-avatars .avatar-img:first-child { margin-left: 0; }
        .trust-text { display: flex; flex-direction: column; gap: 2px; font-size: 0.82rem; }
        .hero-visual {
          padding: 28px;
        }
        .vitals-row {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
        }
        .vitals-row + .vitals-row { margin-top: 20px; }
        .vitals-label {
          font-size: 0.74rem;
          text-transform: uppercase;
          letter-spacing: 0.03em;
          color: var(--color-ink-faint);
          margin-bottom: 4px;
        }
        .vitals-value {
          font-family: var(--font-display);
          font-size: 1.1rem;
          color: var(--color-ink);
        }
        .pulse-visual {
          width: 100%;
          height: 90px;
          margin: 20px 0;
        }
        .features {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 20px;
          padding-top: 8px;
        }
        .feature-icon {
          width: 34px;
          height: 34px;
          border-radius: 10px;
          background: var(--color-primary-tint);
          color: var(--color-primary);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 12px;
        }
        @media (max-width: 1000px) {
          .features { grid-template-columns: repeat(2, 1fr); }
        }
        @media (max-width: 900px) {
          .hero { grid-template-columns: 1fr; }
        }
        @media (max-width: 560px) {
          .features { grid-template-columns: 1fr; }
        }
      `}</style>
    </div>
  );
}
