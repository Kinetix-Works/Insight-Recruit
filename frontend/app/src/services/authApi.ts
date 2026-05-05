import api from './api'

export type AuthProvider = 'GOOGLE' | 'MICROSOFT' | 'ZOHO'

export async function signupInitiate(payload) {
  const { data } = await api.post('/auth/signup/initiate', payload)
  return data
}

export async function signupVerify(payload) {
  const { data } = await api.post('/auth/signup/verify', payload)
  return data
}

export async function signupPassword(payload) {
  const { data } = await api.post('/auth/signup/password', payload)
  return data
}

export async function login(payload) {
  const { data } = await api.post('/auth/login', payload)
  return data
}

export async function oauthLogin(payload) {
  const { data } = await api.post('/auth/oauth/login', payload)
  return data
}

export async function logout() {
  await api.post('/auth/logout')
}
