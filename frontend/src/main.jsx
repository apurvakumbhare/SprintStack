import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import './styles.css'
import './theme.css'
import './auth.css'
import './data.css'
import './dashboard.css'
import './project-detail.css'
import './notifications.css'
import './analytics.css'
import './loading.css'
import './comments.css'
import './settings.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter><App /></BrowserRouter>
  </React.StrictMode>,
)
