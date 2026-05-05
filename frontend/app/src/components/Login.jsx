import { useState } from 'react'
import { Link } from 'react-router-dom'
import { AuthLayout } from './AuthLayout'
import { useAuth } from './AuthContext'
import { login as loginRequest, oauthLogin } from '../services/authApi'

const OAUTH_PROVIDERS = ['GOOGLE', 'MICROSOFT', 'ZOHO']

export function Login() {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const onSubmit = async (event) => {
    event.preventDefault()
    setError('')
    try {
      const authResponse = await loginRequest({ email, password })
      login(authResponse)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Invalid email or password')
    }
  }

  const onOauth = async (provider) => {
    setError('')
    try {
      const authResponse = await oauthLogin({
        provider,
        email,
        firstName: 'OAuth',
        lastName: 'User',
      })
      login(authResponse)
    } catch (err) {
      setError(err?.response?.data?.message ?? `Failed login with ${provider}`)
    }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in with password or OAuth.">
      {error ? <p className="rounded-md bg-red-100 px-3 py-2 text-sm text-red-700">{error}</p> : null}
      <form className="space-y-3" onSubmit={onSubmit}>
        <input className="w-full rounded border px-3 py-2" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="password" className="w-full rounded border px-3 py-2" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button className="w-full rounded bg-violet-600 px-3 py-2 text-white">Login</button>
      </form>
      <div className="space-y-2">
        {OAUTH_PROVIDERS.map((provider) => (
          <button key={provider} type="button" onClick={() => onOauth(provider)} className="w-full rounded border px-3 py-2 text-sm">
            Sign in with {provider}
          </button>
        ))}
      </div>
      <p className="text-sm text-zinc-600 dark:text-zinc-300">
        New here? <Link className="text-violet-600" to="/signup">Create account</Link>
      </p>
    </AuthLayout>
  )
}
