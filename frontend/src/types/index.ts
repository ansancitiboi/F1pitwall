export interface Driver {
  id: number
  driverNumber: number
  code: string
  firstName: string
  lastName: string
  teamName: string
  nationality: string
  headshotUrl: string
}

export interface Race {
  id: number
  season: number
  round: number
  raceName: string
  circuitName: string
  country: string
  raceDate: string
  sessionKey: number | null
  status: 'SCHEDULED' | 'COMPLETED'
}

export interface RaceResult {
  position: number
  driverCode: string
  driverName: string
  teamName: string
  points: number
  fastestLap: boolean
}

export interface PitStop {
  driverCode: string
  driverName: string
  lapNumber: number
  duration: number
  stopNumber: number
}

export interface LivePosition {
  driverNumber: number
  driverCode: string
  position: number
  teamName: string
}

export interface DriverStanding {
  rank: number
  driverId: number
  driverCode: string
  driverName: string
  teamName: string
  points: number
  wins: number
}

export interface News {
  id: number
  title: string
  url: string
  source: string
  summary: string
  tags: string
  publishedAt: string
}

export interface NewsSearch {
  id: number
  title: string
  url: string
  source: string
  summary: string
  tags: string
  publishedAt: string
}

export interface UserInfo {
  id: number
  email: string
  nickname: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
}

export interface RaceEventPayload {
  type: 'POSITION_CHANGE' | 'PIT_STOP' | 'SAFETY_CAR' | 'VIRTUAL_SAFETY_CAR'
  driverNumber: number | null
  driverCode: string | null
  message: string
  timestamp: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
}
