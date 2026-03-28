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
    <div className="max-w-sm mx-auto mt-20">
      <h1 className="text-2xl font-bold text-white mb-8 text-center">로그인</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="email"
          placeholder="이메일"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
          className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-4 py-3 outline-none focus:border-red-500 transition-colors"
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
          className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-4 py-3 outline-none focus:border-red-500 transition-colors"
        />

        {error && <p className="text-red-400 text-sm">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-red-600 hover:bg-red-500 disabled:opacity-50 text-white font-medium py-3 rounded-lg transition-colors"
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </form>

      <p className="text-center text-gray-400 text-sm mt-6">
        계정이 없으신가요?{' '}
        <Link to="/signup" className="text-red-400 hover:underline">회원가입</Link>
      </p>
    </div>
  )
}
