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
    queryFn: () => getNews(0, 5),
  })

  return (
    <div className="space-y-10">
      {/* Hero */}
      <div className="text-center py-10 border-b border-[#2a2a2a]">
        <h1 className="text-4xl font-bold text-white mb-2">
          F1 <span className="text-red-500">PIT WALL</span>
        </h1>
        <p className="text-gray-400">실시간 F1 레이싱 데이터 플랫폼</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Championship Standings */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-white">
              {CURRENT_SEASON} 드라이버 챔피언십
            </h2>
          </div>

          {standingsLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="bg-[#1a1a1a] rounded-xl overflow-hidden">
              {standings?.slice(0, 10).map((s) => (
                <div
                  key={s.driverId}
                  className="flex items-center px-4 py-3 border-b border-[#2a2a2a] last:border-0 hover:bg-[#222] transition-colors"
                >
                  <span className="w-6 text-gray-500 text-sm font-mono">{s.rank}</span>
                  <span className="w-12 text-center font-bold text-sm bg-[#2a2a2a] rounded px-1 py-0.5 text-white mx-3">
                    {s.driverCode}
                  </span>
                  <span className="flex-1 text-sm text-gray-300">{s.driverName}</span>
                  <span className="text-xs text-gray-500 mr-4">{s.teamName}</span>
                  <span className="font-bold text-white text-sm">{s.points} pts</span>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Latest News */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-white">최신 뉴스</h2>
            <Link to="/news" className="text-sm text-red-500 hover:text-red-400">
              전체 보기 →
            </Link>
          </div>

          {newsLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="space-y-3">
              {newsPage?.content.map((news) => (
                <a
                  key={news.id}
                  href={news.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="block bg-[#1a1a1a] rounded-xl p-4 hover:bg-[#222] transition-colors"
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <p className="text-sm text-white font-medium leading-snug line-clamp-2">
                      {news.title}
                    </p>
                    <span className="text-xs text-gray-500 whitespace-nowrap shrink-0">
                      {news.source}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400 line-clamp-2">{news.summary}</p>
                  {news.tags && (
                    <div className="flex flex-wrap gap-1 mt-2">
                      {news.tags.split(',').slice(0, 3).map((tag) => (
                        <span
                          key={tag}
                          className="text-xs bg-red-900/30 text-red-400 px-1.5 py-0.5 rounded"
                        >
                          {tag.trim()}
                        </span>
                      ))}
                    </div>
                  )}
                </a>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
