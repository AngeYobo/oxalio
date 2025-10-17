import React from 'react'
import { Link } from 'react-router-dom'

export const NavBar: React.FC = () => (
  <nav style={{display:'flex',gap:16,padding:12,borderBottom:'1px solid #eee'}}>
    <Link to="/">Factures</Link>
    <Link to="/login">Connexion</Link>
  </nav>
)
