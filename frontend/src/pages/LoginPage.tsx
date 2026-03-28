import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../api'
import { useAuthStore } from '../store/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login: loginStore } = useAuthStore()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const tokens = await login(form.email, form.password)
      loginStore(tokens.accessToken, tokens.refreshToken)
      navigate('/')
    } catch {
      setError('이메일 또는 비밀번호가 올바르지 않습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto mt-16">
      <div className="flex items-center justify-center gap-2 mb-8">
        <div className="w-8 h-8 bg-[#e10600] rounded flex items-center justify-center">
          <span className="text-white font-black text-xs">F1</span>
        </div>
        <span className="text-white font-bold">PIT WALL</span>
      </div>

      <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-6">
        <h1 className="text-lg font-bold text-white mb-5">로그인</h1>

        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            type="email"
            placeholder="이메일"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
            className="w-full bg-[#131620] border border-[#252d3d] text-white text-sm rounded-lg px-4 py-2.5 outline-none focus:border-[#e10600] transition-colors placeholder:text-slate-600"
          />
          <input
            type="password"
            placeholder="비밀번호"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
            className="w-full bg-[#131620] border border-[#252d3d] text-white text-sm rounded-lg px-4 py-2.5 outline-none focus:border-[#e10600] transition-colors placeholder:text-slate-600"
          />

          {error && (
            <p className="text-xs text-red-400 bg-red-500/10 px-3 py-2 rounded-lg">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#e10600] hover:bg-red-600 disabled:opacity-50 text-white font-medium text-sm py-2.5 rounded-lg transition-colors mt-1"
          >
            {loading ? '로그인 중...' : '로그인'}
          </button>
        </form>
      </div>

      <p className="text-center text-slate-500 text-sm mt-4">
        계정이 없으신가요?{' '}
        <Link to="/signup" className="text-[#e10600] hover:underline">회원가입</Link>
      </p>
    </div>
  )
}
