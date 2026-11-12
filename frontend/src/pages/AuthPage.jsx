import { lazy, Suspense, useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, BadgeCheck, Camera, LoaderCircle, ShieldCheck, Sparkles } from 'lucide-react'
import api from '../lib/api'
import { APP_NAME } from '../lib/branding'

const Lanyard = lazy(() => import('../components/Lanyard'))

function xml(value = '') {
  return String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&apos;')
}

function makeLanyardFace({ name, email, role, photo, verified }) {
  const initials = (name || email || 'S').trim().split(/[\s@._-]+/).filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'S'
  const avatar = photo && verified
    ? `<image href="${xml(photo)}" x="174" y="170" width="252" height="252" preserveAspectRatio="xMidYMid slice" clip-path="url(#portrait)"/>`
    : `<circle cx="300" cy="296" r="126" fill="#f0efff"/><text x="300" y="330" text-anchor="middle" fill="#6265df" font-family="Arial,sans-serif" font-size="92" font-weight="700">${xml(initials)}</text>`
  const stateText = verified ? 'IDENTITY VERIFIED' : 'WORKSPACE PASS PREVIEW'
  const roleText = verified ? role : 'MEMBER'
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="900" viewBox="0 0 600 900"><defs><linearGradient id="wash" x1="0" x2="1" y1="0" y2="1"><stop stop-color="#f4f3ff"/><stop offset="1" stop-color="#ffffff"/></linearGradient><clipPath id="portrait"><circle cx="300" cy="296" r="126"/></clipPath></defs><rect width="600" height="900" rx="42" fill="url(#wash)"/><rect width="600" height="22" rx="10" fill="#6265df"/><circle cx="300" cy="296" r="139" fill="#fff" stroke="#e6e5fa" stroke-width="2"/>${avatar}<text x="300" y="486" text-anchor="middle" fill="#747b8a" font-family="Arial,sans-serif" font-size="18" font-weight="700" letter-spacing="4">${stateText}</text><text x="300" y="566" text-anchor="middle" fill="#252a35" font-family="Arial,sans-serif" font-size="38" font-weight="700">${xml(name || 'Workspace member').slice(0, 25)}</text><text x="300" y="612" text-anchor="middle" fill="#7b8290" font-family="Arial,sans-serif" font-size="20">${xml(email || 'your email address').slice(0, 38)}</text><rect x="220" y="656" width="160" height="42" rx="21" fill="#eeedff"/><text x="300" y="683" text-anchor="middle" fill="#5c5fd5" font-family="Arial,sans-serif" font-size="16" font-weight="700" letter-spacing="2">${xml(roleText).slice(0, 20)}</text><path d="M86 770h428" stroke="#e8e9f0" stroke-width="2"/><text x="300" y="814" text-anchor="middle" fill="#969daa" font-family="Arial,sans-serif" font-size="16" letter-spacing="3">${xml(APP_NAME.toUpperCase())} · TEAM PASS</text><circle cx="300" cy="52" r="5" fill="#fff" opacity=".85"/></svg>`
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`
}

export default function AuthPage({ mode, onAuthenticated }) {
  const isRegister = mode === 'register'
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
  const [photo, setPhoto] = useState('')
  const [photoName, setPhotoName] = useState('')
  const [profilePhotoExists, setProfilePhotoExists] = useState(false)
  const [error, setError] = useState('')
  const [photoError, setPhotoError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [verifiedUser, setVerifiedUser] = useState(null)
  const [profilePhotoLoading, setProfilePhotoLoading] = useState(false)
  const [profilePhotoSaving, setProfilePhotoSaving] = useState(false)
  const formRef = useRef(null)

  useEffect(() => {
    const syncAutofill = () => {
      const fields = formRef.current?.elements
      if (!fields) return
      setForm(current => ({
        ...current,
        fullName: fields.namedItem('fullName')?.value ?? current.fullName,
        email: fields.namedItem('email')?.value ?? current.email,
        password: fields.namedItem('password')?.value ?? current.password,
      }))
    }
    const frame = requestAnimationFrame(syncAutofill)
    const delayed = window.setTimeout(syncAutofill, 350)
    let attempts = 0
    const autofillPoll = window.setInterval(() => {
      syncAutofill()
      attempts += 1
      if (attempts >= 20) window.clearInterval(autofillPoll)
    }, 150)
    formRef.current?.addEventListener('input', syncAutofill)
    return () => {
      cancelAnimationFrame(frame)
      window.clearTimeout(delayed)
      window.clearInterval(autofillPoll)
      formRef.current?.removeEventListener('input', syncAutofill)
    }
  }, [])

  const cardName = verifiedUser?.fullName || form.fullName.trim() || 'Workspace member'
  const cardEmail = verifiedUser?.email || form.email.trim()
  const cardRole = verifiedUser?.role?.replaceAll('_', ' ') || 'MEMBER'
  const lanyardFace = useMemo(() => makeLanyardFace({
    name: cardName,
    email: cardEmail,
    role: cardRole,
    photo,
    verified: Boolean(verifiedUser),
  }), [cardName, cardEmail, cardRole, photo, verifiedUser])

  const update = event => setForm(current => ({ ...current, [event.target.name]: event.target.value }))

  async function choosePhoto(event) {
    const file = event.target.files?.[0]
    setPhotoError('')
    if (!file) return
    if (!file.type.startsWith('image/')) { setPhotoError('Choose an image file.'); return }
    if (file.size > 5 * 1024 * 1024) { setPhotoError('Choose an image under 5 MB.'); return }
    const imageUrl = URL.createObjectURL(file)
    const image = new Image()
    image.onload = () => {
      const scale = Math.min(1, 640 / Math.max(image.naturalWidth, image.naturalHeight))
      const canvas = document.createElement('canvas')
      canvas.width = Math.max(1, Math.round(image.naturalWidth * scale))
      canvas.height = Math.max(1, Math.round(image.naturalHeight * scale))
      const context = canvas.getContext('2d')
      if (!context) { URL.revokeObjectURL(imageUrl); setPhotoError('We could not preview that image.'); return }
      context.fillStyle = '#fff'
      context.fillRect(0, 0, canvas.width, canvas.height)
      context.drawImage(image, 0, 0, canvas.width, canvas.height)
      const photoData = canvas.toDataURL('image/jpeg', 0.84)
      setPhoto(photoData)
      setPhotoName(file.name)
      URL.revokeObjectURL(imageUrl)
      if (verifiedUser) {
        setProfilePhotoSaving(true)
        api.put('/auth/photo', { photo: photoData })
          .then(({ data }) => { setPhoto(data.photo); setPhotoName('Saved profile photo'); setProfilePhotoExists(true); setPhotoError('') })
          .catch(requestError => setPhotoError(requestError.response?.data?.error || 'The photo could not be saved. Choose it again to retry.'))
          .finally(() => setProfilePhotoSaving(false))
      }
    }
    image.onerror = () => { URL.revokeObjectURL(imageUrl); setPhotoError('We could not preview that image.') }
    image.src = imageUrl
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const { data } = await api.post(`/auth/${isRegister ? 'register' : 'login'}`, form)
      sessionStorage.setItem('sprintstack.token', data.token)
      sessionStorage.setItem('sprintstack.email', data.user.email)
      setVerifiedUser(data.user)
      onAuthenticated(data.user)
      setProfilePhotoLoading(true)
      try {
        if (photo) {
          const { data: savedPhoto } = await api.put('/auth/photo', { photo })
          setPhoto(savedPhoto.photo)
          setPhotoName('Saved profile photo')
          setProfilePhotoExists(true)
        } else {
          const { data: savedPhoto } = await api.get('/auth/photo')
          if (savedPhoto.photo) {
            setPhoto(savedPhoto.photo)
            setPhotoName('Saved profile photo')
            setProfilePhotoExists(true)
          } else {
            setProfilePhotoExists(false)
          }
        }
      } catch (photoRequestError) {
        setPhotoError(photoRequestError.response?.data?.error || 'Your account is verified, but its profile photo could not be loaded or saved.')
      } finally {
        setProfilePhotoLoading(false)
      }
    } catch (requestError) {
      setError(requestError.response?.data?.error || requestError.response?.data?.message || 'We could not sign you in. Check the API connection and try again.')
    } finally {
      setSubmitting(false)
    }
  }

  function goToDashboard() {
    navigate('/', { replace: true })
  }

  return <main className="auth-screen auth-screen-lanyard">
    <Link className="auth-back" to="/"><ArrowLeft size={16}/> Back to workspace</Link>
    <div className="auth-layout">
      <section className="auth-card auth-flow-card">
        <Link className="brand auth-brand" to="/"><span className="brand-mark">{APP_NAME.slice(0, 1).toUpperCase()}</span><span>{APP_NAME}</span></Link>
        {verifiedUser ? <div className="auth-verified-view" role="status">
          <span className="auth-verified-icon"><BadgeCheck size={22}/></span>
          <div className="eyebrow">IDENTITY VERIFIED</div>
          <h1>Your workspace pass is ready</h1>
          <p className="auth-intro">Your details are on your lanyard card. Drag or tap it to turn it over, then continue whenever you’re ready.</p>
          <label className="auth-photo-upload"><span><Camera size={14}/> Profile photo <small>{profilePhotoSaving?'Saving to your account…':photoName|| (profilePhotoExists?'Saved securely to your account':'No photo saved yet')}</small></span><span className="auth-photo-button">{photoName?'Change photo':'Choose photo'}<input type="file" accept="image/*" onChange={event=>void choosePhoto(event)} aria-label="Change your profile photo" disabled={profilePhotoSaving}/></span></label>
          {photoError&&<div className="auth-error" role="alert">{photoError}</div>}
          <button className="button button-primary auth-submit auth-dashboard-button" onClick={goToDashboard} disabled={profilePhotoLoading||profilePhotoSaving}><Sparkles size={15}/> {profilePhotoLoading?'Preparing your card…':'Go to dashboard'}</button>
        </div> : <>
          <div className="eyebrow">TEAM PROJECTS & PRODUCTIVITY</div>
          <h1>{isRegister ? 'Create your account' : 'Welcome back'}</h1>
          <p className="auth-intro">{isRegister ? 'Start organizing your projects and work with your team.' : 'Sign in to continue to your workspace.'}</p>
          <form ref={formRef} onSubmit={submit} className="auth-form">
            {isRegister && <label>Full name<input name="fullName" autoComplete="name" value={form.fullName} onChange={update} required maxLength="120" placeholder="Your name"/></label>}
            <label>Email address<input name="email" type="email" autoComplete="email" value={form.email} onChange={update} required maxLength="190" placeholder="you@example.com"/></label>
            <label>Password<input name="password" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} value={form.password} onChange={update} required minLength={isRegister ? 8 : 1} maxLength="72" placeholder={isRegister ? 'At least 8 characters' : 'Your password'}/></label>
            <div className="auth-photo-upload"><span><Camera size={14}/> Photo for your pass <small>{photoName||'Optional · saved after successful sign-in'}</small></span><label className="auth-photo-button">{photoName?'Change photo':'Choose photo'}<input type="file" accept="image/*" onChange={event=>void choosePhoto(event)} aria-label="Choose a photo for your pass"/></label></div>
            {photoError&&<div className="auth-error" role="alert">{photoError}</div>}
            {error&&<div className="auth-error" role="alert">{error}</div>}
            <button className="button button-primary auth-submit" disabled={submitting}>{submitting&&<LoaderCircle className="spinner" size={15}/>} {submitting?'Verifying…':isRegister?'Create account':'Sign in'}</button>
          </form>
          <p className="auth-switch">{isRegister?'Already have an account?':`New to ${APP_NAME}?`} <Link to={isRegister?'/login':'/register'}>{isRegister?'Sign in':'Create an account'}</Link></p>
        </>}
      </section>
      <aside className="auth-lanyard-panel" aria-label="Live identity card preview">
        <div className="auth-lanyard-heading"><span className="auth-lanyard-mark"><ShieldCheck size={15}/></span><div><b>{verifiedUser?'Verified member card':'Your workspace pass'}</b><small>{verifiedUser?'Ready to continue':'Updates as you enter your details'}</small></div></div>
        <div className="auth-lanyard-stage"><Suspense fallback={<div className="lanyard-loading"><LoaderCircle className="spinner" size={19}/> Preparing your card…</div>}><Lanyard frontImage={lanyardFace} orientation="portrait" finish="glossy" cornerRadius={0.34} size={0.68} strapColor="#5558d6" strapWidth={0.72} metal="silver" gravity={0.8} elasticity={0.48} damping={0.56} breeze={0.12} interactive intro className="auth-lanyard"/></Suspense></div>
        <p className="auth-lanyard-hint">Drag the card to move it · tap to flip</p>
        <span className="sr-only" aria-live="polite">{verifiedUser?'Verified identity card for':'Preview card for'} {cardName}, {cardEmail || 'email not entered'}.</span>
      </aside>
    </div>
  </main>
}
