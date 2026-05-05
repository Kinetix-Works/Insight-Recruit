import { useMemo, useState } from 'react'
import { Briefcase } from 'lucide-react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { FileUploadZone } from './components/FileUploadZone'
import { KanbanBoard } from './components/KanbanBoard'
import { ToastStack } from './components/ToastStack'
import { ToastProvider } from './components/toast-context'
import { AuthProvider, useAuth } from './components/AuthContext'
import { Login } from './components/Login'
import { Signup } from './components/Signup'
import { JOB_ID_PRESETS } from './constants/jobPresets'

function Dashboard() {
  const { user, logout } = useAuth()
  const presets = useMemo(() => {
    if (Array.isArray(JOB_ID_PRESETS)) return JOB_ID_PRESETS
    return Object.entries(JOB_ID_PRESETS || {}).map(([label, value]) => ({ label, value }))
  }, [])
  const [selectedJobId, setSelectedJobId] = useState(presets[0]?.value ?? '')
  const [pollEpoch, setPollEpoch] = useState(0)
  const bumpPollEpoch = () => setPollEpoch((prev) => prev + 1)

  return (
    <ToastProvider>
      <div className="flex flex-1 flex-col gap-8 px-4 py-8 sm:px-6">
        <ToastStack />

        <header className="text-left">
          <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50 sm:text-3xl">
            Insight Recruit
          </h1>
          <p className="mt-1 max-w-2xl text-sm text-zinc-600 dark:text-zinc-400">
            Upload resumes per role and monitor async screening outcomes in real time.
          </p>
          <div className="mt-3 flex items-center gap-3">
            <span className="text-xs text-zinc-500">{user?.email}</span>
            <button className="rounded border px-2 py-1 text-xs" onClick={logout}>Sign out</button>
          </div>
        </header>

        <section className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-700 dark:bg-zinc-950/50 sm:p-6">
          <div className="flex items-center gap-2 text-zinc-700 dark:text-zinc-300">
            <Briefcase className="h-5 w-5 shrink-0" aria-hidden />
            <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">Job Selection</h2>
          </div>

          <div className="max-w-xl">
            <label htmlFor="job-preset" className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Active job
            </label>
            <select
              id="job-preset"
              value={selectedJobId}
              onChange={(event) => setSelectedJobId(event.target.value)}
              className="w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-500/30 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
            >
              {presets.map((preset) => (
                <option key={preset.value} value={preset.value}>
                  {preset.label}
                </option>
              ))}
            </select>
          </div>

          <FileUploadZone jobId={selectedJobId} onUploadSuccess={bumpPollEpoch} />
        </section>

        <section className="text-left">
          <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">Pipeline</h2>
          <KanbanBoard jobId={selectedJobId} pollEpoch={pollEpoch} />
        </section>
      </div>
    </ToastProvider>
  )
}

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return children
}

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route
          path="/"
          element={(
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          )}
        />
      </Routes>
    </AuthProvider>
  )
}

export default App
