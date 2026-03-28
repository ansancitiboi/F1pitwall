import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { signup } from '../api'

export default function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', nickname: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await signup(form.email, form.password, form.nickname)
      navigate('/login')
    } catch (err: any) {
      setError(err.response?.data?.message ?? '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-sm mx-auto mt-20">
      <h1 className="text-2xl font-bold text-white mb-8 text-center">회원가입</h1>

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
          type="text"
          placeholder="닉네임"
          value={form.nickname}
          onChange={(e) => setForm({ ...form, nickname: e.target.value })}
          required
          className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-4 py-3 outline-none focus:border-red-500 transition-colors"
        />
        <input
          type="password"
          placeholder="비밀번호 (8자 이상)"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
          minLength={8}
          className="w-full bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-4 py-3 outline-none focus:border-red-500 transition-colors"
        />

        {error && <p className="text-red-400 text-sm">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-red-600 hover:bg-red-500 disabled:opacity-50 text-white font-medium py-3 rounded-lg transition-colors"
        >
          {loading ? '처리 중...' : '회원가입'}
        </button>
      </form>

      <p className="text-center text-gray-400 text-sm mt-6">
        이미 계정이 있으신가요?{' '}
        <Link to="/login" className="text-red-400 hover:underline">로그인</Link>
      </p>
    </div>
  )
}
