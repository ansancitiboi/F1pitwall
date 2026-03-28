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
      <h1 className="text-2xl font-bold text-white mb-6">드라이버</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        {drivers?.map((driver) => (
          <div
            key={driver.id}
            className="bg-[#1a1a1a] rounded-xl p-4 border border-[#2a2a2a] hover:border-[#3a3a3a] transition-colors"
          >
            {driver.headshotUrl && (
              <img
                src={driver.headshotUrl}
                alt={driver.lastName}
                className="w-20 h-20 object-cover rounded-full mx-auto mb-3 bg-[#2a2a2a]"
                onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
              />
            )}

            <div className="text-center">
              <span className="inline-block text-xs font-bold bg-red-600 text-white px-2 py-0.5 rounded mb-2">
                #{driver.driverNumber}
              </span>
              <p className="text-white font-semibold">
                {driver.firstName} {driver.lastName}
              </p>
              <p className="text-xs text-gray-400 mt-0.5">{driver.teamName}</p>
              <p className="text-xs text-gray-500 mt-0.5">{driver.nationality}</p>
            </div>

            {isLoggedIn && (
              <button
                onClick={() => toggleSubscribe(driver.id)}
                className={`w-full mt-3 py-1.5 rounded text-xs font-medium transition-colors ${
                  subscribedIds.has(driver.id)
                    ? 'bg-red-900/40 text-red-400 hover:bg-red-900/60'
                    : 'bg-[#2a2a2a] text-gray-300 hover:bg-[#333]'
                }`}
              >
                {subscribedIds.has(driver.id) ? '구독 중 ✓' : '+ 구독'}
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
