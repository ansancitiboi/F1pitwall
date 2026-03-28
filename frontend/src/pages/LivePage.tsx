import { useEffect, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getLivePositions } from '../api'
import { useAuthStore } from '../store/auth'
import type { RaceEventPayload, LivePosition } from '../types'
import LoadingSpinner from '../components/ui/LoadingSpinner'

const EVENT_LABELS: Record<RaceEventPayload['type'], string> = {
  POSITION_CHANGE: '순위 변동',
  PIT_STOP: '피트스톱',
  SAFETY_CAR: '🟡 세이프티카',
  VIRTUAL_SAFETY_CAR: '🟡 VSC',
}

const EVENT_COLORS: Record<RaceEventPayload['type'], string> = {
  POSITION_CHANGE: 'border-blue-500 bg-blue-900/20',
  PIT_STOP: 'border-yellow-500 bg-yellow-900/20',
  SAFETY_CAR: 'border-yellow-400 bg-yellow-900/30',
  VIRTUAL_SAFETY_CAR: 'border-yellow-400 bg-yellow-900/30',
}

export default function LivePage() {
  const { isLoggedIn } = useAuthStore()
  const [events, setEvents] = useState<RaceEventPayload[]>([])
  const [sseStatus, setSseStatus] = useState<'disconnected' | 'connected' | 'error'>('disconnected')
  const eventSourceRef = useRef<EventSource | null>(null)

  const { data: positions, isLoading, refetch } = useQuery({
    queryKey: ['livePositions'],
    queryFn: getLivePositions,
    refetchInterval: 10000,
  })

  useEffect(() => {
    if (!isLoggedIn) return

    const token = localStorage.getItem('accessToken')
    const es = new EventSource(`/api/sse/subscribe?token=${token}`)
    eventSourceRef.current = es

    es.onopen = () => setSseStatus('connected')
    es.onerror = () => setSseStatus('error')

    es.onmessage = (e) => {
      try {
        const payload: RaceEventPayload = JSON.parse(e.data)
        setEvents((prev) => [payload, ...prev].slice(0, 30))
        refetch()
      } catch { /* ignore parse errors */ }
    }

    return () => {
      es.close()
      setSseStatus('disconnected')
    }
  }, [isLoggedIn, refetch])

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-white flex items-center gap-3">
          Live
          <span
            className={`w-2.5 h-2.5 rounded-full ${
              sseStatus === 'connected'
                ? 'bg-green-500 animate-pulse'
                : sseStatus === 'error'
                ? 'bg-red-500'
                : 'bg-gray-500'
            }`}
          />
          <span className="text-sm font-normal text-gray-400">
            {sseStatus === 'connected' ? '실시간 연결됨' : sseStatus === 'error' ? '연결 오류' : '연결 안됨'}
          </span>
        </h1>

        {!isLoggedIn && (
          <p className="text-sm text-gray-400">
            실시간 알림을 받으려면{' '}
            <a href="/login" className="text-red-400 hover:underline">로그인</a>
            하세요
          </p>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Live Positions */}
        <section>
          <h2 className="text-lg font-semibold text-white mb-3">현재 순위</h2>
          {isLoading ? (
            <LoadingSpinner />
          ) : positions && positions.length > 0 ? (
            <div className="bg-[#1a1a1a] rounded-xl overflow-hidden">
              {(positions as LivePosition[])
                .sort((a, b) => a.position - b.position)
                .map((p) => (
                  <div
                    key={p.driverNumber}
                    className="flex items-center px-5 py-3 border-b border-[#2a2a2a] last:border-0"
                  >
                    <span className="w-6 text-gray-500 text-sm font-mono">{p.position}</span>
                    <span className="w-12 text-center font-bold text-sm bg-[#2a2a2a] rounded px-1 text-white mx-3">
                      {p.driverCode}
                    </span>
                    <span className="flex-1 text-sm text-gray-300">{p.teamName}</span>
                    <span className="text-xs text-gray-500">#{p.driverNumber}</span>
                  </div>
                ))}
            </div>
          ) : (
            <div className="bg-[#1a1a1a] rounded-xl p-8 text-center text-gray-500 text-sm">
              현재 진행 중인 레이스가 없습니다
            </div>
          )}
        </section>

        {/* Event Log */}
        <section>
          <h2 className="text-lg font-semibold text-white mb-3">이벤트 로그</h2>
          {events.length === 0 ? (
            <div className="bg-[#1a1a1a] rounded-xl p-8 text-center text-gray-500 text-sm">
              {isLoggedIn ? '이벤트 대기 중...' : '로그인 후 이벤트를 수신할 수 있습니다'}
            </div>
          ) : (
            <div className="space-y-2 max-h-[500px] overflow-y-auto">
              {events.map((event, i) => (
                <div
                  key={i}
                  className={`rounded-xl p-3 border-l-4 ${EVENT_COLORS[event.type]}`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-semibold text-gray-300">
                      {EVENT_LABELS[event.type]}
                      {event.driverCode && (
                        <span className="ml-2 text-white">{event.driverCode}</span>
                      )}
                    </span>
                    <span className="text-xs text-gray-500">
                      {new Date(event.timestamp).toLocaleTimeString()}
                    </span>
                  </div>
                  <p className="text-sm text-gray-300">{event.message}</p>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
