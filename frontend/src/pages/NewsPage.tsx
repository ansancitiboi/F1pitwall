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
      <h1 className="text-2xl font-bold text-white mb-6">뉴스</h1>

      {/* Search */}
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="드라이버, 팀, 키워드 검색..."
          className="flex-1 bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-4 py-2.5 outline-none focus:border-red-500 transition-colors"
        />
        <button
          type="submit"
          className="bg-red-600 hover:bg-red-500 text-white text-sm px-4 py-2.5 rounded-lg transition-colors"
        >
          검색
        </button>
        {submitted && (
          <button
            type="button"
            onClick={handleClear}
            className="bg-[#2a2a2a] hover:bg-[#333] text-gray-300 text-sm px-4 py-2.5 rounded-lg transition-colors"
          >
            초기화
          </button>
        )}
      </form>

      {isLoading ? (
        <LoadingSpinner />
      ) : (
        <>
          <div className="space-y-3">
            {items?.map((news) => (
              <a
                key={news.id}
                href={news.url}
                target="_blank"
                rel="noopener noreferrer"
                className="block bg-[#1a1a1a] rounded-xl p-5 border border-[#2a2a2a] hover:border-[#3a3a3a] hover:bg-[#222] transition-colors"
              >
                <div className="flex items-start justify-between gap-3 mb-2">
                  <p className="text-white font-medium leading-snug">{news.title}</p>
                  <span className="text-xs text-gray-500 whitespace-nowrap shrink-0 mt-0.5">
                    {news.source}
                  </span>
                </div>

                <p className="text-sm text-gray-400 leading-relaxed mb-3">{news.summary}</p>

                <div className="flex items-center justify-between">
                  <div className="flex flex-wrap gap-1">
                    {news.tags && news.tags.split(',').map((tag) => (
                      <span
                        key={tag}
                        className="text-xs bg-red-900/30 text-red-400 px-1.5 py-0.5 rounded"
                      >
                        {tag.trim()}
                      </span>
                    ))}
                  </div>
                  <span className="text-xs text-gray-600">
                    {new Date(news.publishedAt).toLocaleDateString('ko-KR')}
                  </span>
                </div>
              </a>
            ))}
          </div>

          {/* Pagination (list mode only) */}
          {!submitted && newsPage && newsPage.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 text-sm bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg text-gray-300 disabled:opacity-40 hover:bg-[#222] transition-colors"
              >
                이전
              </button>
              <span className="px-4 py-2 text-sm text-gray-400">
                {page + 1} / {newsPage.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => Math.min(newsPage.totalPages - 1, p + 1))}
                disabled={page === newsPage.totalPages - 1}
                className="px-4 py-2 text-sm bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg text-gray-300 disabled:opacity-40 hover:bg-[#222] transition-colors"
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
