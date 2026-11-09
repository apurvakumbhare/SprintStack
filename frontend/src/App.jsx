import { lazy, Suspense, useEffect, useRef, useState } from 'react'
import { Link, NavLink, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import {
  Activity, ChevronDown, CircleHelp, ClipboardCheck, FolderKanban,
  LayoutDashboard, ListTodo, Search, Settings, ShieldAlert, Users,
} from 'lucide-react'
import api from './lib/api'
import { APP_NAME } from './lib/branding'
import AuthPage from './pages/AuthPage'
import ProjectsPage from './pages/ProjectsPage'
import TasksPage from './pages/TasksPage'
import IssuesPage from './pages/IssuesPage'
import ProjectDetailPage from './pages/ProjectDetailPage'
import SettingsPage from './pages/SettingsPage'
import MembersPage from './pages/MembersPage'
import NotificationsMenu from './components/NotificationsMenu'
const DashboardCharts = lazy(() => import('./components/DashboardCharts'))

const navigation = [
  { label: 'Overview', to: '/', icon: LayoutDashboard },
  { label: 'Projects', to: '/projects', icon: FolderKanban },
  { label: 'My tasks', to: '/tasks', icon: ListTodo },
  { label: 'Issues', to: '/issues', icon: ShieldAlert },
  { label: 'Members', to: '/members', icon: Users },
]

function Sidebar({ user, onLogout }) {
  return <aside className="sidebar">
    <Link className="brand" to="/" aria-label={`${APP_NAME} home`}><span className="brand-mark">S</span><span>{APP_NAME}</span></Link>
    <div className="workspace"><span className="workspace-avatar">S</span><span className="workspace-copy"><b>Your workspace</b><small>Getting started</small></span><ChevronDown size={15}/></div>
    <div className="nav-label">WORKSPACE</div>
    <nav className="nav-list">{navigation.map(({label,to,icon:Icon})=><NavLink key={to} to={to} end={to==='/'} className={({isActive})=>`nav-item ${isActive?'active':''}`}><Icon size={18}/><span>{label}</span></NavLink>)}</nav>
    <div className="nav-label team-label">TEAM</div>
    <Link className="nav-item" to="/#dashboard-activity"><Activity size={18}/><span>Activity</span></Link>
    <div className="sidebar-bottom"><a className="nav-item" href="#help"><CircleHelp size={18}/><span>Help & support</span></a><NavLink className={({isActive})=>isActive?'nav-item active':'nav-item'} to="/settings"><Settings size={18}/><span>Settings</span></NavLink>{user ? <div className="profile"><span className="profile-avatar">{user.fullName?.slice(0,1).toUpperCase()}</span><span className="profile-copy"><b>{user.fullName}</b><small>{user.role?.replaceAll('_',' ')}</small></span><button className="icon-button" aria-label="Sign out" title="Sign out" onClick={onLogout}><ChevronDown size={16}/></button></div> : <div className="profile"><span className="profile-avatar">?</span><span className="profile-copy"><b>Not signed in</b><small>Account setup pending</small></span></div>}</div>
  </aside>
}

function EmptyDashboard({health, user, summary, summaryError}) {
  return <div className="page-content">
    <div className="page-heading"><div><div className="eyebrow">TEAM PROJECTS & PRODUCTIVITY</div><h1>{user ? `Welcome, ${user.fullName}` : `Welcome to ${APP_NAME}`} <span className="wave">✦</span></h1><p>Bring your projects, tasks, and team into one place.</p></div><button className="button button-light" onClick={()=>document.getElementById('dashboard-activity')?.scrollIntoView({behavior:'smooth'})}><Activity size={16}/> Activity log</button></div>
    <div className="connect-banner"><span className={`status-dot ${health==='online'?'online':''}`}></span><div><b>{health==='online'?'API connected':`${APP_NAME} is ready to get started`}</b><p>{health==='online'?'Your workspace service is responding.':'Create your first project to bring your team and tasks together.'}</p></div><span className="banner-tag">{health==='online'?'LIVE':'WORKSPACE'}</span></div>
    {user&&summary&&<section className="metric-grid" aria-label="Workspace statistics"><article><span>Total projects</span><b>{summary.totalProjects}</b><small>{summary.activeProjects} active</small></article><article><span>Completed projects</span><b>{summary.completedProjects}</b><small>Finished projects</small></article><article><span>Total tasks</span><b>{summary.totalTasks}</b><small>{summary.pendingTasks} still open</small></article><article><span>Completed tasks</span><b>{summary.completedTasks}</b><small>Across your projects</small></article><article><span>Due in 7 days</span><b>{summary.dueSoonTasks}</b><small>Open tasks approaching</small></article><article><span>Overdue tasks</span><b className={summary.overdueTasks?'metric-alert':''}>{summary.overdueTasks}</b><small>Past their due date</small></article></section>}
    {user&&summary&&<Suspense fallback={<div className="chart-loading">Loading analytics…</div>}><DashboardCharts summary={summary}/></Suspense>}
    {summaryError&&<div className="inline-error" role="alert">{summaryError}</div>}
    <section className="overview-grid" aria-label="Workspace overview">
      <article className="overview-card"><div className="card-top"><span className="card-icon violet"><FolderKanban size={18}/></span><span className="card-note">Your workspace</span></div><h2>Projects</h2><p>Organize work into focused projects and invite your teammates.</p><NavLink className="text-button" to="/projects">Explore projects <span>→</span></NavLink></article>
      <article className="overview-card"><div className="card-top"><span className="card-icon blue"><ClipboardCheck size={18}/></span><span className="card-note">Stay on track</span></div><h2>Tasks</h2><p>Plan, assign, and follow work from start to done.</p><NavLink className="text-button" to="/tasks">Explore tasks <span>→</span></NavLink></article>
      <article className="overview-card"><div className="card-top"><span className="card-icon orange"><ShieldAlert size={18}/></span><span className="card-note">Keep quality high</span></div><h2>Issues</h2><p>Track bugs and follow every fix through to resolution.</p><NavLink className="text-button" to="/issues">Explore issues <span>→</span></NavLink></article>
    </section>
    {summary?.projectProgress?.length>0?<section className="progress-section"><div className="section-title"><div><div className="eyebrow">YOUR WORK</div><h2>Project progress</h2></div><Link className="text-button" to="/projects">All projects <span>→</span></Link></div><div className="progress-list">{summary.projectProgress.slice(0,5).map(project=><article className="progress-row" key={project.id}><div className="progress-name"><span className="progress-project-icon"><FolderKanban size={15}/></span><div><b>{project.name}</b><small>{project.totalTasks} tasks · {project.status.toLowerCase().replace('_',' ')}</small></div></div><div className="progress-bar"><i style={{width:`${project.completionPercent}%`}}/></div><b className="progress-value">{project.completionPercent}%</b></article>)}</div></section>:<section className="setup-card"><div className="setup-illustration"><div className="setup-tile tile-one"><FolderKanban size={22}/></div><div className="setup-tile tile-two"><ListTodo size={20}/></div><div className="setup-tile tile-three"><Users size={20}/></div><div className="setup-orbit"></div></div><div className="setup-copy"><div className="eyebrow">YOUR NEXT STEP</div><h2>Set up your workspace</h2><p>Create your first project, add teammates who already have accounts, and start planning tasks in your workspace.</p><Link className="button button-primary" to={user?'/projects':'/register'}><span>＋</span> {user?'Create a project':'Create your account'}</Link></div><div className="setup-progress"><span>01</span><i></i><span>02</span><i></i><span>03</span></div></section>}
    {user&&summary&&<section className="dashboard-activity" id="dashboard-activity"><div className="section-title"><div><div className="eyebrow">WORKSPACE HISTORY</div><h2>Recent activity</h2></div><Activity size={16}/></div>{summary.recentActivity?.length?summary.recentActivity.map(item=><article className="dashboard-activity-row" key={item.id}><span className="activity-avatar">{item.userName.slice(0,1).toUpperCase()}</span><div><p><b>{item.userName}</b> {item.description}</p><small>{item.projectName||'Workspace'} · {new Date(item.createdAt).toLocaleString()}</small></div></article>):<p className="dashboard-activity-empty">Project updates will appear here as your team works.</p>}</section>}
    <footer className="footer"><span>Made for teams that get things done.</span><span>{APP_NAME} <span className="footer-dot">·</span> v0.1</span></footer>
  </div>
}

function PlaceholderPage({title}) {
  return <div className="page-content"><div className="eyebrow">WORKSPACE</div><h1>{title}</h1><p className="placeholder-copy">This area is being built as part of the SprintStack project.</p><div className="placeholder-panel"><span className="card-icon violet"><FolderKanban size={18}/></span><div><b>Coming up next</b><p>Project and task data will appear here once the API and database modules are in place.</p></div></div></div>
}

function ProtectedRoute({ user, loading, children }) {
  const location = useLocation()
  if (loading) return <div className="page-content"><div className="loading-state">Checking your session…</div></div>
  return user ? children : <Navigate to="/login" state={{ from: location }} replace />
}

function QuickSearch({ onClose, onSearch }) {
  const [query, setQuery] = useState('')
  const inputRef = useRef(null)
  useEffect(() => { inputRef.current?.focus() }, [])
  return <div className="quick-search-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) onClose() }}>
    <form className="quick-search-dialog" role="dialog" aria-modal="true" aria-labelledby="quick-search-title" onSubmit={event => { event.preventDefault(); onSearch(query) }}>
      <label id="quick-search-title" htmlFor="quick-search-input">Search tasks</label>
      <div className="quick-search-input"><Search size={17}/><input ref={inputRef} id="quick-search-input" value={query} onChange={event => setQuery(event.target.value)} placeholder="Type a task title, label, or detail…"/><kbd>ESC</kbd></div>
      <p>Search across tasks you can access.</p>
    </form>
  </div>
}

export default function App() {
  const [health,setHealth] = useState('checking')
  const [user,setUser] = useState(null)
  const [authLoading,setAuthLoading] = useState(Boolean(sessionStorage.getItem('sprintstack.token')))
  const [summary,setSummary] = useState(null)
  const [summaryError,setSummaryError] = useState('')
  const [searchOpen,setSearchOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  useEffect(()=>{api.get('/health').then(()=>setHealth('online')).catch(()=>setHealth('offline'))},[])
  useEffect(()=>{const token=sessionStorage.getItem('sprintstack.token');if(!token){setAuthLoading(false);return}api.get('/auth/me').then(({data})=>{setUser(data);sessionStorage.setItem('sprintstack.email',data.email)}).catch(()=>{sessionStorage.removeItem('sprintstack.token');sessionStorage.removeItem('sprintstack.email')}).finally(()=>setAuthLoading(false))},[])
  useEffect(()=>{const onExpired=()=>{setUser(null);setSummary(null);setAuthLoading(false);navigate('/login',{replace:true})};window.addEventListener('sprintstack:session-expired',onExpired);return()=>window.removeEventListener('sprintstack:session-expired',onExpired)},[navigate])
  useEffect(()=>{if(!user){setSummary(null);return} api.get('/dashboard').then(({data})=>{setSummary(data);setSummaryError('')}).catch(e=>setSummaryError(e.response?.data?.error||'Dashboard data could not be loaded.'))},[user,location.pathname])
  useEffect(()=>{const onKeyDown=event=>{if((event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==='k'){event.preventDefault();setSearchOpen(true)}if(event.key==='Escape')setSearchOpen(false)};window.addEventListener('keydown',onKeyDown);return()=>window.removeEventListener('keydown',onKeyDown)},[])
  function logout() { sessionStorage.removeItem('sprintstack.token'); sessionStorage.removeItem('sprintstack.email'); setUser(null); navigate('/login') }
  function searchTasks(query) { setSearchOpen(false); navigate(`/tasks${query.trim()?`?q=${encodeURIComponent(query.trim())}`:''}`) }
  return <><Routes><Route path="/login" element={<AuthPage mode="login" onAuthenticated={setUser}/>}/><Route path="/register" element={<AuthPage mode="register" onAuthenticated={setUser}/>}/><Route path="*" element={<div className="app-shell"><Sidebar user={user} onLogout={logout}/><main className="main-area"><header className="topbar"><div className="breadcrumbs"><span>Workspace</span><b>/</b><strong>Overview</strong></div><div className="top-actions"><button className="icon-button search-button" aria-label="Search tasks" onClick={()=>setSearchOpen(true)}><Search size={18}/><span>Search</span><kbd>⌘ K</kbd></button><span className="top-divider"></span><NotificationsMenu user={user}/>{user ? <span className="top-avatar">{user.fullName?.slice(0,1).toUpperCase()}</span> : <Link className="text-button" to="/login">Sign in</Link>}</div></header><Routes><Route path="/" element={<EmptyDashboard health={health} user={user} summary={summary} summaryError={summaryError}/>}/><Route path="/projects" element={<ProtectedRoute user={user} loading={authLoading}><ProjectsPage/></ProtectedRoute>}/><Route path="/projects/:projectId" element={<ProtectedRoute user={user} loading={authLoading}><ProjectDetailPage/></ProtectedRoute>}/><Route path="/tasks" element={<ProtectedRoute user={user} loading={authLoading}><TasksPage user={user}/></ProtectedRoute>}/><Route path="/issues" element={<ProtectedRoute user={user} loading={authLoading}><IssuesPage user={user}/></ProtectedRoute>}/><Route path="/members" element={<ProtectedRoute user={user} loading={authLoading}><MembersPage/></ProtectedRoute>}/><Route path="/settings" element={<ProtectedRoute user={user} loading={authLoading}><SettingsPage user={user} onUserUpdated={setUser}/></ProtectedRoute>}/></Routes></main></div>}/></Routes>{searchOpen&&<QuickSearch onClose={()=>setSearchOpen(false)} onSearch={searchTasks}/>}</>
}
