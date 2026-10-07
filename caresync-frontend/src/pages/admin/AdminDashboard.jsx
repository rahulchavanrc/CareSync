import { useEffect, useState } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Users, Stethoscope, CalendarCheck, Star } from 'lucide-react';
import * as api from '../../api/endpoints';

const STATUS_COLORS = {
  Pending: '#C68A3D',
  Confirmed: '#2E9E8C',
  Completed: '#1B4B66',
  Cancelled: '#B3435C',
};

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.getDashboardStats()
      .then(({ data }) => setStats(data))
      .catch(() => setError('Could not load dashboard stats right now.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page container"><p className="text-faint">Loading dashboard…</p></div>;
  if (error || !stats) return <div className="page container"><div className="alert alert-error">{error}</div></div>;

  const chartData = [
    { name: 'Pending', value: stats.pendingCount },
    { name: 'Confirmed', value: stats.confirmedCount },
    { name: 'Completed', value: stats.completedCount },
    { name: 'Cancelled', value: stats.cancelledCount },
  ].filter((d) => d.value > 0);

  return (
    <div className="page container">
      <h2>Admin dashboard</h2>
      <p className="text-soft">Platform-wide activity at a glance.</p>

      <div className="stat-cards">
        <StatCard icon={<Stethoscope size={20} />} label="Doctors" value={stats.totalDoctors} />
        <StatCard icon={<Users size={20} />} label="Patients" value={stats.totalPatients} />
        <StatCard icon={<CalendarCheck size={20} />} label="Appointments" value={stats.totalAppointments} />
        <StatCard
          icon={<Star size={20} />}
          label="Avg. rating"
          value={stats.platformAverageRating ? stats.platformAverageRating.toFixed(1) : '—'}
          sub={`${stats.totalReviews} review${stats.totalReviews === 1 ? '' : 's'}`}
        />
      </div>

      <div className="dashboard-lower">
        <div className="card chart-card">
          <h3>Appointments by status</h3>
          {chartData.length === 0 ? (
            <p className="text-faint">No appointments booked yet.</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={chartData} dataKey="value" nameKey="name" innerRadius={60} outerRadius={95} paddingAngle={3}>
                  {chartData.map((entry) => (
                    <Cell key={entry.name} fill={STATUS_COLORS[entry.name]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="card breakdown-card">
          <h3>Breakdown</h3>
          <BreakdownRow label="Pending" value={stats.pendingCount} color={STATUS_COLORS.Pending} />
          <BreakdownRow label="Confirmed" value={stats.confirmedCount} color={STATUS_COLORS.Confirmed} />
          <BreakdownRow label="Completed" value={stats.completedCount} color={STATUS_COLORS.Completed} />
          <BreakdownRow label="Cancelled" value={stats.cancelledCount} color={STATUS_COLORS.Cancelled} />
        </div>
      </div>

      <style>{`
        .stat-cards {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 16px;
          margin-bottom: 24px;
        }
        .dashboard-lower {
          display: grid;
          grid-template-columns: 1.2fr 0.8fr;
          gap: 20px;
        }
        .chart-card, .breakdown-card { padding: 24px; }
        .chart-card h3, .breakdown-card h3 { margin-bottom: 14px; }
        @media (max-width: 900px) {
          .stat-cards { grid-template-columns: repeat(2, 1fr); }
          .dashboard-lower { grid-template-columns: 1fr; }
        }
      `}</style>
    </div>
  );
}

function StatCard({ icon, label, value, sub }) {
  return (
    <div className="card stat-card">
      <div className="stat-card-icon">{icon}</div>
      <div className="stat-card-value">{value}</div>
      <div className="stat-card-label text-faint">{label}{sub ? ` · ${sub}` : ''}</div>
      <style>{`
        .stat-card { padding: 20px; }
        .stat-card-icon {
          width: 36px; height: 36px;
          border-radius: 10px;
          background: var(--color-primary-tint);
          color: var(--color-primary);
          display: flex; align-items: center; justify-content: center;
          margin-bottom: 12px;
        }
        .stat-card-value { font-family: var(--font-display); font-size: 1.8rem; font-weight: 600; }
        .stat-card-label { font-size: 0.82rem; margin-top: 2px; }
      `}</style>
    </div>
  );
}

function BreakdownRow({ label, value, color }) {
  return (
    <div className="breakdown-row">
      <span className="breakdown-dot" style={{ background: color }} />
      <span className="breakdown-label">{label}</span>
      <span className="breakdown-value mono">{value}</span>
      <style>{`
        .breakdown-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid var(--color-border); }
        .breakdown-row:last-child { border-bottom: none; }
        .breakdown-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
        .breakdown-label { flex: 1; font-size: 0.9rem; }
        .breakdown-value { font-size: 0.9rem; color: var(--color-ink-soft); }
      `}</style>
    </div>
  );
}
