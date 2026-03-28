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
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-white mb-6">마이페이지</h1>

      {/* Profile */}
      <div className="bg-[#1a1a1a] rounded-xl p-5 mb-6 border border-[#2a2a2a]">
        <p className="text-white font-semibold text-lg">{user.nickname}</p>
        <p className="text-gray-400 text-sm mt-1">{user.email}</p>
      </div>

      {/* Subscribed Drivers */}
      <section>
        <h2 className="text-lg font-semibold text-white mb-4">구독 드라이버</h2>

        {driversLoading ? (
          <LoadingSpinner />
        ) : drivers && drivers.length > 0 ? (
          <div className="space-y-2">
            {drivers.map((driver) => (
              <div
                key={driver.id}
                className="flex items-center bg-[#1a1a1a] rounded-xl px-5 py-4 border border-[#2a2a2a]"
              >
                {driver.headshotUrl && (
                  <img
                    src={driver.headshotUrl}
                    alt={driver.lastName}
                    className="w-10 h-10 rounded-full object-cover bg-[#2a2a2a] mr-4"
                    onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                  />
                )}
                <div className="flex-1">
                  <p className="text-white font-medium">
                    {driver.firstName} {driver.lastName}
                    <span className="ml-2 text-sm text-gray-400">#{driver.driverNumber}</span>
                  </p>
                  <p className="text-xs text-gray-500 mt-0.5">{driver.teamName}</p>
                </div>
                <button
                  onClick={() => unsubscribeMutation.mutate(driver.id)}
                  className="text-xs text-gray-500 hover:text-red-400 transition-colors"
                >
                  구독 해제
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-[#1a1a1a] rounded-xl p-8 text-center border border-[#2a2a2a]">
            <p className="text-gray-500 text-sm mb-3">구독 중인 드라이버가 없습니다</p>
            <a href="/drivers" className="text-red-400 text-sm hover:underline">
              드라이버 구독하러 가기 →
            </a>
          </div>
        )}
      </section>
    </div>
  )
}
