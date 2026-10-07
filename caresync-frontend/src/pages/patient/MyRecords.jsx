import { useEffect, useState } from 'react';
import { FileDown, Loader2 } from 'lucide-react';
import * as api from '../../api/endpoints';
import { triggerBlobDownload } from '../../utils/download';

export default function MyRecords() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);

  useEffect(() => {
    api.getMyRecords()
      .then(({ data }) => setRecords(data))
      .catch(() => setError('Could not load your records right now.'))
      .finally(() => setLoading(false));
  }, []);

  const handleDownload = async (recordId) => {
    setDownloadingId(recordId);
    try {
      const { data } = await api.downloadPrescriptionPdf(recordId);
      triggerBlobDownload(data, `prescription-${recordId}.pdf`);
    } catch {
      setError('Could not generate that PDF right now. Try again in a moment.');
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <div className="page container">
      <h2>My medical records</h2>
      <p className="text-soft">Visible only to you and the doctor who treated you.</p>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="text-faint">Loading…</p>
      ) : records.length === 0 ? (
        <div className="empty-state card">
          <h3>No records yet</h3>
          <p>Records appear here after a doctor completes your visit.</p>
        </div>
      ) : (
        <div className="record-list">
          {records.map((r) => (
            <div className="record-card card" key={r.recordId}>
              <div className="record-header">
                <div>
                  <h3>{r.diagnosis}</h3>
                  <p className="text-soft">Dr. {r.doctorName?.replace(/^Dr\.?\s*/, '')}</p>
                </div>
                <span className="mono text-faint record-date">
                  {new Date(r.createdAt).toLocaleDateString()}
                </span>
              </div>
              {r.symptoms && (
                <div className="record-field">
                  <span className="record-label">Symptoms</span>
                  <p>{r.symptoms}</p>
                </div>
              )}
              {r.prescription && (
                <div className="record-field">
                  <span className="record-label">Prescription</span>
                  <p>{r.prescription}</p>
                </div>
              )}
              <button
                className="btn btn-secondary btn-sm record-download"
                disabled={downloadingId === r.recordId}
                onClick={() => handleDownload(r.recordId)}
              >
                {downloadingId === r.recordId
                  ? <><Loader2 size={14} className="spin" /> Preparing PDF…</>
                  : <><FileDown size={14} /> Download prescription (PDF)</>}
              </button>
            </div>
          ))}
        </div>
      )}

      <style>{`
        .record-list { display: flex; flex-direction: column; gap: 16px; }
        .record-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
        .record-date { font-size: 0.78rem; }
        .record-field { margin-top: 10px; }
        .record-label {
          font-size: 0.72rem;
          text-transform: uppercase;
          letter-spacing: 0.03em;
          color: var(--color-ink-faint);
          display: block;
          margin-bottom: 2px;
        }
        .record-field p { margin: 0; color: var(--color-ink); }
        .record-download { margin-top: 16px; }
        .spin { animation: spin 0.8s linear infinite; }
        @keyframes spin { to { transform: rotate(360deg); } }
      `}</style>
    </div>
  );
}
