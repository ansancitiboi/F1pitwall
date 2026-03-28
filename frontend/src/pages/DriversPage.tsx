import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getDrivers, getMyDrivers, subscribe, unsubscribe } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import { useAuthStore } from '../store/auth'

export default function DriversPage() {
  const { isLoggedIn } = useAuthStore()
  const queryClient = useQueryClient()

  const { data: drivers, isLoading } = useQuery({
    queryKey: ['drivers'],
    queryFn: getDrivers,
  })

  const { data: myDrivers } = useQuery({
    queryKey: ['myDrivers'],
    queryFn: getMyDrivers,
    enabled: isLoggedIn,
  })

  const subscribedIds = new Set(myDrivers?.map((d) => d.id) ?? [])

  const subscribeMutation = useMutation({
    mutationFn: subscribe,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['myDrivers'] }),
  })

  const unsubscribeMutation = useMutation({
    mutationFn: unsubscribe,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['myDrivers'] }),
  })

  const toggleSubscribe = (driverId: number) => {
    if (subscribedIds.has(driverId)) {
      unsubscribeMutation.mutate(driverId)
    } else {
      subscribeMutation.mutate(driverId)
    }
  }

  if (isLoading) return <LoadingSpinner />

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-lg font-bold text-white">드라이버</h1>
        {!isLoggedIn && (
          <p className="text-xs text-slate-500">구독하려면 <a href="/login" className="text-[#e10600]">로그인</a>하세요</p>
        )}
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
        {drivers?.map((driver) => {
          const isSubscribed = subscribedIds.has(driver.id)
          return (
            <div
              key={driver.id}
              className={`bg-[#1a1f2e] rounded-xl border transition-colors overflow-hidden ${
                isSubscribed ? 'border-[#e10600]/40' : 'border-[#252d3d] hover:border-[#2d3748]'
              }`}
            >
              <div className="bg-[#1e2535] h-24 flex items-center justify-center">
                {driver.headshotUrl ? (
                  <img
                    src={driver.headshotUrl}
                    alt={driver.lastName}
                    className="h-24 w-full object-cover object-top"
                    onError={(e) => {
                      const el = e.target as HTMLImageElement
                      el.style.display = 'none'
                      el.parentElement!.innerHTML = `<span class="text-3xl font-black text-slate-600">${driver.driverNumber}</span>`
                    }}
                  />
                ) : (
                  <span className="text-3xl font-black text-slate-600">{driver.driverNumber}</span>
                )}
              </div>

              <div className="p-3">
                <div className="flex items-center gap-1.5 mb-0.5">
                  <span className="text-xs font-bold text-[#e10600] font-mono">#{driver.driverNumber}</span>
                  <span className="text-xs font-bold text-white bg-[#252d3d] px-1.5 rounded font-mono">{driver.code}</span>
                </div>
                <p className="text-sm font-semibold text-white leading-tight">
                  {driver.firstName} {driver.lastName}
                </p>
                <p className="text-xs text-slate-500 mt-0.5 truncate">{driver.teamName}</p>

                {isLoggedIn && (
                  <button
                    onClick={() => toggleSubscribe(driver.id)}
                    className={`w-full mt-2.5 py-1.5 rounded text-xs font-medium transition-colors ${
                      isSubscribed
                        ? 'bg-[#e10600]/10 text-[#e10600] hover:bg-[#e10600]/20'
                        : 'bg-[#252d3d] text-slate-400 hover:bg-[#2d3748] hover:text-white'
                    }`}
                  >
                    {isSubscribed ? '구독 중 ✓' : '+ 구독'}
                  </button>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
