export default function PulseLine({ className = '', color = 'var(--color-accent)' }) {
  return (
    <svg
      className={className}
      viewBox="0 0 600 80"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      preserveAspectRatio="none"
      aria-hidden="true"
    >
      <path
        d="M0 40 H140 L165 40 L180 12 L200 68 L218 40 L240 40 L260 20 L275 40 H600"
        stroke={color}
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        pathLength="1"
        style={{
          strokeDasharray: 1,
          strokeDashoffset: 1,
          animation: 'draw-pulse 2.4s ease-out forwards',
        }}
      />
      <style>{`
        @keyframes draw-pulse {
          to { stroke-dashoffset: 0; }
        }
        @media (prefers-reduced-motion: reduce) {
          path { animation: none !important; stroke-dashoffset: 0 !important; }
        }
      `}</style>
    </svg>
  );
}
