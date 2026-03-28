import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getNews, searchNews } from '../api'
import LoadingSpinner from '../components/ui/LoadingSpinner'

export default function NewsPage() {
  const [query, setQuery] = useState('')
  const [submitted, setSubmitted] = useState('')
  const [page, setPage] = useState(0)

  const { data: newsPage, isLoading: newsLoading } = useQuery({
    queryKey: ['news', page],
    queryFn: () => getNews(page, 10),
    enabled: !submitted,
  })

  const { data: searchResults, isLoading: searchLoading } = useQuery({
    queryKey: ['newsSearch', submitted],
    queryFn: () => searchNews(submitted),
    enabled: !!submitted,
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitted(query.trim())
    setPage(0)
  }

  const handleClear = () => {
    setQuery('')
    setSubmitted('')
  }

  const isLoading = newsLoading || searchLoading
  const items = submitted ? searchResults : newsPage?.content

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-lg font-bold text-white">뉴스</h1>
        <span className="text-xs text-slate-500">Claude AI 요약</span>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch} className="flex gap-2 mb-5">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="드라이버, 팀, 키워드 검색..."
          className="flex-1 bg-[#1a1f2e] border border-[#252d3d] text-white text-sm rounded-lg px-4 py-2.5 outline-none focus:border-[#e10600] transition-colors placeholder:text-slate-600"
        />
        <button
          type="submit"
          className="bg-[#e10600] hover:bg-red-600 text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
        >
          검색
        </button>
        {submitted && (
          <button
            type="button"
            onClick={handleClear}
            className="bg-[#1a1f2e] border border-[#252d3d] hover:bg-[#1e2535] text-slate-400 text-sm px-4 py-2.5 rounded-lg transition-colors"
          >
            초기화
          </button>
        )}
      </form>

      {isLoading ? <LoadingSpinner /> : (
        <>
          <div className="space-y-2">
            {items?.map((news) => (
              <a
                key={news.id}
                href={news.url}
                target="_blank"
                rel="noopener noreferrer"
                className="flex gap-4 bg-[#1a1f2e] rounded-xl p-4 border border-[#252d3d] hover:border-[#2d3748] hover:bg-[#1e2535] transition-colors group"
              >
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-3 mb-2">
                    <p className="text-sm text-white font-medium leading-snug group-hover:text-slate-100">
                      {news.title}
                    </p>
                    <span className="text-xs text-slate-600 whitespace-nowrap shrink-0">{news.source}</span>
                  </div>

                  <p className="text-xs text-slate-400 leading-relaxed mb-2.5 line-clamp-2">{news.summary}</p>

                  <div className="flex items-center justify-between">
                    <div className="flex flex-wrap gap-1">
                      {news.tags && news.tags.split(',').slice(0, 4).map((tag) => (
                        <span key={tag} className="text-xs bg-[#252d3d] text-slate-400 px-1.5 py-0.5 rounded">
                          {tag.trim()}
                        </span>
                      ))}
                    </div>
                    <span className="text-xs text-slate-600 tabular-nums">
                      {new Date(news.publishedAt).toLocaleDateString('ko-KR')}
                    </span>
                  </div>
                </div>
              </a>
            ))}
          </div>

          {/* Pagination */}
          {!submitted && newsPage && newsPage.totalPages > 1 && (
            <div className="flex justify-center items-center gap-3 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 text-sm bg-[#1a1f2e] border border-[#252d3d] rounded-lg text-slate-400 disabled:opacity-30 hover:bg-[#1e2535] transition-colors"
              >
                이전
              </button>
              <span className="text-sm text-slate-500 tabular-nums">
                {page + 1} / {newsPage.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(newsPage.totalPages - 1, p + 1))}
                disabled={page === newsPage.totalPages - 1}
                className="px-4 py-2 text-sm bg-[#1a1f2e] border border-[#252d3d] rounded-lg text-slate-400 disabled:opacity-30 hover:bg-[#1e2535] transition-colors"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
