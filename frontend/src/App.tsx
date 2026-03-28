import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import Layout from './components/layout/Layout'
import HomePage from './pages/HomePage'
import DriversPage from './pages/DriversPage'
import RacesPage from './pages/RacesPage'
import RaceDetailPage from './pages/RaceDetailPage'
import LivePage from './pages/LivePage'
import NewsPage from './pages/NewsPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import MyPage from './pages/MyPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/drivers" element={<DriversPage />} />
            <Route path="/races" element={<RacesPage />} />
            <Route path="/races/:id" element={<RaceDetailPage />} />
            <Route path="/live" element={<LivePage />} />
            <Route path="/news" element={<NewsPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/me" element={<MyPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
