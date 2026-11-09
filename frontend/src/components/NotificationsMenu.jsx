import { useEffect, useRef, useState } from 'react'
import { Bell, Check, CheckCheck, LoaderCircle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import api from '../lib/api'

export default function NotificationsMenu({user}){
  const [open,setOpen]=useState(false);const [items,setItems]=useState([]);const [unread,setUnread]=useState(0);const [loading,setLoading]=useState(false);const wrap=useRef(null);const navigate=useNavigate()
  useEffect(()=>{if(!user){setUnread(0);setItems([]);return}api.get('/notifications/unread-count').then(({data})=>setUnread(data.count)).catch(()=>{})},[user])
  useEffect(()=>{function close(e){if(wrap.current&&!wrap.current.contains(e.target))setOpen(false)}document.addEventListener('mousedown',close);return()=>document.removeEventListener('mousedown',close)},[])
  async function toggle(){if(!user){navigate('/login');return}const next=!open;setOpen(next);if(next){setLoading(true);try{const [list,count]=await Promise.all([api.get('/notifications'),api.get('/notifications/unread-count')]);setItems(list.data);setUnread(count.data.count)}catch{}finally{setLoading(false)}}}
  async function markRead(item){try{const {data}=await api.patch(`/notifications/${item.id}/read`);setItems(current=>current.map(n=>n.id===data.id?data:n));setUnread(current=>Math.max(0,current-1))}catch{}}
  async function markAll(){try{await api.patch('/notifications/read-all');setItems(current=>current.map(n=>({...n,read:true})));setUnread(0)}catch{}}
  return <div className="notification-wrap" ref={wrap}><button className="icon-button notification-button" aria-label={`Notifications${unread?`, ${unread} unread`:''}`} onClick={toggle}><Bell size={18}/>{unread>0&&<i/>}</button>{open&&<section className="notification-popover" aria-label="Notifications"><header><div><b>Notifications</b>{unread>0&&<span>{unread} new</span>}</div>{unread>0&&<button onClick={markAll}><CheckCheck size={13}/> Mark all read</button>}</header>{loading?<div className="notification-loading"><LoaderCircle className="spinner" size={17}/> Loading…</div>:items.length===0?<div className="notification-empty"><Bell size={19}/><b>You’re all caught up</b><small>New project updates will appear here.</small></div>:<div className="notification-list">{items.map(item=><article key={item.id} className={`notification-item ${item.read?'read':''}`}><span className="notification-point"/><div><p>{item.message}</p><small>{new Date(item.createdAt).toLocaleString()}</small></div>{!item.read&&<button aria-label="Mark as read" onClick={()=>markRead(item)}><Check size={14}/></button>}</article>)}</div>}</section>}</div>
}
