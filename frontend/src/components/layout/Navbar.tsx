import { Link, NavLink, useNavigate } from 'react-router-dom'
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

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `text-sm font-medium transition-colors pb-0.5 border-b-2 ${
      isActive
        ? 'text-white border-[#e10600]'
        : 'text-slate-400 border-transparent hover:text-white'
    }`

  return (
    <nav className="bg-[#1a1f2e] border-b border-[#252d3d] sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-5 h-14 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0">
          <div className="w-7 h-7 bg-[#e10600] rounded flex items-center justify-center">
            <span className="text-white font-black text-xs">F1</span>
          </div>
          <span className="text-white font-bold text-sm tracking-wide">PIT WALL</span>
        </Link>

        {/* Nav Links */}
        <div className="flex items-center gap-6">
          <NavLink to="/drivers" className={linkClass}>Drivers</NavLink>
          <NavLink to="/races" className={linkClass}>Races</NavLink>
          <NavLink to="/live" className={({ isActive }) =>
            `text-sm font-medium transition-colors pb-0.5 border-b-2 flex items-center gap-1.5 ${
              isActive ? 'text-white border-[#e10600]' : 'text-slate-400 border-transparent hover:text-white'
            }`
          }>
            <span className="w-1.5 h-1.5 rounded-full bg-[#e10600] animate-pulse" />
            Live
          </NavLink>
          <NavLink to="/news" className={linkClass}>News</NavLink>
        </div>

        {/* Auth */}
        <div className="flex items-center gap-3">
          {isLoggedIn ? (
            <>
              <NavLink to="/me" className={linkClass}>My Page</NavLink>
              <button
                onClick={handleLogout}
                className="text-xs text-slate-500 hover:text-slate-300 transition-colors"
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="text-sm font-medium bg-[#e10600] hover:bg-red-600 text-white px-4 py-1.5 rounded transition-colors"
            >
              Login
            </Link>
          )}
        </div>
      </div>
    </nav>
  )
}
