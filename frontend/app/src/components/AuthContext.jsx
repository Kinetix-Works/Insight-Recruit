import { createContext, useContext, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { logout as logoutRequest } from '../services/authApi'
import {
  clearAccessToken,
  clearStoredUser,
  getStoredUser,
  setAccessToken,
  setStoredUser,
} from '../services/tokenStorage'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const navigate = useNavigate()
  const [user, setUser] = useState(() => getStoredUser())

  const login = (authResponse) => {
    setAccessToken(authResponse.accessToken)
    setStoredUser(authResponse.user)
    setUser(authResponse.user)
  }

  const logout = async () => {
    try {
      await logoutRequest()
    } catch {
      // Force local logout even if backend token revocation fails.
    } finally {
      clearAccessToken()
      clearStoredUser()
      setUser(null)
      navigate('/login', { replace: true })
    }
  }

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      login,
      logout,
    }),
    [user]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
