import { Star } from 'lucide-react';

export default function StarRating({ value, onChange, size = 16, readOnly = true }) {
  const stars = [1, 2, 3, 4, 5];

  return (
    <span className="star-rating" role={readOnly ? undefined : 'radiogroup'} aria-label={readOnly ? undefined : 'Rating'}>
      {stars.map((n) => {
        const filled = n <= Math.round(value || 0);
        return readOnly ? (
          <Star
            key={n}
            size={size}
            fill={filled ? 'var(--color-warn)' : 'none'}
            color={filled ? 'var(--color-warn)' : 'var(--color-border)'}
            strokeWidth={1.5}
          />
        ) : (
          <button
            key={n}
            type="button"
            className="star-input-btn"
            aria-label={`${n} star${n > 1 ? 's' : ''}`}
            onClick={() => onChange(n)}
          >
            <Star
              size={size}
              fill={n <= value ? 'var(--color-warn)' : 'none'}
              color={n <= value ? 'var(--color-warn)' : 'var(--color-border)'}
              strokeWidth={1.5}
            />
          </button>
        );
      })}
      <style>{`
        .star-rating { display: inline-flex; align-items: center; gap: 2px; }
        .star-input-btn { background: none; border: none; padding: 2px; cursor: pointer; line-height: 0; }
      `}</style>
    </span>
  );
}
