import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ArrowLeft, LoaderCircle } from 'lucide-react'
import api from '../lib/api'
import { APP_NAME } from '../lib/branding'

export default function AuthPage({ mode, onAuthenticated }) {
  const isRegister = mode === 'register'
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const update = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  async function submit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const { data } = await api.post(`/auth/${isRegister ? 'register' : 'login'}`, form)
      sessionStorage.setItem('sprintstack.token', data.token)
      sessionStorage.setItem('sprintstack.email', data.user.email)
      onAuthenticated(data.user)
      const returnTo = location.state?.from
      navigate(returnTo ? `${returnTo.pathname}${returnTo.search || ''}${returnTo.hash || ''}` : '/', { replace: true })
    } catch (requestError) {
      setError(requestError.response?.data?.error || requestError.response?.data?.message || 'We could not sign you in. Check the API connection and try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return <main className="auth-screen"><Link className="auth-back" to="/"><ArrowLeft size={16}/> Back to workspace</Link><section className="auth-card"><a className="brand auth-brand" href="/"><span className="brand-mark">{APP_NAME.slice(0,1).toUpperCase()}</span><span>{APP_NAME}</span></a><div className="eyebrow">TEAM PROJECTS & PRODUCTIVITY</div><h1>{isRegister ? 'Create your account' : 'Welcome back'}</h1><p className="auth-intro">{isRegister ? 'Start organizing your projects and work with your team.' : 'Sign in to continue to your workspace.'}</p><form onSubmit={submit} className="auth-form">{isRegister && <label>Full name<input name="fullName" autoComplete="name" value={form.fullName} onChange={update} required maxLength="120" placeholder="Your name"/></label>}<label>Email address<input name="email" type="email" autoComplete="email" value={form.email} onChange={update} required maxLength="190" placeholder="you@example.com"/></label><label>Password<input name="password" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} value={form.password} onChange={update} required minLength={isRegister ? 8 : 1} maxLength="72" placeholder={isRegister ? 'At least 8 characters' : 'Your password'}/></label>{error && <div className="auth-error" role="alert">{error}</div>}<button className="button button-primary auth-submit" disabled={submitting}>{submitting && <LoaderCircle className="spinner" size={15}/>} {submitting ? 'Please wait…' : isRegister ? 'Create account' : 'Sign in'}</button></form><p className="auth-switch">{isRegister ? 'Already have an account?' : `New to ${APP_NAME}?`} <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create an account'}</Link></p></section></main>
}
