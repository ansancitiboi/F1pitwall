import { useState, useEffect } from 'react'

export function useAuthStore() {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('accessToken'))

  useEffect(() => {
    const handleStorage = () => setIsLoggedIn(!!localStorage.getItem('accessToken'))
    window.addEventListener('storage', handleStorage)
    return () => window.removeEventListener('storage', handleStorage)
  }, [])

  const login = (accessToken: string, refreshToken: string) => {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    setIsLoggedIn(true)
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setIsLoggedIn(false)
  }

  return { isLoggedIn, login, logout }
}
