import { useState } from 'react'
import { Link } from 'react-router-dom'
import { AuthLayout } from './AuthLayout'
import { useAuth } from './AuthContext'
import { oauthLogin, signupInitiate, signupPassword, signupVerify } from '../services/authApi'

const OAUTH_PROVIDERS = ['GOOGLE', 'MICROSOFT', 'ZOHO']

export function Signup() {
  const { login } = useAuth()
  const [step, setStep] = useState(1)
  const [form, setForm] = useState({ email: '', firstName: '', lastName: '', role: 'RECRUITER', otp: '', password: '' })
  const [error, setError] = useState('')

  const onInitiate = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await signupInitiate({
        email: form.email,
        firstName: form.firstName,
        lastName: form.lastName,
        role: form.role,
      })
      setStep(2)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Unable to send OTP')
    }
  }

  const onVerifyOtp = async (event) => {
    event.preventDefault()
    setError('')
    try {
      await signupVerify({ email: form.email, otp: form.otp })
      setStep(3)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Invalid or expired OTP')
    }
  }

  const onSetPassword = async (event) => {
    event.preventDefault()
    setError('')
    try {
      const authResponse = await signupPassword({ email: form.email, password: form.password })
      login(authResponse)
    } catch (err) {
      setError(err?.response?.data?.message ?? 'Unable to complete signup')
    }
  }

  const onOauth = async (provider) => {
    setError('')
    try {
      const authResponse = await oauthLogin({
        provider,
        email: form.email,
        firstName: form.firstName || 'OAuth',
        lastName: form.lastName || 'User',
      })
      login(authResponse)
    } catch (err) {
      setError(err?.response?.data?.message ?? `Failed to sign up with ${provider}`)
    }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Verify email with OTP, then set a secure password.">
      {error ? <p className="rounded-md bg-red-100 px-3 py-2 text-sm text-red-700">{error}</p> : null}
      {step === 1 ? (
        <form className="space-y-3" onSubmit={onInitiate}>
          <input className="w-full rounded border px-3 py-2" placeholder="Email" value={form.email} onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))} required />
          <input className="w-full rounded border px-3 py-2" placeholder="First name" value={form.firstName} onChange={(e) => setForm((prev) => ({ ...prev, firstName: e.target.value }))} required />
          <input className="w-full rounded border px-3 py-2" placeholder="Last name" value={form.lastName} onChange={(e) => setForm((prev) => ({ ...prev, lastName: e.target.value }))} required />
          <select className="w-full rounded border px-3 py-2" value={form.role} onChange={(e) => setForm((prev) => ({ ...prev, role: e.target.value }))}>
            <option value="HR">HR</option>
            <option value="RECRUITER">Recruiter</option>
            <option value="DEVELOPER">Developer</option>
            <option value="STUDENT">Student</option>
            <option value="OTHER">Other</option>
          </select>
          <button className="w-full rounded bg-violet-600 px-3 py-2 text-white">Send OTP</button>
        </form>
      ) : null}
      {step === 2 ? (
        <form className="space-y-3" onSubmit={onVerifyOtp}>
          <input className="w-full rounded border px-3 py-2" placeholder="Enter 6-digit OTP" value={form.otp} onChange={(e) => setForm((prev) => ({ ...prev, otp: e.target.value }))} required />
          <button className="w-full rounded bg-violet-600 px-3 py-2 text-white">Verify OTP</button>
          <button type="button" className="w-full rounded border px-3 py-2" onClick={onInitiate}>Code expired? Resend</button>
        </form>
      ) : null}
      {step === 3 ? (
        <form className="space-y-3" onSubmit={onSetPassword}>
          <input type="password" className="w-full rounded border px-3 py-2" placeholder="Set password (min 8 chars)" value={form.password} onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))} required minLength={8} />
          <button className="w-full rounded bg-violet-600 px-3 py-2 text-white">Finish Signup</button>
        </form>
      ) : null}
      <div className="space-y-2">
        {OAUTH_PROVIDERS.map((provider) => (
          <button key={provider} type="button" onClick={() => onOauth(provider)} className="w-full rounded border px-3 py-2 text-sm">
            Sign up with {provider}
          </button>
        ))}
      </div>
      <p className="text-sm text-zinc-600 dark:text-zinc-300">
        Already have an account? <Link className="text-violet-600" to="/login">Login</Link>
      </p>
    </AuthLayout>
  )
}
