import React from 'react'

export const Login: React.FC = () => {
  const onLogin = () => {
    localStorage.setItem('token','demo-token')
    alert('Token simulé stocké.')
  }
  return (
    <div style={{padding:24}}>
      <h2>Connexion</h2>
      <p>Simulation OAuth2 client_credentials</p>
      <button onClick={onLogin}>Se connecter</button>
    </div>
  )
}
