import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { CalendarDays, Check, ChevronDown, CirclePlus, ClipboardCheck, Download, GripVertical, LoaderCircle, Plus, Search } from 'lucide-react'
import api from '../lib/api'
import TaskDetailsDialog from '../components/TaskDetailsDialog'

const columns = [
  { status: 'TODO', label: 'To do', tone: 'todo' },
  { status: 'IN_PROGRESS', label: 'In progress', tone: 'progress' },
  { status: 'IN_REVIEW', label: 'In review', tone: 'review' },
  { status: 'COMPLETED', label: 'Completed', tone: 'done' },
]

function downloadTasksCsv(tasks, projectName) {
  const columns = [
    ['Title', task => task.title], ['Description', task => task.description], ['Project', task => task.projectName],
    ['Status', task => task.status], ['Priority', task => task.priority], ['Assignee', task => task.assigneeName],
    ['Labels', task => task.labels?.join(', ')], ['Start date', task => task.startDate], ['Due date', task => task.dueDate],
    ['Estimate (hours)', task => task.estimatedEffort], ['Created by', task => task.createdByName],
  ]
  const escape = value => {
    let text = String(value ?? '')
    if (/^[\s]*[=+@\-\t\r]/.test(text)) text = `'${text}`
    return `"${text.replaceAll('"', '""')}"`
  }
  const csv = [columns.map(([header]) => escape(header)), ...tasks.map(task => columns.map(([, value]) => escape(value(task))))]
    .map(row => row.join(',')).join('\r\n')
  const blobUrl = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  const filePart = (projectName || 'tasks').normalize('NFKD').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toLowerCase() || 'tasks'
  link.href = blobUrl
  link.download = `${filePart}-tasks.csv`
  document.body.append(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(blobUrl)
}

function TaskDialog({ projectId, onClose, onCreated }) {
  const [form,setForm] = useState({title:'',description:'',priority:'MEDIUM',startDate:'',dueDate:'',estimatedEffort:'',assigneeId:'',labels:''})
  const [members,setMembers]=useState([])
  const [busy,setBusy] = useState(false)
  const [error,setError] = useState('')
  useEffect(()=>{api.get(`/projects/${projectId}/members`).then(({data})=>setMembers(data)).catch(()=>{})},[projectId])
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('')
    try { const {data}=await api.post(`/projects/${projectId}/tasks`,{...form,assigneeId:form.assigneeId?Number(form.assigneeId):null,startDate:form.startDate||null,dueDate:form.dueDate||null,estimatedEffort:form.estimatedEffort?Number(form.estimatedEffort):null,labels:form.labels.split(',').map(label=>label.trim()).filter(Boolean)}); onCreated(data) }
    catch(e) { setError(e.response?.data?.error||'Could not create the task. Please try again.') }
    finally { setBusy(false) }
  }
  return <div className="dialog-backdrop" onMouseDown={e=>{if(e.target===e.currentTarget)onClose()}}><section className="dialog-card" role="dialog" aria-modal="true" aria-labelledby="task-dialog-title"><div className="dialog-heading"><div><span className="eyebrow">NEW TASK</span><h2 id="task-dialog-title">Add a task</h2></div><button className="icon-button" onClick={onClose} aria-label="Close">×</button></div><form className="dialog-form" onSubmit={submit}><label>Task title<input autoFocus required maxLength="180" value={form.title} onChange={e=>setForm({...form,title:e.target.value})} placeholder="What needs to get done?"/></label><label>Description <span className="field-hint">Optional</span><textarea rows="3" maxLength="4000" value={form.description} onChange={e=>setForm({...form,description:e.target.value})} placeholder="Add details or acceptance criteria"/></label><div className="form-row"><label>Priority<select value={form.priority} onChange={e=>setForm({...form,priority:e.target.value})}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option><option value="URGENT">Urgent</option></select></label><label>Assignee<select value={form.assigneeId} onChange={e=>setForm({...form,assigneeId:e.target.value})}><option value="">Unassigned</option>{members.map(m=><option key={m.userId} value={m.userId}>{m.fullName}</option>)}</select></label></div><div className="form-row"><label>Start date<input type="date" value={form.startDate} onChange={e=>setForm({...form,startDate:e.target.value})}/></label><label>Deadline<input type="date" value={form.dueDate} onChange={e=>setForm({...form,dueDate:e.target.value})}/></label></div><label>Estimate in hours <span className="field-hint">Optional</span><input type="number" min="0" step="1" value={form.estimatedEffort} onChange={e=>setForm({...form,estimatedEffort:e.target.value})} placeholder="e.g. 4"/></label><label>Labels <span className="field-hint">Optional · comma-separated, up to 10</span><input maxLength="650" value={form.labels} onChange={e=>setForm({...form,labels:e.target.value})} placeholder="frontend, bug, sprint-1"/></label>{error&&<p className="form-error" role="alert">{error}</p>}<div className="dialog-actions"><button type="button" className="button button-light" onClick={onClose}>Cancel</button><button className="button button-primary" disabled={busy}>{busy&&<LoaderCircle className="spinner" size={14}/>} Create task</button></div></form></section></div>
}

export default function TasksPage({user}) {
  const [searchParams]=useSearchParams()
  const requestedProject=searchParams.get('project')
  const [projects,setProjects]=useState([])
  const [projectId,setProjectId]=useState('')
  const [tasks,setTasks]=useState([])
  const [query,setQuery]=useState(()=>searchParams.get('q')||'')
  const [status,setStatus]=useState('ALL')
  const [priority,setPriority]=useState('ALL')
  const [mine,setMine]=useState(false)
  const [overdue,setOverdue]=useState(false)
  const [loading,setLoading]=useState(true)
  const [error,setError]=useState('')
  const [dialog,setDialog]=useState(false)
  const [dragged,setDragged]=useState(null)
  const [selectedTask,setSelectedTask]=useState(null)
  useEffect(()=>{api.get('/projects').then(({data})=>{setProjects(data);if(data.length)setProjectId(String(data.find(p=>String(p.id)===requestedProject)?.id||data[0].id))}).catch(e=>setError(e.response?.status===401?'Sign in to see your tasks.':'Could not load projects. Check the API connection.')).finally(()=>setLoading(false))},[])
  useEffect(()=>{if(!projectId)return;setLoading(true);api.get(`/projects/${projectId}/tasks`,{params:{q:query||undefined,status:status==='ALL'?undefined:status,priority:priority==='ALL'?undefined:priority,mine:mine||undefined,overdue:overdue||undefined}}).then(({data})=>setTasks(data)).catch(e=>setError(e.response?.data?.error||'Could not load tasks.')).finally(()=>setLoading(false))},[projectId,query,status,priority,mine,overdue])
  async function moveTask(taskId,nextStatus) {
    const previous=tasks
    setTasks(items=>items.map(task=>task.id===taskId?{...task,status:nextStatus}:task))
    try { const {data}=await api.patch(`/tasks/${taskId}/status`,{status:nextStatus});setTasks(items=>items.map(task=>task.id===taskId?data:task)) }
    catch(e) { setTasks(previous);setError(e.response?.data?.error||'Could not update the task status.') }
  }
  const visible=tasks.filter(task=>`${task.title} ${task.description||''}`.toLowerCase().includes(query.toLowerCase())||task.labels?.some(label=>label.toLowerCase().includes(query.toLowerCase())))
  return <div className="page-content data-page"><div className="page-heading"><div><div className="eyebrow">PLAN AND DELIVER</div><h1>Task board</h1><p>Move work through each step as your team makes progress.</p></div><div className="task-heading-actions"><button className="button button-light" onClick={()=>downloadTasksCsv(visible,projects.find(project=>String(project.id)===projectId)?.name)} disabled={!visible.length} title="Export tasks matching the current filters"><Download size={15}/> Export CSV</button><button className="button button-primary" onClick={()=>setDialog(true)} disabled={!projectId}><Plus size={15}/> New task</button></div></div>
    <div className="data-toolbar"><div className="search-field"><Search size={16}/><input aria-label="Search tasks" placeholder="Search tasks..." value={query} onChange={e=>setQuery(e.target.value)}/></div><label className="select-with-icon"><span className="sr-only">Project</span><select aria-label="Choose project" value={projectId} onChange={e=>setProjectId(e.target.value)}><option value="">Choose a project</option>{projects.map(p=><option key={p.id} value={p.id}>{p.name}</option>)}</select><ChevronDown size={14}/></label><select aria-label="Filter tasks by status" value={status} onChange={e=>setStatus(e.target.value)}><option value="ALL">All statuses</option>{columns.map(c=><option key={c.status} value={c.status}>{c.label}</option>)}</select><select aria-label="Filter tasks by priority" value={priority} onChange={e=>setPriority(e.target.value)}><option value="ALL">All priorities</option>{['LOW','MEDIUM','HIGH','URGENT'].map(p=><option key={p}>{p}</option>)}</select><label className="check-filter"><input type="checkbox" checked={mine} onChange={e=>setMine(e.target.checked)}/> Assigned to me</label><label className="check-filter"><input type="checkbox" checked={overdue} onChange={e=>setOverdue(e.target.checked)}/> Overdue</label></div>
    {error&&<div className="inline-error" role="alert">{error} {error.includes('Sign in')&&<Link to="/login">Sign in</Link>}</div>}
    {loading?<div className="loading-state"><LoaderCircle className="spinner" size={18}/> Loading tasks…</div>:projects.length===0?<div className="empty-state"><span className="empty-icon"><ClipboardCheck size={22}/></span><h2>No project to plan in yet</h2><p>Create a project before adding tasks.</p><Link className="button button-primary" to="/projects"><Plus size={15}/> Create project</Link></div>:<div className="kanban-board">{columns.map(column=>{const items=visible.filter(task=>task.status===column.status);return <section className="kanban-column" key={column.status} onDragOver={e=>e.preventDefault()} onDrop={e=>{e.preventDefault();if(dragged)moveTask(dragged,column.status);setDragged(null)}}><header className="column-heading"><span className={`column-dot ${column.tone}`}></span><h2>{column.label}</h2><span className="column-count">{items.length}</span><button className="icon-button add-task-quiet" aria-label={`Add task in ${column.label}`} onClick={()=>setDialog(true)}><CirclePlus size={16}/></button></header><div className="column-cards">{items.map(task=><article className="task-card" key={task.id} draggable onClick={()=>setSelectedTask(task)} onDragStart={()=>setDragged(task.id)} onDragEnd={()=>setDragged(null)}><div className="task-card-top"><span className={`priority priority-${task.priority.toLowerCase()}`}>{task.priority}</span><GripVertical size={14} className="grip" aria-hidden="true"/></div><h3>{task.title}</h3>{task.description&&<p>{task.description}</p>}{task.labels?.length>0&&<div className="task-labels">{task.labels.map(label=><span className="task-label" key={label}>{label}</span>)}</div>}<div className="task-card-foot">{task.dueDate?<span className="task-date"><CalendarDays size={13}/>{new Date(`${task.dueDate}T00:00:00`).toLocaleDateString()}</span>:<span className="task-date muted">No deadline</span>}{task.assigneeName&&<span className="assignee-badge" title={task.assigneeName}>{task.assigneeName.split(' ').map(n=>n[0]).join('').slice(0,2).toUpperCase()}</span>}</div>{column.status!=='COMPLETED'&&<button className="complete-task" onClick={e=>{e.stopPropagation();moveTask(task.id,columns[columns.findIndex(c=>c.status===column.status)+1].status)}}><Check size={13}/> Move forward <ChevronDown size={12}/></button>}</article>)}{items.length===0&&<div className="column-empty">Drop a task here</div>}</div></section>})}</div>}
    {dialog&&projectId&&<TaskDialog projectId={projectId} onClose={()=>setDialog(false)} onCreated={task=>{setTasks(items=>[...items,task]);setDialog(false)}}/>}{selectedTask&&<TaskDetailsDialog task={selectedTask} user={user} onClose={()=>setSelectedTask(null)} onUpdated={task=>{setTasks(items=>items.map(item=>item.id===task.id?task:item));setSelectedTask(task)}} onDeleted={id=>{setTasks(items=>items.filter(item=>item.id!==id));setSelectedTask(null)}}/>}
  </div>
}
