export default function ProfileAvatar({ name, photo, className = 'profile-avatar', label }) {
  return <span className={`${className} avatar-photo`} aria-label={label || `${name || 'User'} profile`}>
    {photo ? <img src={photo} alt=""/> : (name || '?').trim().slice(0, 1).toUpperCase()}
  </span>
}
