import client from './client'
import type {
  Driver, Race, RaceResult, PitStop, LivePosition,
  DriverStanding, News, NewsSearch, UserInfo, TokenResponse, PageResponse
} from '../types'

// Auth
export const signup = (email: string, password: string, nickname: string) =>
  client.post('/auth/signup', { email, password, nickname })

export const login = async (email: string, password: string): Promise<TokenResponse> => {
  const { data } = await client.post('/auth/login', { email, password })
  return data.data
}

export const logout = (refreshToken: string) =>
  client.post('/auth/logout', { refreshToken })

// Drivers
export const getDrivers = async (): Promise<Driver[]> => {
  const { data } = await client.get('/drivers')
  return data.data
}

export const getDriver = async (id: number): Promise<Driver> => {
  const { data } = await client.get(`/drivers/${id}`)
  return data.data
}

// Races
export const getRaces = async (season: number): Promise<Race[]> => {
  const { data } = await client.get('/races', { params: { season } })
  return data.data
}

export const getRace = async (id: number): Promise<Race> => {
  const { data } = await client.get(`/races/${id}`)
  return data.data
}

export const getRaceResults = async (id: number): Promise<RaceResult[]> => {
  const { data } = await client.get(`/races/${id}/results`)
  return data.data
}

export const getPitStops = async (id: number): Promise<PitStop[]> => {
  const { data } = await client.get(`/races/${id}/pitstops`)
  return data.data
}

export const getLivePositions = async (): Promise<LivePosition[]> => {
  const { data } = await client.get('/races/live')
  return data.data
}

// Standings
export const getDriverStandings = async (season: number): Promise<DriverStanding[]> => {
  const { data } = await client.get('/standings/drivers', { params: { season } })
  return data.data
}

// News
export const getNews = async (page = 0, size = 10): Promise<PageResponse<News>> => {
  const { data } = await client.get('/news', { params: { page, size } })
  return data.data
}

export const getNewsById = async (id: number): Promise<News> => {
  const { data } = await client.get(`/news/${id}`)
  return data.data
}

export const getNewsByDriver = async (code: string): Promise<News[]> => {
  const { data } = await client.get(`/news/drivers/${code}`)
  return data.data
}

export const searchNews = async (q: string): Promise<NewsSearch[]> => {
  const { data } = await client.get('/news/search', { params: { q } })
  return data.data
}

// User
export const getMe = async (): Promise<UserInfo> => {
  const { data } = await client.get('/users/me')
  return data.data
}

export const getMyDrivers = async (): Promise<Driver[]> => {
  const { data } = await client.get('/users/me/drivers')
  return data.data
}

export const subscribe = (driverId: number) =>
  client.post(`/users/me/drivers/${driverId}`)

export const unsubscribe = (driverId: number) =>
  client.delete(`/users/me/drivers/${driverId}`)
