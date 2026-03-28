import { Link, useNavigate } from 'react-router-dom'
import { logout as logoutApi } from '../../api'
import { useAuthStore } from '../../store/auth'

export default function Navbar() {
  const navigate = useNavigate()
  const { isLoggedIn, logout } = useAuthStore()

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem('refreshToken') ?? ''
    try { await logoutApi(refreshToken) } catch { /* ignore */ }
    logout()
    navigate('/login')
  }

  return (
    <nav className="bg-[#1a1a1a] border-b border-[#2a2a2a] sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
        <Link to="/" className="text-red-500 font-bold text-xl tracking-tight">
          F1 PIT WALL
        </Link>

        <div className="flex items-center gap-6 text-sm text-gray-400">
          <Link to="/drivers" className="hover:text-white transition-colors">Drivers</Link>
          <Link to="/races" className="hover:text-white transition-colors">Races</Link>
          <Link to="/live" className="hover:text-white transition-colors flex items-center gap-1">
            <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse" />
            Live
          </Link>
          <Link to="/news" className="hover:text-white transition-colors">News</Link>

          {isLoggedIn ? (
            <>
              <Link to="/me" className="hover:text-white transition-colors">My Page</Link>
              <button
                onClick={handleLogout}
                className="text-gray-500 hover:text-white transition-colors"
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="bg-red-600 hover:bg-red-500 text-white px-3 py-1 rounded transition-colors"
            >
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  )
}
