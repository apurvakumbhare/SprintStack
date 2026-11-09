import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Activity, LoaderCircle, Search } from 'lucide-react'
import api from '../lib/api'

export default function ActivityPage() {
  const [items, setItems] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/activity')
      .then(({ data }) => setItems(data))
      .catch(failure => setError(failure.response?.data?.error || 'Could not load workspace activity.'))
      .finally(() => setLoading(false))
  }, [])

  const visible = useMemo(() => items.filter(item =>
    `${item.userName} ${item.projectName || ''} ${item.description} ${item.actionType}`.toLowerCase().includes(query.trim().toLowerCase())
  ), [items, query])

  return <div className="page-content data-page activity-page">
    <div className="page-heading"><div><div className="eyebrow">WORKSPACE HISTORY</div><h1>Activity</h1><p>Recent updates across the projects you belong to.</p></div></div>
    <div className="members-toolbar"><div className="search-field"><Search size={16}/><input aria-label="Search activity" placeholder="Search updates or projects…" value={query} onChange={event => setQuery(event.target.value)}/></div></div>
    {error && <div className="inline-error" role="alert">{error}</div>}
    {loading ? <div className="loading-state"><LoaderCircle className="spinner" size={18}/> Loading activity…</div>
      : visible.length ? <section className="activity-feed" aria-label="Recent workspace activity">{visible.map(item => <article className="activity-feed-row" key={item.id}>
        <span className="activity-avatar">{item.userName?.slice(0, 1).toUpperCase()}</span><div className="activity-feed-copy"><p><b>{item.userName}</b> {item.description}</p><small>{item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Recently'}{item.projectName && <> · {item.projectId ? <Link to={`/projects/${item.projectId}`}>{item.projectName}</Link> : item.projectName}</>}</small></div>
      </article>)}</section>
      : <div className="empty-state"><span className="empty-icon"><Activity size={22}/></span><h2>{items.length ? 'No matching updates' : 'No activity yet'}</h2><p>{items.length ? 'Try another search term.' : 'Changes made in your projects will show up here.'}</p></div>}
  </div>
}
