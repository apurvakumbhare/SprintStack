import { useEffect, useState } from 'react'
import { LoaderCircle, MessageSquare, Pencil, Send, Trash2, X } from 'lucide-react'
import api from '../lib/api'

const statuses = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'COMPLETED']
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

export default function TaskDetailsDialog({ task, user, onClose, onUpdated, onDeleted }) {
  const [comments, setComments] = useState([])
  const [body, setBody] = useState('')
  const [editingComment, setEditingComment] = useState(null)
  const [editingTask, setEditingTask] = useState(false)
  const [form, setForm] = useState(null)
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get(`/tasks/${task.id}/comments`).then(({ data }) => setComments(data))
      .catch(e => setError(e.response?.data?.error || 'Could not load comments.'))
      .finally(() => setLoading(false))
    api.get(`/projects/${task.projectId}/members`).then(({ data }) => setMembers(data)).catch(() => {})
  }, [task.id, task.projectId])

  function beginTaskEdit() {
    setForm({ title: task.title, description: task.description || '', assigneeId: task.assigneeId || '',
      priority: task.priority, status: task.status, startDate: task.startDate || '', dueDate: task.dueDate || '',
      estimatedEffort: task.estimatedEffort ?? '', labels: (task.labels || []).join(', ') })
    setEditingTask(true)
    setError('')
  }

  async function saveTask(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const { data } = await api.put(`/tasks/${task.id}`, {
        ...form,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        startDate: form.startDate || null,
        dueDate: form.dueDate || null,
        estimatedEffort: form.estimatedEffort === '' ? null : Number(form.estimatedEffort),
        labels: form.labels.split(',').map(label => label.trim()).filter(Boolean),
      })
      onUpdated(data)
      setEditingTask(false)
    } catch (e) {
      setError(e.response?.data?.error || 'Could not update this task.')
    } finally {
      setBusy(false)
    }
  }

  async function deleteTask() {
    if (!window.confirm(`Delete task “${task.title}”? This cannot be undone.`)) return
    setBusy(true)
    setError('')
    try {
      await api.delete(`/tasks/${task.id}`)
      onDeleted(task.id)
    } catch (e) {
      setError(e.response?.data?.error || 'Could not delete this task.')
    } finally {
      setBusy(false)
    }
  }

  async function submitComment(event) {
    event.preventDefault()
    if (!body.trim()) return
    setBusy(true)
    setError('')
    try {
      const { data } = editingComment
        ? await api.put(`/tasks/${task.id}/comments/${editingComment}`, { body })
        : await api.post(`/tasks/${task.id}/comments`, { body })
      setComments(items => editingComment ? items.map(item => item.id === data.id ? data : item) : [...items, data])
      setBody('')
      setEditingComment(null)
    } catch (e) {
      setError(e.response?.data?.error || 'Could not save this comment.')
    } finally {
      setBusy(false)
    }
  }

  async function removeComment(comment) {
    if (!window.confirm('Delete this comment?')) return
    try {
      await api.delete(`/tasks/${task.id}/comments/${comment.id}`)
      setComments(items => items.filter(item => item.id !== comment.id))
    } catch (e) {
      setError(e.response?.data?.error || 'Could not delete this comment.')
    }
  }

  return <div className="dialog-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}>
    <section className="dialog-card task-detail-dialog" role="dialog" aria-modal="true" aria-labelledby="task-detail-title">
      <div className="dialog-heading"><div><span className="eyebrow">{task.projectName}</span><h2 id="task-detail-title">{task.title}</h2></div>
        <div className="task-detail-actions"><button className="icon-button" onClick={beginTaskEdit} aria-label="Edit task"><Pencil size={15}/></button><button className="icon-button danger-action" onClick={deleteTask} aria-label="Delete task"><Trash2 size={15}/></button><button className="icon-button" onClick={onClose} aria-label="Close"><X size={17}/></button></div>
      </div>
      {editingTask ? <form className="dialog-form task-edit-form" onSubmit={saveTask}>
        <label>Title<input required maxLength="180" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}/></label>
        <label>Description<textarea rows="3" maxLength="4000" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}/></label>
        <div className="form-row"><label>Status<select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>{statuses.map(status => <option key={status} value={status}>{status.replaceAll('_', ' ')}</option>)}</select></label><label>Priority<select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>{priorities.map(priority => <option key={priority} value={priority}>{priority}</option>)}</select></label></div>
        <label>Assignee<select value={form.assigneeId} onChange={e => setForm({ ...form, assigneeId: e.target.value })}><option value="">Unassigned</option>{members.map(member => <option key={member.userId} value={member.userId}>{member.fullName}</option>)}</select></label>
        <div className="form-row"><label>Start date<input type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })}/></label><label>Deadline<input type="date" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })}/></label></div>
        <div className="form-row"><label>Estimate in hours<input type="number" min="0" step="1" value={form.estimatedEffort} onChange={e => setForm({ ...form, estimatedEffort: e.target.value })}/></label><label>Labels <span className="field-hint">Comma-separated, up to 10</span><input maxLength="650" value={form.labels} onChange={e => setForm({ ...form, labels: e.target.value })}/></label></div>
        {error && <p className="form-error" role="alert">{error}</p>}<div className="dialog-actions"><button type="button" className="button button-light" onClick={() => setEditingTask(false)}>Cancel</button><button className="button button-primary" disabled={busy}>{busy && <LoaderCircle className="spinner" size={14}/>} Save task</button></div>
      </form> : <>
        <p className="task-detail-description">{task.description || 'No description added.'}</p>
        {task.labels?.length > 0 && <div className="task-labels task-detail-labels">{task.labels.map(label => <span className="task-label" key={label}>{label}</span>)}</div>}
        <div className="task-detail-meta"><span className={`priority priority-${task.priority.toLowerCase()}`}>{task.priority}</span><span>{task.status.replace('_', ' ')}</span><span>{task.dueDate ? `Due ${new Date(`${task.dueDate}T00:00:00`).toLocaleDateString()}` : 'No deadline'}</span>{task.assigneeName && <span>Assigned to {task.assigneeName}</span>}</div>
        <div className="comments-heading"><MessageSquare size={15}/><b>Comments</b><span>{comments.length}</span></div><div className="comments-list">{loading ? <div className="loading-state"><LoaderCircle className="spinner" size={16}/> Loading comments…</div> : comments.length === 0 ? <div className="panel-empty">No comments yet. Add the first update.</div> : comments.map(comment => <article className="comment-row" key={comment.id}><span className="activity-avatar">{comment.authorName.slice(0, 1).toUpperCase()}</span><div className="comment-content"><div className="comment-author"><b>{comment.authorName}</b><small>{new Date(comment.createdAt).toLocaleString()}</small></div><p>{comment.body}</p>{user?.id === comment.authorId && <div className="comment-actions"><button onClick={() => { setEditingComment(comment.id); setBody(comment.body) }}><Pencil size={12}/> Edit</button><button onClick={() => removeComment(comment)}><Trash2 size={12}/> Delete</button></div>}</div></article>)}</div>
        <form className="comment-form" onSubmit={submitComment}><textarea rows="2" maxLength="4000" value={body} onChange={e => setBody(e.target.value)} placeholder={editingComment ? 'Edit your comment…' : 'Write a comment…'}/><div>{error && <span className="form-error">{error}</span>}{editingComment && <button type="button" className="text-button" onClick={() => { setEditingComment(null); setBody('') }}>Cancel edit</button>}<button className="button button-primary" disabled={busy || !body.trim()}>{busy ? <LoaderCircle className="spinner" size={14}/> : <Send size={13}/>} {editingComment ? 'Save' : 'Comment'}</button></div></form>
      </>}
      {!editingTask && error && <p className="form-error" role="alert">{error}</p>}
    </section>
  </div>
}
