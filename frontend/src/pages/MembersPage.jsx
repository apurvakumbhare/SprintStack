import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Download, LoaderCircle, Search, Users } from 'lucide-react'
import api from '../lib/api'
import { downloadCsv } from '../lib/csv'

export default function MembersPage() {
  const [projects, setProjects] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/projects')
      .then(({ data }) => setProjects(data))
      .catch((failure) => setError(failure.response?.data?.error || 'Could not load project members.'))
      .finally(() => setLoading(false))
  }, [])

  const memberships = useMemo(() => projects.flatMap(project =>
    (project.members || []).map(member => ({ ...member, projectId: project.id, projectName: project.name }))
  ), [projects])
  const visible = memberships.filter(member =>
    `${member.fullName} ${member.email} ${member.role} ${member.projectName}`.toLowerCase().includes(query.trim().toLowerCase())
  )
  const uniqueMembers = new Set(memberships.map(member => member.id)).size

  return <div className="page-content data-page">
    <div className="page-heading"><div><div className="eyebrow">YOUR COLLABORATORS</div><h1>Members</h1><p>People and roles across the projects you can access.</p></div></div>
    <div className="member-summary"><span><b>{uniqueMembers}</b> {uniqueMembers === 1 ? 'person' : 'people'}</span><span><b>{projects.length}</b> {projects.length === 1 ? 'project' : 'projects'}</span></div>
    <div className="members-toolbar"><div className="search-field"><Search size={16}/><input aria-label="Search members" placeholder="Search people or projects…" value={query} onChange={event => setQuery(event.target.value)}/></div><button className="button button-light" disabled={!visible.length} onClick={()=>downloadCsv('project-members.csv',['Name','Email','Role','Project'],visible.map(member=>[member.fullName,member.email,member.role,member.projectName]))}><Download size={15}/> Export CSV</button></div>
    {error && <div className="inline-error" role="alert">{error}</div>}
    {loading ? <div className="loading-state"><LoaderCircle className="spinner" size={18}/> Loading members…</div>
      : visible.length ? <section className="member-directory" aria-label="Project members">{visible.map(member => <article className="directory-member" key={`${member.projectId}-${member.id}`}>
        <span className="profile-avatar">{member.fullName?.slice(0, 1).toUpperCase()}</span><span className="directory-member-copy"><b>{member.fullName}</b><small>{member.email}</small></span>
        <span className="member-role">{member.role.replaceAll('_', ' ')}</span><Link className="directory-project" to={`/projects/${member.projectId}`}>{member.projectName}</Link>
      </article>)}</section>
      : <div className="empty-state"><span className="empty-icon"><Users size={22}/></span><h2>{memberships.length ? 'No matching members' : 'No project members yet'}</h2><p>{memberships.length ? 'Try a different name, email, role, or project.' : 'Create a project to start building your team.'}</p>{!memberships.length && <Link className="button button-primary" to="/projects">View projects</Link>}</div>}
  </div>
}
