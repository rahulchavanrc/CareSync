import { useEffect } from 'react';

export default function Modal({ title, onClose, children, width = 480 }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="modal-panel" style={{ maxWidth: width }} onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{title}</h3>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            &times;
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
      <style>{`
        .modal-overlay {
          position: fixed;
          inset: 0;
          background: rgba(22, 37, 46, 0.45);
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20px;
          z-index: 100;
        }
        .modal-panel {
          background: var(--color-surface);
          border-radius: var(--radius-lg);
          box-shadow: var(--shadow-lg);
          width: 100%;
          max-height: 90vh;
          overflow-y: auto;
        }
        .modal-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 20px 24px 0;
        }
        .modal-header h3 { margin: 0; }
        .modal-close {
          background: none;
          border: none;
          font-size: 1.6rem;
          line-height: 1;
          color: var(--color-ink-faint);
          cursor: pointer;
          padding: 4px 8px;
        }
        .modal-close:hover { color: var(--color-ink); }
        .modal-body { padding: 20px 24px 24px; }
      `}</style>
    </div>
  );
}
