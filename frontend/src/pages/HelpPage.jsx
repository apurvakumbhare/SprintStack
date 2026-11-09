import { Link } from 'react-router-dom'
import { ArrowRight, BookOpen, CircleHelp, FolderKanban, ListTodo, ShieldAlert, Users } from 'lucide-react'

const guides = [
  { title: 'Set up a project', detail: 'Create a project, add dates and priority, then open its detail page to manage the team.', to: '/projects', icon: FolderKanban },
  { title: 'Plan and track tasks', detail: 'Choose a project, add tasks with assignees and deadlines, and move cards across the board.', to: '/tasks', icon: ListTodo },
  { title: 'Work with your team', detail: 'Review project members and their roles. Project owners and managers can update access.', to: '/members', icon: Users },
  { title: 'Report an issue', detail: 'Choose a project, report a bug, set its severity, and follow it through resolution.', to: '/issues', icon: ShieldAlert },
]

const questions = [
  ['How do I add a teammate?', 'Open a project, select Members, and search for an existing account by name or email. Choose a role and add them to the project.'],
  ['Why can’t I see a project?', 'Projects are visible to their members. Ask a project owner or manager to add your existing account.'],
  ['How do I change my password?', 'Open Settings, enter your current password, then choose a new password with at least eight characters.'],
  ['Where can I find recent changes?', 'Use Activity from the sidebar or the recent activity section on the overview page.'],
]

export default function HelpPage() {
  return <div className="page-content data-page help-page">
    <div className="page-heading"><div><div className="eyebrow">GUIDES AND ANSWERS</div><h1>Help & support</h1><p>Quick guidance for getting your workspace set up and keeping work moving.</p></div></div>
    <section className="help-guides" aria-label="Quick guides">{guides.map(({ title, detail, to, icon: Icon }) => <Link className="help-guide" to={to} key={title}><span className="card-icon violet"><Icon size={18}/></span><span><b>{title}</b><small>{detail}</small></span><ArrowRight size={15}/></Link>)}</section>
    <section className="help-faq"><div className="section-title"><div><div className="eyebrow">COMMON QUESTIONS</div><h2>Frequently asked</h2></div><CircleHelp size={17}/></div>{questions.map(([question, answer]) => <details key={question}><summary>{question}</summary><p>{answer}</p></details>)}</section>
    <div className="help-contact"><BookOpen size={17}/><p>Need account help? Check your profile and password options in Settings.</p><Link className="text-button" to="/settings">Open settings <span>→</span></Link></div>
  </div>
}
