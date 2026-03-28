import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getRace, getRaceResults, getPitStops } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

export default function RaceDetailPage() {
  const { id } = useParams<{ id: string }>()
  const raceId = Number(id)
  const [tab, setTab] = useState<'results' | 'pitstops'>('results')

  const { data: race } = useQuery({
    queryKey: ['race', raceId],
    queryFn: () => getRace(raceId),
  })

  const { data: results, isLoading: resultsLoading } = useQuery({
    queryKey: ['raceResults', raceId],
    queryFn: () => getRaceResults(raceId),
    enabled: tab === 'results',
  })

  const { data: pitStops, isLoading: pitLoading } = useQuery({
    queryKey: ['pitStops', raceId],
    queryFn: () => getPitStops(raceId),
    enabled: tab === 'pitstops',
  })

  return (
    <div>
      {race && (
        <div className="mb-6">
          <p className="text-gray-400 text-sm mb-1">Round {race.round} · {race.season}</p>
          <h1 className="text-2xl font-bold text-white">{race.raceName}</h1>
          <p className="text-gray-400 mt-1">{race.circuitName} · {race.country} · {race.raceDate}</p>
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-2 mb-6 border-b border-[#2a2a2a]">
        {(['results', 'pitstops'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors -mb-px ${
              tab === t
                ? 'border-red-500 text-white'
                : 'border-transparent text-gray-400 hover:text-white'
            }`}
          >
            {t === 'results' ? '레이스 결과' : '피트스톱'}
          </button>
        ))}
      </div>

      {tab === 'results' && (
        resultsLoading ? <LoadingSpinner /> : (
          <div className="bg-[#1a1a1a] rounded-xl overflow-hidden">
            {results?.map((r) => (
              <div
                key={r.position}
                className="flex items-center px-5 py-3 border-b border-[#2a2a2a] last:border-0"
              >
                <span className="w-8 text-gray-500 text-sm font-mono">{r.position}</span>
                <span className="w-12 text-center font-bold text-sm bg-[#2a2a2a] rounded px-1 text-white mx-3">
                  {r.driverCode}
                </span>
                <span className="flex-1 text-sm text-gray-300">{r.driverName}</span>
                <span className="text-xs text-gray-500 mr-6">{r.teamName}</span>
                <span className="text-sm font-bold text-white w-14 text-right">{r.points} pts</span>
                {r.fastestLap && (
                  <span className="ml-3 text-xs text-purple-400">⚡ FL</span>
                )}
              </div>
            ))}
          </div>
        )
      )}

      {tab === 'pitstops' && (
        pitLoading ? <LoadingSpinner /> : (
          <div className="bg-[#1a1a1a] rounded-xl overflow-hidden">
            <div className="grid grid-cols-4 px-5 py-2 text-xs text-gray-500 border-b border-[#2a2a2a]">
              <span>드라이버</span>
              <span>랩</span>
              <span>횟수</span>
              <span>소요시간</span>
            </div>
            {pitStops?.map((p, i) => (
              <div
                key={i}
                className="grid grid-cols-4 px-5 py-3 border-b border-[#2a2a2a] last:border-0 text-sm"
              >
                <span className="text-white font-medium">{p.driverCode}</span>
                <span className="text-gray-300">Lap {p.lapNumber}</span>
                <span className="text-gray-300">#{p.stopNumber}</span>
                <span className="text-gray-300">{p.duration.toFixed(1)}s</span>
              </div>
            ))}
          </div>
        )
      )}
    </div>
  )
}
