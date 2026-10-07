// Falls back to a DiceBear-generated avatar (seeded by name, so it's stable per doctor)
// when no photoUrl is set — avoids using unlicensed stock photography.
function fallbackAvatarUrl(seed) {
  return `https://api.dicebear.com/9.x/notionists/svg?seed=${encodeURIComponent(seed)}&backgroundColor=e4eef3`;
}

export default function Avatar({ name, photoUrl, size = 56 }) {
  const src = photoUrl || fallbackAvatarUrl(name || 'doctor');
  return (
    <img
      className="avatar-img"
      src={src}
      alt={name ? `Photo of ${name}` : 'Doctor avatar'}
      width={size}
      height={size}
      style={{ width: size, height: size }}
      loading="lazy"
      onError={(e) => {
        // If a hand-entered photoUrl 404s, drop back to the generated avatar instead of a broken image.
        if (e.currentTarget.src !== fallbackAvatarUrl(name || 'doctor')) {
          e.currentTarget.src = fallbackAvatarUrl(name || 'doctor');
        }
      }}
    />
  );
}
