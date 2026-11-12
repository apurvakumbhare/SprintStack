import { lazy, Suspense, useEffect, useRef, useState } from 'react'
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import {
  Activity, Bell, ChevronDown, ClipboardCheck, FolderKanban, ListTodo, Search, Users,
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
import HelpPage from './pages/HelpPage'
import ActivityPage from './pages/ActivityPage'
import NotificationsMenu from './components/NotificationsMenu'
import ThemeToggle from './components/ThemeToggle'
import MagicBento from './components/MagicBento'
import ProfileAvatar from './components/ProfileAvatar'
import ClickSpark from './components/ClickSpark'
import OptionWheel from './components/OptionWheel'
import TechText from './components/TechText'
const DashboardCharts = lazy(() => import('./components/DashboardCharts'))

const navigation = [
  { label: 'Overview', to: '/' },
  { label: 'Projects', to: '/projects' },
  { label: 'My tasks', to: '/tasks' },
  { label: 'Issues', to: '/issues' },
  { label: 'Members', to: '/members' },
]

const sidebarOptions = [
  ...navigation,
  { label: 'Activity', to: '/activity' },
  { label: 'Help & support', to: '/help' },
  { label: 'Settings', to: '/settings' },
]

function Sidebar({ user, photo, onLogout }) {
  const location = useLocation()
  const navigate = useNavigate()
  const [wheelOpen, setWheelOpen] = useState(false)
  const selectedIndex = Math.max(0, sidebarOptions.findIndex(option =>
    option.to === location.pathname || (option.to === '/projects' && location.pathname.startsWith('/projects/'))
  ))

  useEffect(() => {
    if (!wheelOpen) return undefined
    const closeOnEscape = event => { if (event.key === 'Escape') setWheelOpen(false) }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [wheelOpen])

  return <aside className="sidebar">
    <Link className="brand" to="/" aria-label={`${APP_NAME} home`}><span className="brand-mark">{APP_NAME.slice(0,1).toUpperCase()}</span><span>{APP_NAME}</span></Link>
    <div className="workspace"><span className="workspace-avatar">{APP_NAME.slice(0,1).toUpperCase()}</span><span className="workspace-copy"><b>Your workspace</b><small>Getting started</small></span><ChevronDown size={15}/></div>
    <div className="sidebar-wheel-title"><span className="nav-label">NAVIGATION</span><button className="sidebar-wheel-toggle" type="button" aria-expanded={wheelOpen} aria-label={wheelOpen?'Close navigation':'Open navigation'} onClick={()=>setWheelOpen(open=>!open)}>{wheelOpen?'×':'☰'}</button></div>
    {wheelOpen&&<button className="sidebar-wheel-backdrop" type="button" aria-label="Close navigation" onClick={()=>setWheelOpen(false)}/>}
    <div className={`sidebar-wheel-panel${wheelOpen?' sidebar-wheel-panel--open':''}`} aria-label="Workspace navigation">
      <OptionWheel key={selectedIndex} items={sidebarOptions.map(option=>option.label)} defaultSelected={selectedIndex} onChange={index=>{navigate(sidebarOptions[index].to);setWheelOpen(false)}} textColor="#8b91a0" activeColor="#5558d6" side="left" fontSize={1} spacing={1.42} curve={0.9} tilt={6} blur={0.45} fade={0.19} minOpacity={0.16} smoothing={260} inset={22} draggable className="sidebar-option-wheel"/>
    </div>
    <div className="sidebar-bottom">{user ? <div className="profile"><ProfileAvatar className="profile-avatar" name={user.fullName} photo={photo}/><span className="profile-copy"><b>{user.fullName}</b><small>{user.role?.replaceAll('_',' ')}</small></span><button className="icon-button" aria-label="Sign out" title="Sign out" onClick={onLogout}><ChevronDown size={16}/></button></div> : <div className="profile"><span className="profile-avatar">?</span><span className="profile-copy"><b>Not signed in</b><small>Account setup pending</small></span></div>}</div>
  </aside>
}

function EmptyDashboard({health, user, summary, summaryError}) {
  return <div className="page-content">
    <div className="page-heading"><div><div className="eyebrow">TEAM PROJECTS & PRODUCTIVITY</div><h1>{user ? `Welcome, ${user.fullName}` : `Welcome to ${APP_NAME}`} <span className="wave">✦</span></h1><p>Bring your projects, tasks, and team into one place.</p></div><button className="button button-light" onClick={()=>document.getElementById('dashboard-activity')?.scrollIntoView({behavior:'smooth'})}><Activity size={16}/> Activity log</button></div>
    <div className="connect-banner"><span className={`status-dot ${health==='online'?'online':''}`}></span><div><b>{health==='online'?'API connected':`${APP_NAME} is ready to get started`}</b><p>{health==='online'?'Your workspace service is responding.':'Create your first project to bring your team and tasks together.'}</p></div><span className="banner-tag">{health==='online'?'LIVE':'WORKSPACE'}</span></div>
    {user&&summary&&<section className="metric-grid" aria-label="Workspace statistics"><article><span>Total projects</span><b>{summary.totalProjects}</b><small>{summary.activeProjects} active</small></article><article><span>Completed projects</span><b>{summary.completedProjects}</b><small>Finished projects</small></article><article><span>Total tasks</span><b>{summary.totalTasks}</b><small>{summary.pendingTasks} still open</small></article><article><span>Completed tasks</span><b>{summary.completedTasks}</b><small>Across your projects</small></article><article><span>Due in 7 days</span><b>{summary.dueSoonTasks}</b><small>Open tasks approaching</small></article><article><span>Overdue tasks</span><b className={summary.overdueTasks?'metric-alert':''}>{summary.overdueTasks}</b><small>Past their due date</small></article></section>}
    {user&&summary&&<Suspense fallback={<div className="chart-loading">Loading analytics…</div>}><DashboardCharts summary={summary}/></Suspense>}
    {summaryError&&<div className="inline-error" role="alert">{summaryError}</div>}
    <MagicBento summary={summary}/>
    {summary?.projectProgress?.length>0?<section className="progress-section"><div className="section-title"><div><div className="eyebrow">YOUR WORK</div><h2>Project progress</h2></div><Link className="text-button" to="/projects">All projects <span>→</span></Link></div><div className="progress-list">{summary.projectProgress.slice(0,5).map(project=><article className="progress-row" key={project.id}><div className="progress-name"><span className="progress-project-icon"><FolderKanban size={15}/></span><div><b>{project.name}</b><small>{project.totalTasks} tasks · {project.status.toLowerCase().replace('_',' ')}</small></div></div><div className="progress-bar"><i style={{width:`${project.completionPercent}%`}}/></div><b className="progress-value">{project.completionPercent}%</b></article>)}</div></section>:<section className="setup-card"><div className="setup-illustration"><div className="setup-tile tile-one"><FolderKanban size={22}/></div><div className="setup-tile tile-two"><ListTodo size={20}/></div><div className="setup-tile tile-three"><Users size={20}/></div><div className="setup-orbit"></div></div><div className="setup-copy"><div className="eyebrow">YOUR NEXT STEP</div><h2>Set up your workspace</h2><p>Create your first project, add teammates who already have accounts, and start planning tasks in your workspace.</p><Link className="button button-primary gradient-button" to={user?'/projects':'/register'}><span>＋</span> {user?'Create a project':'Create your account'}</Link></div><div className="setup-progress"><span>01</span><i></i><span>02</span><i></i><span>03</span></div></section>}
    {user&&summary&&<section className="dashboard-activity" id="dashboard-activity"><div className="section-title"><div><div className="eyebrow">WORKSPACE HISTORY</div><h2>Recent activity</h2></div><Activity size={16}/></div>{summary.recentActivity?.length?summary.recentActivity.map(item=><article className="dashboard-activity-row" key={item.id}><ProfileAvatar className="activity-avatar" name={item.userName} photo={item.profilePhoto}/><div><p><b>{item.userName}</b> {item.description}</p><small>{item.projectName||'Workspace'} · {new Date(item.createdAt).toLocaleString()}</small></div></article>):<p className="dashboard-activity-empty">Project updates will appear here as your team works.</p>}</section>}
        {user&&summary&&<section className="dashboard-notifications"><div className="section-title"><div><div className="eyebrow">YOUR INBOX</div><h2>Recent notifications</h2></div><Bell size={16}/></div>{summary.recentNotifications?.length?summary.recentNotifications.map(item=>{const target=item.entityType==='TASK'?`/tasks?task=${item.entityId}`:item.entityType==='ISSUE'?`/issues?issue=${item.entityId}`:item.entityType==='PROJECT'?`/projects/${item.entityId}`:item.entityType==='USER'?'/members':null;return <article className={`dashboard-notification-row ${item.read?'read':''}`} key={item.id}><span className="notification-point"/><div><p>{item.message}</p><small>{new Date(item.createdAt).toLocaleString()}</small></div>{target&&<Link to={target}>Open</Link>}</article>}):<p className="dashboard-notifications-empty">You’re all caught up. New updates will appear here.</p>}</section>}<footer className="footer"><span>Made for teams that get things done.</span><span>{APP_NAME} <span className="footer-dot">·</span> v0.1</span></footer>
  </div>
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
  const [profilePhoto,setProfilePhoto] = useState(null)
  const [authLoading,setAuthLoading] = useState(Boolean(sessionStorage.getItem('sprintstack.token')))
  const [summary,setSummary] = useState(null)
  const [summaryError,setSummaryError] = useState('')
  const [searchOpen,setSearchOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const currentPage=location.pathname.startsWith('/projects/')?'Project details':({'/':'Overview','/projects':'Projects','/tasks':'My tasks','/issues':'Issues','/members':'Members','/activity':'Activity','/help':'Help & support','/settings':'Settings'}[location.pathname]||'Overview')
  const displayTitle=location.pathname.startsWith('/projects/')?'PROJECT':({'/':'OVERVIEW','/projects':'PROJECTS','/tasks':'TASKS','/issues':'ISSUES','/members':'MEMBERS','/activity':'ACTIVITY','/help':'HELP','/settings':'SETTINGS'}[location.pathname]||'OVERVIEW')
  useEffect(()=>{api.get('/health').then(()=>setHealth('online')).catch(()=>setHealth('offline'))},[])
  useEffect(()=>{document.title=APP_NAME;document.querySelector('meta[name="description"]')?.setAttribute('content',`${APP_NAME} — team project and productivity management.`)},[])
  useEffect(()=>{const token=sessionStorage.getItem('sprintstack.token');if(!token){setAuthLoading(false);return}api.get('/auth/me').then(async({data})=>{setUser(data);sessionStorage.setItem('sprintstack.email',data.email);const photoResponse=await api.get('/auth/photo').catch(()=>null);setProfilePhoto(photoResponse?.data?.photo||null)}).catch(()=>{sessionStorage.removeItem('sprintstack.token');sessionStorage.removeItem('sprintstack.email');setProfilePhoto(null)}).finally(()=>setAuthLoading(false))},[])
  useEffect(()=>{const onExpired=()=>{setUser(null);setProfilePhoto(null);setSummary(null);setAuthLoading(false);navigate('/login',{replace:true})};window.addEventListener('sprintstack:session-expired',onExpired);return()=>window.removeEventListener('sprintstack:session-expired',onExpired)},[navigate])
  useEffect(()=>{if(!user){setSummary(null);return} api.get('/dashboard').then(({data})=>{setSummary(data);setSummaryError('')}).catch(e=>setSummaryError(e.response?.data?.error||'Dashboard data could not be loaded.'))},[user,location.pathname])
  useEffect(()=>{const onKeyDown=event=>{if((event.metaKey||event.ctrlKey)&&event.key.toLowerCase()==='k'){event.preventDefault();setSearchOpen(true)}if(event.key==='Escape')setSearchOpen(false)};window.addEventListener('keydown',onKeyDown);return()=>window.removeEventListener('keydown',onKeyDown)},[])
  function logout() { sessionStorage.removeItem('sprintstack.token'); sessionStorage.removeItem('sprintstack.email'); setUser(null); setProfilePhoto(null); navigate('/login') }
  function onAuthenticated(nextUser, nextPhoto) { setUser(nextUser); setProfilePhoto(nextPhoto ?? null) }
  function searchTasks(query) { setSearchOpen(false); navigate(`/tasks${query.trim()?`?q=${encodeURIComponent(query.trim())}`:''}`) }
  return <ClickSpark className="app-click-spark" style={{height:"auto",minHeight:"100vh"}} sparkColor="#696cf1" sparkSize={6} sparkRadius={25} sparkCount={7} sparkWidth={1.8} duration={430} shape="star" glow={0.25}><Routes><Route path="/login" element={<AuthPage mode="login" onAuthenticated={onAuthenticated}/>}/><Route path="/register" element={<AuthPage mode="register" onAuthenticated={onAuthenticated}/>}/><Route path="*" element={<div className="app-shell"><Sidebar user={user} photo={profilePhoto} onLogout={logout}/><main className="main-area"><header className="topbar"><div className="breadcrumbs"><span>Workspace</span><b>/</b><strong>{currentPage}</strong></div><div className="top-actions"><button className="icon-button search-button" aria-label="Search tasks" onClick={()=>setSearchOpen(true)}><Search size={18}/><span>Search</span><kbd>⌘ K</kbd></button><span className="top-divider"></span><ThemeToggle/><NotificationsMenu user={user}/>{user ? <ProfileAvatar className="top-avatar" name={user.fullName} photo={profilePhoto}/> : <Link className="text-button" to="/login">Sign in</Link>}</div></header><div className="page-tech-title" aria-label={displayTitle}><TechText text={displayTitle} fontFamily="Manrope, sans-serif" fontWeight={750} fontSize={72} letterSpacing={0.025} color="#82889a" accentColor="#6669e8" align="left" reach={150} softness={0.72} strokeWidth={1.6} reveal="letter" specks={8} selection draggable={false} bounce={0.3} sweep clickKick intro="metrics" introDuration={1.35}/></div><Routes><Route path="/" element={<EmptyDashboard health={health} user={user} summary={summary} summaryError={summaryError}/>}/><Route path="/projects" element={<ProtectedRoute user={user} loading={authLoading}><ProjectsPage/></ProtectedRoute>}/><Route path="/projects/:projectId" element={<ProtectedRoute user={user} loading={authLoading}><ProjectDetailPage/></ProtectedRoute>}/><Route path="/tasks" element={<ProtectedRoute user={user} loading={authLoading}><TasksPage user={user}/></ProtectedRoute>}/><Route path="/issues" element={<ProtectedRoute user={user} loading={authLoading}><IssuesPage user={user}/></ProtectedRoute>}/><Route path="/members" element={<ProtectedRoute user={user} loading={authLoading}><MembersPage/></ProtectedRoute>}/><Route path="/help" element={<HelpPage/>}/><Route path="/activity" element={<ProtectedRoute user={user} loading={authLoading}><ActivityPage/></ProtectedRoute>}/><Route path="/settings" element={<ProtectedRoute user={user} loading={authLoading}><SettingsPage user={user} photo={profilePhoto} onUserUpdated={(nextUser,nextPhoto)=>{setUser(nextUser);if(nextPhoto!==undefined)setProfilePhoto(nextPhoto)}}/></ProtectedRoute>}/></Routes></main></div>}/></Routes>{searchOpen&&<QuickSearch onClose={()=>setSearchOpen(false)} onSearch={searchTasks}/>}</ClickSpark>
}
