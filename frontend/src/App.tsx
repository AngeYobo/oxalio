import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { NavBar } from './components/NavBar'
import { Login } from './pages/Login'
import { Invoices } from './pages/Invoices'

const App: React.FC = () => (
  <BrowserRouter>
    <NavBar/>
    <Routes>
      <Route path="/" element={<Invoices/>}/>
      <Route path="/login" element={<Login/>}/>
    </Routes>
  </BrowserRouter>
)

createRoot(document.getElementById('root')!).render(<App/>)
