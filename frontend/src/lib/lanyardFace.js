import { APP_NAME } from './branding'

function xml(value = '') {
  return String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&apos;')
}

export function makeLanyardFace({ name, email, role, photo, verified = true }) {
  const initials = (name || email || 'S').trim().split(/[\s@._-]+/).filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'S'
  const avatar = photo && verified
    ? `<image href="${xml(photo)}" x="174" y="170" width="252" height="252" preserveAspectRatio="xMidYMid slice" clip-path="url(#portrait)"/>`
    : `<circle cx="300" cy="296" r="126" fill="#f0efff"/><text x="300" y="330" text-anchor="middle" fill="#6265df" font-family="Arial,sans-serif" font-size="92" font-weight="700">${xml(initials)}</text>`
  const stateText = verified ? 'IDENTITY VERIFIED' : 'WORKSPACE PASS PREVIEW'
  const roleText = verified ? role : 'MEMBER'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="900" viewBox="0 0 600 900"><defs><linearGradient id="wash" x1="0" x2="1" y1="0" y2="1"><stop stop-color="#f4f3ff"/><stop offset="1" stop-color="#ffffff"/></linearGradient><clipPath id="portrait"><circle cx="300" cy="296" r="126"/></clipPath></defs><rect width="600" height="900" rx="42" fill="url(#wash)"/><rect width="600" height="22" rx="10" fill="#6265df"/><circle cx="300" cy="296" r="139" fill="#fff" stroke="#e6e5fa" stroke-width="2"/>${avatar}<text x="300" y="486" text-anchor="middle" fill="#747b8a" font-family="Arial,sans-serif" font-size="18" font-weight="700" letter-spacing="4">${stateText}</text><text x="300" y="566" text-anchor="middle" fill="#252a35" font-family="Arial,sans-serif" font-size="38" font-weight="700">${xml(name || 'Workspace member').slice(0, 25)}</text><text x="300" y="612" text-anchor="middle" fill="#7b8290" font-family="Arial,sans-serif" font-size="20">${xml(email || 'your email address').slice(0, 38)}</text><rect x="220" y="656" width="160" height="42" rx="21" fill="#eeedff"/><text x="300" y="683" text-anchor="middle" fill="#5c5fd5" font-family="Arial,sans-serif" font-size="16" font-weight="700" letter-spacing="2">${xml(roleText).slice(0, 20)}</text><path d="M86 770h428" stroke="#e8e9f0" stroke-width="2"/><text x="300" y="814" text-anchor="middle" fill="#969daa" font-family="Arial,sans-serif" font-size="16" letter-spacing="3">${xml(APP_NAME.toUpperCase())} · TEAM PASS</text><circle cx="300" cy="52" r="5" fill="#fff" opacity=".85"/></svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}
