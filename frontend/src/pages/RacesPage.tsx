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
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-white">레이스</h1>
        <select
          value={season}
          onChange={(e) => setSeason(Number(e.target.value))}
          className="bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded px-3 py-1.5"
        >
          {[2025, 2024, 2023].map((y) => (
            <option key={y} value={y}>{y}</option>
          ))}
        </select>
      </div>

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <div className="space-y-2">
          {races?.map((race) => (
            <Link
              key={race.id}
              to={`/races/${race.id}`}
              className="flex items-center bg-[#1a1a1a] rounded-xl px-5 py-4 border border-[#2a2a2a] hover:border-[#3a3a3a] hover:bg-[#222] transition-colors"
            >
              <span className="w-8 text-gray-500 text-sm font-mono">R{race.round}</span>
              <div className="flex-1 ml-4">
                <p className="text-white font-medium">{race.raceName}</p>
                <p className="text-xs text-gray-400 mt-0.5">{race.circuitName} · {race.country}</p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-300">{race.raceDate}</p>
                <span
                  className={`text-xs px-2 py-0.5 rounded mt-1 inline-block ${
                    race.status === 'COMPLETED'
                      ? 'bg-green-900/40 text-green-400'
                      : 'bg-yellow-900/40 text-yellow-400'
                  }`}
                >
                  {race.status === 'COMPLETED' ? '완료' : '예정'}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
