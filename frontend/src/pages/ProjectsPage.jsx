import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Archive, FolderKanban, LoaderCircle, Plus, Search, X } from 'lucide-react'
import api from '../lib/api'

const statusLabels = { PLANNING: 'Planning', ACTIVE: 'Active', ON_HOLD: 'On hold', COMPLETED: 'Completed', ARCHIVED: 'Archived' }
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

function ProjectDialog({ onClose, onCreated }) {
  const [form, setForm] = useState({ name: '', description: '', priority: 'MEDIUM', startDate: '', dueDate: '' })
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('')
    try { const { data } = await api.post('/projects', { ...form, startDate: form.startDate || null, dueDate: form.dueDate || null }); onCreated(data) }
    catch (e) { setError(e.response?.data?.error || 'Could not create the project. Please try again.') }
    finally { setBusy(false) }
  }
  return <div className="dialog-backdrop" onMouseDown={(event)=>{if(event.target===event.currentTarget)onClose()}}><section className="dialog-card" role="dialog" aria-modal="true" aria-labelledby="project-dialog-title"><div className="dialog-heading"><div><span className="eyebrow">WORKSPACE</span><h2 id="project-dialog-title">Create a project</h2></div><button className="icon-button" onClick={onClose} aria-label="Close"><X size={18}/></button></div><form className="dialog-form" onSubmit={submit}><label>Project name<input autoFocus required maxLength="140" value={form.name} onChange={e=>setForm({...form,name:e.target.value})} placeholder="e.g. Website redesign"/></label><label>Description <span className="field-hint">Optional</span><textarea rows="3" maxLength="2000" value={form.description} onChange={e=>setForm({...form,description:e.target.value})} placeholder="What is this project about?"/></label><div className="form-row"><label>Priority<select value={form.priority} onChange={e=>setForm({...form,priority:e.target.value})}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option><option value="CRITICAL">Critical</option></select></label><label>Start date<input type="date" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})}/></label></div><label>Deadline<input type="date" value={form.dueDate} onChange={e=>setForm({...form,dueDate:e.target.value})}/></label>{error&&<p className="form-error" role="alert">{error}</p>}<div className="dialog-actions"><button type="button" className="button button-light" onClick={onClose}>Cancel</button><button className="button button-primary" disabled={busy}>{busy&&<LoaderCircle className="spinner" size={14}/>} Create project</button></div></form></section></div>
}

export default function ProjectsPage() {
  const navigate=useNavigate()
  const [projects,setProjects] = useState([])
  const [loading,setLoading] = useState(true)
  const [error,setError] = useState('')
  const [query,setQuery] = useState('')
  const [filter,setFilter] = useState('ALL')
  const [priority,setPriority] = useState('ALL')
  const [sort,setSort] = useState('updated')
  const [dialog,setDialog] = useState(false)
  useEffect(()=>{api.get('/projects').then(({data})=>setProjects(data)).catch(e=>setError(e.response?.status===401?'Sign in to see your projects.':'Could not load projects. Check that the API and database are available.')).finally(()=>setLoading(false))},[])
  const visible = useMemo(()=>{
    const filtered=projects.filter(p=>(filter==='ALL'||p.status===filter)&&(priority==='ALL'||p.priority===priority)&&`${p.name} ${p.description||''}`.toLowerCase().includes(query.trim().toLowerCase()))
    const priorityRank={CRITICAL:4,HIGH:3,MEDIUM:2,LOW:1}
    return filtered.sort((a,b)=>sort==='name'?a.name.localeCompare(b.name):sort==='due'?(a.dueDate||'9999-12-31').localeCompare(b.dueDate||'9999-12-31'):sort==='priority'?priorityRank[b.priority]-priorityRank[a.priority]:new Date(b.updatedAt)-new Date(a.updatedAt))
  },[projects,filter,priority,sort,query])
  async function archive(project) {
    if(!window.confirm(`Archive “${project.name}”?`)) return
    try { const {data}=await api.patch(`/projects/${project.id}/archive`); setProjects(items=>items.map(item=>item.id===data.id?data:item)) }
    catch(e) { setError(e.response?.data?.error||'Could not archive this project.') }
  }
  return <div className="page-content data-page"><div className="page-heading"><div><div className="eyebrow">PLAN AND ORGANIZE</div><h1>Projects</h1><p>Keep related work and teammates together.</p></div><button className="button button-primary" onClick={()=>setDialog(true)}><Plus size={15}/> New project</button></div>
    <div className="data-toolbar"><div className="search-field"><Search size={16}/><input aria-label="Search projects" placeholder="Search projects..." value={query} onChange={e=>setQuery(e.target.value)}/></div><select aria-label="Filter projects by status" value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All statuses</option>{Object.entries(statusLabels).map(([key,label])=><option key={key} value={key}>{label}</option>)}</select><select aria-label="Filter projects by priority" value={priority} onChange={e=>setPriority(e.target.value)}><option value="ALL">All priorities</option>{priorities.map(value=><option value={value} key={value}>{value}</option>)}</select><select aria-label="Sort projects" value={sort} onChange={e=>setSort(e.target.value)}><option value="updated">Recently updated</option><option value="due">Deadline</option><option value="priority">Priority</option><option value="name">Name</option></select></div>
    {error&&<div className="inline-error" role="alert">{error} {error.includes('Sign in')&&<Link to="/login">Sign in</Link>}</div>}
    {loading?<div className="loading-state"><LoaderCircle className="spinner" size={18}/> Loading projects…</div>:visible.length===0?<div className="empty-state"><span className="empty-icon"><FolderKanban size={22}/></span><h2>{projects.length?'No projects match your filters':'No projects yet'}</h2><p>{projects.length?'Try a different search or status filter.':'Create a project to give your team’s work a shared home.'}</p>{projects.length===0&&<button className="button button-primary" onClick={()=>setDialog(true)}><Plus size={15}/> Create your first project</button>}</div>:<div className="project-grid">{visible.map(project=><article className="project-card clickable" key={project.id} role="link" tabIndex="0" onClick={()=>navigate(`/projects/${project.id}`)} onKeyDown={e=>{if(e.key==='Enter')navigate(`/projects/${project.id}`)}}><div className="project-card-head"><span className="card-icon violet"><FolderKanban size={18}/></span><span className={`status-pill status-${project.status.toLowerCase()}`}>{statusLabels[project.status]||project.status}</span></div><h2>{project.name}</h2><p className="project-description">{project.description||'No description added.'}</p><div className="project-meta"><span>{project.priority.toLowerCase()} priority</span><span>{project.memberCount} {project.memberCount===1?'member':'members'}</span></div><div className="project-card-foot"><span>{project.dueDate?`Due ${new Date(`${project.dueDate}T00:00:00`).toLocaleDateString()}`:'No deadline'}</span>{project.status!=='ARCHIVED'&&project.members?.some(member=>member.email===sessionStorage.getItem('sprintstack.email')&&['OWNER','PROJECT_MANAGER'].includes(member.role))&&<button className="icon-button" onClick={e=>{e.stopPropagation();archive(project)}} aria-label={`Archive ${project.name}`} title="Archive project"><Archive size={15}/></button>}</div></article>)}</div>}
    {dialog&&<ProjectDialog onClose={()=>setDialog(false)} onCreated={project=>{setProjects(items=>[project,...items]);setDialog(false)}}/>}
  </div>
}
