import { useEffect, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getLivePositions } from '../api'
import { useAuthStore } from '../store/auth'
import type { RaceEventPayload, LivePosition } from '../types'
import LoadingSpinner from '../components/ui/LoadingSpinner'

const EVENT_LABELS: Record<RaceEventPayload['type'], string> = {
  POSITION_CHANGE: '순위 변동',
  PIT_STOP: '피트스톱',
  SAFETY_CAR: '세이프티카',
  VIRTUAL_SAFETY_CAR: 'VSC',
}

const EVENT_STYLES: Record<RaceEventPayload['type'], { dot: string; badge: string }> = {
  POSITION_CHANGE: { dot: 'bg-blue-400', badge: 'bg-blue-500/10 text-blue-400' },
  PIT_STOP:        { dot: 'bg-yellow-400', badge: 'bg-yellow-500/10 text-yellow-400' },
  SAFETY_CAR:      { dot: 'bg-orange-400', badge: 'bg-orange-500/10 text-orange-400' },
  VIRTUAL_SAFETY_CAR: { dot: 'bg-orange-400', badge: 'bg-orange-500/10 text-orange-400' },
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
      } catch { /* ignore */ }
    }
    return () => { es.close(); setSseStatus('disconnected') }
  }, [isLoggedIn, refetch])

  const sseIndicator = {
    connected: { color: 'bg-emerald-400', text: '실시간 연결됨' },
    error:      { color: 'bg-red-400', text: '연결 오류' },
    disconnected: { color: 'bg-slate-500', text: '연결 안됨' },
  }[sseStatus]

  return (
    <div>
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-3">
          <h1 className="text-lg font-bold text-white">Live</h1>
          <div className="flex items-center gap-1.5">
            <span className={`w-2 h-2 rounded-full ${sseIndicator.color} ${sseStatus === 'connected' ? 'animate-pulse' : ''}`} />
            <span className="text-xs text-slate-500">{sseIndicator.text}</span>
          </div>
        </div>
        {!isLoggedIn && (
          <p className="text-xs text-slate-500">
            실시간 알림은 <a href="/login" className="text-[#e10600] hover:underline">로그인</a> 후 이용 가능
          </p>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Live Positions */}
        <section>
          <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">현재 순위</h2>

          {isLoading ? <LoadingSpinner /> : positions && (positions as LivePosition[]).length > 0 ? (
            <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
              {(positions as LivePosition[])
                .sort((a, b) => a.position - b.position)
                .map((p) => (
                  <div
                    key={p.driverNumber}
                    className="flex items-center px-4 py-3 border-b border-[#252d3d] last:border-0 hover:bg-[#1e2535] transition-colors"
                  >
                    <span className="w-6 text-xs font-bold text-slate-500 font-mono">{p.position}</span>
                    <span className="w-10 text-center text-xs font-bold text-white bg-[#252d3d] rounded px-1 py-0.5 mx-3 font-mono">
                      {p.driverCode}
                    </span>
                    <span className="flex-1 text-sm text-slate-300">{p.teamName}</span>
                    <span className="text-xs text-slate-600 font-mono">#{p.driverNumber}</span>
                  </div>
                ))}
            </div>
          ) : (
            <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-10 text-center">
              <p className="text-slate-500 text-sm">현재 진행 중인 레이스가 없습니다</p>
            </div>
          )}
        </section>

        {/* Event Log */}
        <section>
          <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">이벤트 로그</h2>

          {events.length === 0 ? (
            <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-10 text-center">
              <p className="text-slate-500 text-sm">
                {isLoggedIn ? '이벤트 대기 중...' : '로그인 후 이벤트를 수신할 수 있습니다'}
              </p>
            </div>
          ) : (
            <div className="space-y-2 max-h-[500px] overflow-y-auto">
              {events.map((event, i) => {
                const style = EVENT_STYLES[event.type]
                return (
                  <div key={i} className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] p-4 flex gap-3">
                    <div className="pt-1">
                      <span className={`block w-2 h-2 rounded-full ${style.dot}`} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <span className={`text-xs font-medium px-1.5 py-0.5 rounded ${style.badge}`}>
                          {EVENT_LABELS[event.type]}
                        </span>
                        {event.driverCode && (
                          <span className="text-xs font-bold text-white bg-[#252d3d] px-1.5 py-0.5 rounded font-mono">
                            {event.driverCode}
                          </span>
                        )}
                        <span className="text-xs text-slate-600 ml-auto tabular-nums">
                          {new Date(event.timestamp).toLocaleTimeString('ko-KR')}
                        </span>
                      </div>
                      <p className="text-sm text-slate-300">{event.message}</p>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
