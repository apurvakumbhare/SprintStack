import { useCallback, useEffect, useState } from 'react'
import { ExternalLink, GitCommitHorizontal, Github, LoaderCircle, RefreshCw, Unlink } from 'lucide-react'
import api from '../lib/api'

export default function GitHubActivityPanel({ project, projectId, isOwner, onRepositoryChanged }) {
  const [repositoryUrl, setRepositoryUrl] = useState(project.githubRepoUrl || '')
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const linked = Boolean(project.githubRepoUrl)

  useEffect(() => setRepositoryUrl(project.githubRepoUrl || ''), [project.githubRepoUrl])

  const refresh = useCallback(async () => {
    if (!project.githubRepoUrl) return
    setLoading(true)
    setError('')
    try {
      const { data } = await api.get(`/projects/${projectId}/github`)
      setItems(data)
    } catch (failure) {
      setError(failure.response?.data?.error || 'Could not load GitHub activity.')
    } finally { setLoading(false) }
  }, [project.githubRepoUrl, projectId])

  useEffect(() => {
    refresh()
    if (!project.githubRepoUrl) return undefined
    const timer = window.setInterval(refresh, 300_000)
    return () => window.clearInterval(timer)
  }, [refresh, project.githubRepoUrl])

  async function saveRepository(event) {
    event.preventDefault()
    setSaving(true); setError(''); setMessage('')
    try {
      const { data } = await api.put(`/projects/${projectId}/github/repository`, { repositoryUrl })
      onRepositoryChanged(data.repositoryUrl)
      setMessage('Repository linked. Recent activity is now visible to project members.')
    } catch (failure) {
      setError(failure.response?.data?.error || 'Could not link this repository.')
    } finally { setSaving(false) }
  }

  async function removeRepository() {
    if (!window.confirm('Remove the GitHub repository from this project?')) return
    setSaving(true); setError(''); setMessage('')
    try {
      await api.delete(`/projects/${projectId}/github/repository`)
      onRepositoryChanged(null)
      setItems([])
      setMessage('GitHub repository removed from this project.')
    } catch (failure) {
      setError(failure.response?.data?.error || 'Could not remove this repository.')
    } finally { setSaving(false) }
  }

  return <section className="detail-panel github-panel">
    <div className="panel-heading"><div><h2><Github size={17}/> GitHub activity</h2><p>Recent commits and pull requests. Activity refreshes about every five minutes.</p></div>{linked&&<button className="button button-light" onClick={refresh} disabled={loading} aria-label="Refresh GitHub activity"><RefreshCw size={14} className={loading?'spinner':''}/> Refresh</button>}</div>
    {isOwner ? <form className="github-link-form" onSubmit={saveRepository}>
      <label htmlFor="github-repository-url">Public GitHub repository</label>
      <div className="github-link-controls"><input id="github-repository-url" type="url" required maxLength="300" value={repositoryUrl} onChange={event=>setRepositoryUrl(event.target.value)} placeholder="https://github.com/owner/repository"/><button className="button button-primary" disabled={saving||!repositoryUrl.trim()}>{saving?<LoaderCircle className="spinner" size={14}/>:<Github size={14}/>} {linked?'Save link':'Link repository'}</button>{linked&&<button className="button button-light github-remove" type="button" onClick={removeRepository} disabled={saving}><Unlink size={14}/> Remove</button>}</div>
      <small>The project owner manages this link. Activity is shared with project members.</small>
    </form> : !linked ? <div className="panel-empty">The project owner has not linked a GitHub repository yet.</div> : null}
    {error&&<div className="inline-error" role="alert">{error}</div>}{message&&<p className="form-success github-message">{message}</p>}
    {linked&&<>
      <a className="github-repository-link" href={project.githubRepoUrl} target="_blank" rel="noreferrer"><Github size={15}/>{project.githubRepoUrl}<ExternalLink size={13}/></a>
      {loading&&!items.length?<div className="loading-state"><LoaderCircle className="spinner" size={16}/> Loading GitHub activity…</div>
        : items.length ? <div className="github-commit-list">{items.map(item=><article className="github-commit" key={item.id}><span className="github-commit-icon"><GitCommitHorizontal size={16}/></span><div className="github-commit-copy"><a href={item.htmlUrl} target="_blank" rel="noreferrer"><b>{item.title}</b><ExternalLink size={12}/></a><small>{item.type} · {item.actor}{item.ref&&<> · {item.ref}</>}{item.createdAt&&<> · {new Date(item.createdAt).toLocaleString()}</>}</small></div></article>)}</div>
        : !loading&&!error?<div className="panel-empty">No recent repository updates found.</div>:null}
    </>}
  </section>
}
