import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getRaces } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

const CURRENT_SEASON = new Date().getFullYear()

export default function RacesPage() {
  const [season, setSeason] = useState(CURRENT_SEASON)

  const { data: races, isLoading } = useQuery({
    queryKey: ['races', season],
    queryFn: () => getRaces(season),
  })

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-lg font-bold text-white">레이스</h1>
        <select
          value={season}
          onChange={(e) => setSeason(Number(e.target.value))}
          className="bg-[#1a1f2e] border border-[#252d3d] text-slate-300 text-sm rounded-lg px-3 py-1.5 outline-none focus:border-[#e10600] transition-colors"
        >
          {[2025, 2024, 2023].map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
      </div>

      {isLoading ? <LoadingSpinner /> : (
        <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
          {races?.map((race) => (
            <Link
              key={race.id}
              to={`/races/${race.id}`}
              className="flex items-center px-5 py-3.5 border-b border-[#252d3d] last:border-0 hover:bg-[#1e2535] transition-colors group"
            >
              <span className="w-8 text-xs font-bold text-slate-600 font-mono">R{race.round}</span>

              <div className="flex-1 ml-3">
                <p className="text-sm font-medium text-white group-hover:text-slate-100">{race.raceName}</p>
                <p className="text-xs text-slate-500 mt-0.5">{race.circuitName} · {race.country}</p>
              </div>

              <div className="flex items-center gap-3">
                <span className="text-xs text-slate-500 tabular-nums">{race.raceDate}</span>
                <span className={`text-xs px-2 py-0.5 rounded font-medium ${
                  race.status === 'COMPLETED'
                    ? 'bg-emerald-500/10 text-emerald-400'
                    : 'bg-blue-500/10 text-blue-400'
                }`}>
                  {race.status === 'COMPLETED' ? '완료' : '예정'}
                </span>
                <span className="text-slate-600 group-hover:text-slate-400 transition-colors">›</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
