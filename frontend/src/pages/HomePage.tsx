import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getDriverStandings, getNews } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

const CURRENT_SEASON = new Date().getFullYear()

export default function HomePage() {
  const { data: standings, isLoading: standingsLoading } = useQuery({
    queryKey: ['standings', CURRENT_SEASON],
    queryFn: () => getDriverStandings(CURRENT_SEASON),
  })

  const { data: newsPage, isLoading: newsLoading } = useQuery({
    queryKey: ['news', 0],
    queryFn: () => getNews(0, 6),
  })

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-[#1a1f2e] rounded-xl p-5 border border-[#252d3d] flex items-center justify-between">
        <div>
          <h1 className="text-lg font-bold text-white">F1 Pit Wall</h1>
          <p className="text-slate-400 text-sm mt-0.5">
            실시간 레이싱 데이터 플랫폼 · {CURRENT_SEASON} Season
          </p>
        </div>
        <Link
          to="/live"
          className="flex items-center gap-2 bg-[#e10600] hover:bg-red-600 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
        >
          <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
          Live 보기
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Championship Standings */}
        <section className="lg:col-span-2">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              드라이버 챔피언십
            </h2>
            <span className="text-xs text-slate-600">{CURRENT_SEASON}</span>
          </div>

          <div className="bg-[#1a1f2e] rounded-xl border border-[#252d3d] overflow-hidden">
            {standingsLoading ? <LoadingSpinner /> : (
              standings?.slice(0, 10).map((s, i) => (
                <div
                  key={s.driverId}
                  className="flex items-center px-4 py-2.5 border-b border-[#252d3d] last:border-0 hover:bg-[#1e2535] transition-colors"
                >
                  <span className={`w-5 text-xs font-bold font-mono ${i === 0 ? 'text-yellow-400' : i < 3 ? 'text-slate-300' : 'text-slate-600'}`}>
                    {s.rank}
                  </span>
                  <span className="w-10 text-center text-xs font-bold text-white bg-[#252d3d] rounded px-1 py-0.5 mx-3 font-mono">
                    {s.driverCode}
                  </span>
                  <span className="flex-1 text-sm text-slate-300 truncate">{s.driverName}</span>
                  <span className="text-sm font-bold text-white tabular-nums">{s.points}</span>
                  <span className="text-xs text-slate-600 ml-0.5">pts</span>
                </div>
              ))
            )}
          </div>
        </section>

        {/* Latest News */}
        <section className="lg:col-span-3">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">최신 뉴스</h2>
            <Link to="/news" className="text-xs text-[#e10600] hover:text-red-400 transition-colors">
              전체 보기 →
            </Link>
          </div>

          {newsLoading ? <LoadingSpinner /> : (
            <div className="space-y-2">
              {newsPage?.content.map((news) => (
                <a
                  key={news.id}
                  href={news.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex gap-3 bg-[#1a1f2e] rounded-xl p-4 border border-[#252d3d] hover:border-[#2d3748] hover:bg-[#1e2535] transition-colors group"
                >
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-white font-medium leading-snug mb-1.5 line-clamp-2 group-hover:text-slate-100">
                      {news.title}
                    </p>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-slate-600 shrink-0">{news.source}</span>
                      {news.tags && news.tags.split(',').slice(0, 2).map((tag) => (
                        <span key={tag} className="text-xs bg-[#252d3d] text-slate-400 px-1.5 py-0.5 rounded shrink-0">
                          {tag.trim()}
                        </span>
                      ))}
                    </div>
                  </div>
                </a>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
