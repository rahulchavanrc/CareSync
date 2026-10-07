const LABELS = {
  PENDING: 'Pending',
  CONFIRMED: 'Confirmed',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
};

export default function StatusPill({ status }) {
  const cls = `pill pill-${status?.toLowerCase()}`;
  return <span className={cls}>{LABELS[status] || status}</span>;
}
