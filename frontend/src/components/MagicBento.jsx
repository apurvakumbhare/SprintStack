import { useEffect, useRef, useState } from 'react'
import { ArrowUpRight, CalendarClock, FolderKanban, ListTodo, Users } from 'lucide-react'
import { Link } from 'react-router-dom'

function BentoCard({ item, index, visible }) {
  const Icon = item.icon
  function trackPointer(event) {
    const bounds = event.currentTarget.getBoundingClientRect()
    event.currentTarget.style.setProperty('--pointer-x', `${event.clientX - bounds.left}px`)
    event.currentTarget.style.setProperty('--pointer-y', `${event.clientY - bounds.top}px`)
  }
  return <Link className={`magic-bento-card ${item.size || ''} ${visible ? 'is-visible' : ''}`} style={{ '--bento-accent': item.accent, '--bento-delay': `${index * 90}ms` }} onPointerMove={trackPointer} onPointerLeave={event => { event.currentTarget.style.setProperty('--pointer-x', '50%'); event.currentTarget.style.setProperty('--pointer-y', '0%') }} to={item.to}>
    <span className="magic-bento-glow" aria-hidden="true"/>
    <div className="magic-bento-head"><span className="magic-bento-icon"><Icon size={17}/></span><ArrowUpRight size={15} className="magic-bento-arrow"/></div>
    <div className="magic-bento-main"><div className="magic-bento-label">{item.label}</div><div className="magic-bento-value">{item.value}</div><div className="magic-bento-description">{item.description}</div></div>
    {item.progress != null&&<div className="magic-bento-progress" aria-label={`${item.progress}% complete`}><span style={{width:`${item.progress}%`}}/></div>}
    <div className="magic-bento-visual" aria-hidden="true">{item.visual}</div>
  </Link>
}

export default function MagicBento({ summary }) {
  const grid = useRef(null)
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const node = grid.current
    if (!node) return
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) { setVisible(true); observer.disconnect() }
    }, { threshold: 0.12 })
    observer.observe(node)
    return () => observer.disconnect()
  }, [])

  const totalTasks = summary?.totalTasks || 0
  const completedTasks = summary?.completedTasks || 0
  const completion = totalTasks ? Math.round(completedTasks / totalTasks * 100) : 0
  const projectProgress = summary?.projectProgress || []
  const avgProgress = projectProgress.length ? Math.round(projectProgress.reduce((sum, project) => sum + project.completionPercent, 0) / projectProgress.length) : 0
  const items = [
    { label: 'PROJECTS', value: summary ? summary.totalProjects : '—', description: summary ? `${summary.activeProjects} active · ${summary.completedProjects} completed` : 'Plan work in focused team spaces', icon: FolderKanban, accent: '#7775ee', to: '/projects', size: 'bento-wide', progress: summary ? avgProgress : null, visual: <div className="bento-orbit"><i/><i/><i/><b><FolderKanban size={20}/></b></div> },
    { label: 'TASKS', value: summary ? totalTasks : '—', description: summary ? `${completedTasks} done · ${summary.pendingTasks} still open` : 'Move work from planned to done', icon: ListTodo, accent: '#5fa8dc', to: '/tasks', progress: summary ? completion : null, visual: <div className="bento-bars"><i/><i/><i/><i/><i/><i/><i/></div> },
    { label: 'DUE SOON', value: summary ? summary.dueSoonTasks : '—', description: summary ? `${summary.overdueTasks} overdue tasks need attention` : 'See upcoming work and deadlines', icon: CalendarClock, accent: '#e7a552', to: '/tasks', visual: <div className="bento-calendar"><span>MON</span><span>TUE</span><span>WED</span><b>12</b><b>13</b><b>14</b></div> },
    { label: 'TEAM LOAD', value: summary ? `${summary.teamWorkload?.length || 0} members` : '—', description: 'Members with assigned tasks', icon: Users, accent: '#55b58b', to: '/members', visual: <div className="bento-people"><span><Users size={13}/></span><i/><i/><i/></div> },
  ]

  return <section className="magic-bento-section" aria-label="Workspace at a glance"><div className="magic-bento-title"><div><div className="eyebrow">WORKSPACE AT A GLANCE</div><h2>Your work, in focus</h2></div><span>LIVE OVERVIEW</span></div><div ref={grid} className={`magic-bento ${visible ? 'is-visible' : ''}`}>{items.map((item, index) => <BentoCard key={item.label} item={item} index={index} visible={visible}/>)}</div></section>
}
