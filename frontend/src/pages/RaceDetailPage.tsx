import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getRace, getRaceResults, getPitStops } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

const POSITION_MEDALS: Record<number, string> = { 1: '🥇', 2: '🥈', 3: '🥉' }

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
      <Link to="/races" className="text-xs text-slate-500 hover:text-slate-300 transition-colors mb-4 inline-flex items-center gap-1">
        ← 레이스 목록
      </Link>

      {race && (
        <div className="bg-[#1a1f2e] rounded-xl p-5 border border-[#252d3d] mb-5 mt-2">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-xs text-slate-500 mb-1">Round {race.round} · {race.season}</p>
              <h1 className="text-xl font-bold text-white">{race.raceName}</h1>
              <p className="text-sm text-slate-400 mt-1">{race.circuitName} · {race.country}</p>
            </div>
            <div className="text-right">
              <p className="text-sm text-slate-400 tabular-nums">{race.raceDate}</p>
              <span className={`inline-block text-xs px-2 py-0.5 rounded font-medium mt-1 ${
                race.status === 'COMPLETED'
                  ? 'bg-emerald-500/10 text-emerald-400'
                  : 'bg-blue-500/10 text-blue-400'
              }`}>
                {race.status === 'COMPLETED' ? '완료' : '예정'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-1 mb-5 bg-[#1a1f2e] p-1 rounded-lg border border-[#252d3d] w-fit">
        {(['results', 'pitstops'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-1.5 text-sm font-medium rounded-md transition-colors ${
              tab === t
                ? 'bg-[#252d3d] text-white'
                : 'text-slate-500 hover:text-slate-300'
            }`}
          >
            {t === 'results' ? '레이스 결과' : '피트스톱'}
          </button>
        ))}
      </div>

      {tab === 'results' && (
        resultsLoading ? <LoadingSpinner /> : (
          <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
            <div className="grid grid-cols-[2rem_3rem_1fr_1fr_4rem_2rem] px-5 py-2 text-xs text-slate-600 border-b border-[#252d3d] font-medium uppercase tracking-wider">
              <span>P</span>
              <span></span>
              <span>드라이버</span>
              <span>팀</span>
              <span className="text-right">Points</span>
              <span></span>
            </div>
            {results?.map((r) => (
              <div
                key={r.position}
                className="grid grid-cols-[2rem_3rem_1fr_1fr_4rem_2rem] items-center px-5 py-3 border-b border-[#252d3d] last:border-0 hover:bg-[#1e2535] transition-colors"
              >
                <span className="text-sm font-bold text-slate-400">
                  {POSITION_MEDALS[r.position] ?? r.position}
                </span>
                <span className="text-xs font-bold text-white bg-[#252d3d] rounded px-1 py-0.5 text-center font-mono">
                  {r.driverCode}
                </span>
                <span className="text-sm text-slate-300 pl-3">{r.driverName}</span>
                <span className="text-xs text-slate-500">{r.teamName}</span>
                <span className="text-sm font-bold text-white text-right tabular-nums">{r.points}</span>
                <span className="text-center">{r.fastestLap && <span className="text-purple-400 text-xs">⚡</span>}</span>
              </div>
            ))}
          </div>
        )
      )}

      {tab === 'pitstops' && (
        pitLoading ? <LoadingSpinner /> : (
          <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
            <div className="grid grid-cols-4 px-5 py-2 text-xs text-slate-600 border-b border-[#252d3d] font-medium uppercase tracking-wider">
              <span>드라이버</span>
              <span>랩</span>
              <span>횟수</span>
              <span>소요시간</span>
            </div>
            {pitStops?.map((p, i) => (
              <div
                key={i}
                className="grid grid-cols-4 px-5 py-3 border-b border-[#252d3d] last:border-0 hover:bg-[#1e2535] transition-colors"
              >
                <span className="text-sm font-bold text-white font-mono">{p.driverCode}</span>
                <span className="text-sm text-slate-400">Lap {p.lapNumber}</span>
                <span className="text-sm text-slate-400">#{p.stopNumber}</span>
                <span className="text-sm text-slate-300 font-medium tabular-nums">{p.duration.toFixed(1)}s</span>
              </div>
            ))}
          </div>
        )
      )}
    </div>
  )
}
