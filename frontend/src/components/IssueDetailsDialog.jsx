import { useEffect, useState } from 'react'
import { LoaderCircle, MessageSquare, Pencil, Send, Trash2, X } from 'lucide-react'
import api from '../lib/api'
import ProfileAvatar from './ProfileAvatar'

const nextStatus = { OPEN: 'IN_PROGRESS', IN_PROGRESS: 'RESOLVED', RESOLVED: 'CLOSED' }
const statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const severities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const priorities = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

export default function IssueDetailsDialog({ issue, user, onClose, onUpdated }) {
  const [comments, setComments] = useState([])
  const [body, setBody] = useState('')
  const [editingComment, setEditingComment] = useState(null)
  const [editingIssue, setEditingIssue] = useState(false)
  const [form, setForm] = useState(null)
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get(`/issues/${issue.id}/comments`).then(({ data }) => setComments(data))
      .catch(e => setError(e.response?.data?.error || 'Could not load comments.'))
      .finally(() => setLoading(false))
    api.get(`/projects/${issue.projectId}/members`).then(({ data }) => setMembers(data)).catch(() => {})
  }, [issue.id, issue.projectId])

  function beginEdit() {
    setForm({ title: issue.title, description: issue.description || '', assigneeId: issue.assigneeId || '',
      severity: issue.severity, priority: issue.priority, status: issue.status })
    setEditingIssue(true)
    setError('')
  }

  async function saveIssue(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const { data } = await api.put(`/issues/${issue.id}`, {
        ...form,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        description: form.description || null,
      })
      onUpdated(data)
      setEditingIssue(false)
    } catch (e) {
      setError(e.response?.data?.error || 'Could not update this issue.')
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
        ? await api.put(`/issues/${issue.id}/comments/${editingComment}`, { body })
        : await api.post(`/issues/${issue.id}/comments`, { body })
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
      await api.delete(`/issues/${issue.id}/comments/${comment.id}`)
      setComments(items => items.filter(item => item.id !== comment.id))
    } catch (e) {
      setError(e.response?.data?.error || 'Could not delete this comment.')
    }
  }

  async function changeStatus() {
    const status = nextStatus[issue.status]
    if (!status) return
    try {
      const { data } = await api.patch(`/issues/${issue.id}/status`, { status })
      onUpdated(data)
    } catch (e) {
      setError(e.response?.data?.error || 'Could not update issue status.')
    }
  }

  return <div className="dialog-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) onClose() }}>
    <section className="dialog-card task-detail-dialog" role="dialog" aria-modal="true" aria-labelledby="issue-detail-title">
      <div className="dialog-heading"><div><span className="eyebrow">{issue.projectName} · {issue.severity}</span><h2 id="issue-detail-title">{issue.title}</h2></div>
        <div className="task-detail-actions"><button className="icon-button" onClick={beginEdit} aria-label="Edit issue"><Pencil size={15}/></button><button className="icon-button" onClick={onClose} aria-label="Close"><X size={17}/></button></div>
      </div>
      {editingIssue ? <form className="dialog-form task-edit-form" onSubmit={saveIssue}>
        <label>Title<input required maxLength="180" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}/></label>
        <label>Description<textarea rows="4" maxLength="4000" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}/></label>
        <div className="form-row"><label>Status<select value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>{statuses.map(status => <option key={status} value={status}>{status.replaceAll('_', ' ')}</option>)}</select></label><label>Severity<select value={form.severity} onChange={e => setForm({ ...form, severity: e.target.value })}>{severities.map(value => <option key={value} value={value}>{value}</option>)}</select></label></div>
        <div className="form-row"><label>Priority<select value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>{priorities.map(value => <option key={value} value={value}>{value}</option>)}</select></label><label>Assignee<select value={form.assigneeId} onChange={e => setForm({ ...form, assigneeId: e.target.value })}><option value="">Unassigned</option>{members.map(member => <option key={member.userId} value={member.userId}>{member.fullName}</option>)}</select></label></div>
        {error && <p className="form-error" role="alert">{error}</p>}<div className="dialog-actions"><button type="button" className="button button-light" onClick={() => setEditingIssue(false)}>Cancel</button><button className="button button-primary" disabled={busy}>{busy && <LoaderCircle className="spinner" size={14}/>} Save issue</button></div>
      </form> : <>
        <p className="task-detail-description">{issue.description || 'No additional description.'}</p>
        <div className="task-detail-meta"><span>{issue.status.replace('_', ' ')}</span><span>{issue.priority} priority</span><span>Reported by {issue.reporterName}</span>{issue.assigneeName && <span>Assigned to {issue.assigneeName}</span>}{nextStatus[issue.status] && <button className="text-button" onClick={changeStatus}>Move to {nextStatus[issue.status].toLowerCase().replace('_', ' ')}</button>}</div>
        <div className="comments-heading"><MessageSquare size={15}/><b>Comments</b><span>{comments.length}</span></div><div className="comments-list">{loading ? <div className="loading-state"><LoaderCircle className="spinner" size={16}/> Loading comments…</div> : comments.length === 0 ? <div className="panel-empty">No comments yet. Add the first update.</div> : comments.map(comment => <article className="comment-row" key={comment.id}><ProfileAvatar className="activity-avatar" name={comment.authorName} photo={comment.profilePhoto}/><div className="comment-content"><div className="comment-author"><b>{comment.authorName}</b><small>{new Date(comment.createdAt).toLocaleString()}</small></div><p>{comment.body}</p>{user?.id === comment.authorId && <div className="comment-actions"><button onClick={() => { setEditingComment(comment.id); setBody(comment.body) }}><Pencil size={12}/> Edit</button><button onClick={() => removeComment(comment)}><Trash2 size={12}/> Delete</button></div>}</div></article>)}</div>
        <form className="comment-form" onSubmit={submitComment}><textarea rows="2" maxLength="4000" value={body} onChange={e => setBody(e.target.value)} placeholder={editingComment ? 'Edit your comment…' : 'Write a comment…'}/><div>{error && <span className="form-error">{error}</span>}{editingComment && <button type="button" className="text-button" onClick={() => { setEditingComment(null); setBody('') }}>Cancel edit</button>}<button className="button button-primary" disabled={busy || !body.trim()}>{busy ? <LoaderCircle className="spinner" size={14}/> : <Send size={13}/>} {editingComment ? 'Save' : 'Comment'}</button></div></form>
      </>}
      {!editingIssue && error && <p className="form-error" role="alert">{error}</p>}
    </section>
  </div>
}
