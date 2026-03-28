import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getMe, getMyDrivers, unsubscribe } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

export default function MyPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data: user, isLoading: userLoading } = useQuery({
    queryKey: ['me'],
    queryFn: getMe,
    retry: false,
  })

  const { data: drivers, isLoading: driversLoading } = useQuery({
    queryKey: ['myDrivers'],
    queryFn: getMyDrivers,
  })

  const unsubscribeMutation = useMutation({
    mutationFn: unsubscribe,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['myDrivers'] }),
  })

  if (userLoading) return <LoadingSpinner />

  if (!user) {
    navigate('/login')
    return null
  }

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-lg font-bold text-white mb-5">마이페이지</h1>

      {/* Profile Card */}
      <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-5 mb-6">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-[#252d3d] flex items-center justify-center">
            <span className="text-xl font-bold text-slate-300">
              {user.nickname.charAt(0).toUpperCase()}
            </span>
          </div>
          <div>
            <p className="text-white font-semibold">{user.nickname}</p>
            <p className="text-slate-500 text-sm mt-0.5">{user.email}</p>
          </div>
        </div>
      </div>

      {/* Subscribed Drivers */}
      <section>
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">구독 드라이버</h2>
          <span className="text-xs text-slate-600">{drivers?.length ?? 0}명</span>
        </div>

        {driversLoading ? <LoadingSpinner /> : drivers && drivers.length > 0 ? (
          <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
            {drivers.map((driver) => (
              <div
                key={driver.id}
                className="flex items-center px-4 py-3.5 border-b border-[#252d3d] last:border-0"
              >
                {driver.headshotUrl && (
                  <img
                    src={driver.headshotUrl}
                    alt={driver.lastName}
                    className="w-9 h-9 rounded-full object-cover bg-[#252d3d] mr-3 shrink-0"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                  />
                )}
                <span className="text-xs font-bold text-white bg-[#252d3d] rounded px-1.5 py-0.5 mr-3 font-mono shrink-0">
                  {driver.code}
                </span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-white font-medium">
                    {driver.firstName} {driver.lastName}
                  </p>
                  <p className="text-xs text-slate-500 truncate">{driver.teamName}</p>
                </div>
                <button
                  onClick={() => unsubscribeMutation.mutate(driver.id)}
                  className="text-xs text-slate-600 hover:text-red-400 transition-colors ml-3 shrink-0"
                >
                  구독 해제
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-10 text-center">
            <p className="text-slate-500 text-sm mb-3">구독 중인 드라이버가 없습니다</p>
            <a href="/drivers" className="text-[#e10600] text-sm hover:underline">
              드라이버 구독하러 가기 →
            </a>
          </div>
        )}
      </section>
    </div>
  )
}
