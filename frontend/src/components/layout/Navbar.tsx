import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { logout as logoutApi } from '../../api'
import { useAuthStore } from '../../store/auth'

export default function Navbar() {
  const navigate = useNavigate()
  const { isLoggedIn, logout } = useAuthStore()
  const [menuOpen, setMenuOpen] = useState(false)

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem('refreshToken') ?? ''
    try { await logoutApi(refreshToken) } catch { /* ignore */ }
    logout()
    navigate('/login')
    setMenuOpen(false)
  }

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `text-sm font-medium transition-colors pb-0.5 border-b-2 ${
      isActive ? 'text-white border-[#e10600]' : 'text-slate-400 border-transparent hover:text-white'
    }`

  const mobileLinkClass = ({ isActive }: { isActive: boolean }) =>
    `block px-4 py-3 text-sm font-medium border-b border-[#252d3d] last:border-0 transition-colors ${
      isActive ? 'text-white bg-[#1e2535]' : 'text-slate-400 hover:text-white hover:bg-[#1e2535]'
    }`

  return (
    <nav className="bg-[#1a1f2e] border-b border-[#252d3d] sticky top-0 z-50">
      <div className="max-w-[1400px] mx-auto px-4 sm:px-6 lg:px-8 h-14 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2.5 shrink-0" onClick={() => setMenuOpen(false)}>
          <div className="w-7 h-7 bg-[#e10600] rounded flex items-center justify-center">
            <span className="text-white font-black text-xs">F1</span>
          </div>
          <span className="text-white font-bold text-sm tracking-wide">PIT WALL</span>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-6">
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

        {/* Desktop Auth */}
        <div className="hidden md:flex items-center gap-3">
          {isLoggedIn ? (
            <>
              <NavLink to="/me" className={linkClass}>My Page</NavLink>
              <button onClick={handleLogout} className="text-xs text-slate-500 hover:text-slate-300 transition-colors">
                Logout
              </button>
            </>
          ) : (
            <Link to="/login" className="text-sm font-medium bg-[#e10600] hover:bg-red-600 text-white px-4 py-1.5 rounded transition-colors">
              Login
            </Link>
          )}
        </div>

        {/* Mobile Hamburger */}
        <button
          className="md:hidden flex flex-col gap-1.5 p-2"
          onClick={() => setMenuOpen((v) => !v)}
          aria-label="메뉴"
        >
          <span className={`block w-5 h-0.5 bg-slate-400 transition-transform duration-200 ${menuOpen ? 'rotate-45 translate-y-2' : ''}`} />
          <span className={`block w-5 h-0.5 bg-slate-400 transition-opacity duration-200 ${menuOpen ? 'opacity-0' : ''}`} />
          <span className={`block w-5 h-0.5 bg-slate-400 transition-transform duration-200 ${menuOpen ? '-rotate-45 -translate-y-2' : ''}`} />
        </button>
      </div>

      {/* Mobile Menu */}
      {menuOpen && (
        <div className="md:hidden bg-[#1a1f2e] border-t border-[#252d3d]">
          <NavLink to="/drivers" className={mobileLinkClass} onClick={() => setMenuOpen(false)}>Drivers</NavLink>
          <NavLink to="/races" className={mobileLinkClass} onClick={() => setMenuOpen(false)}>Races</NavLink>
          <NavLink to="/live" className={mobileLinkClass} onClick={() => setMenuOpen(false)}>
            <span className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[#e10600] animate-pulse" />
              Live
            </span>
          </NavLink>
          <NavLink to="/news" className={mobileLinkClass} onClick={() => setMenuOpen(false)}>News</NavLink>
          {isLoggedIn ? (
            <>
              <NavLink to="/me" className={mobileLinkClass} onClick={() => setMenuOpen(false)}>My Page</NavLink>
              <button onClick={handleLogout} className="block w-full text-left px-4 py-3 text-sm text-slate-500 hover:text-white hover:bg-[#1e2535] transition-colors">
                Logout
              </button>
            </>
          ) : (
            <Link to="/login" className="block px-4 py-3 text-sm font-medium text-[#e10600]" onClick={() => setMenuOpen(false)}>
              Login
            </Link>
          )}
        </div>
      )}
    </nav>
  )
}
