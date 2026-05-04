import { useCallback, useEffect, useRef, useState } from 'react'
import axios from 'axios'
import { Briefcase } from 'lucide-react'
import { uploadCandidateResume } from './api/candidates'
import { FileUploadZone } from './components/FileUploadZone'
import { KanbanBoard } from './components/KanbanBoard'
import { ToastStack } from './components/ToastStack'
import { JOB_ID_PRESETS } from './constants/jobPresets'
import { useCandidatePolling } from './hooks/useCandidatePolling'
import { isValidUuid } from './lib/uuid'

let toastSeq = 0

function nextToastId() {
  toastSeq += 1
  return `toast-${toastSeq}`
}

function isCanceledError(error) {
  return axios.isAxiosError(error) && error.code === 'ERR_CANCELED'
}

function App() {
  const [jobId, setJobId] = useState('')
  const [candidates, setCandidates] = useState([])
  const [pollEpoch, setPollEpoch] = useState(0)
  const [toasts, setToasts] = useState([])
  const uploadControllersRef = useRef(new Set())

  const apiBase = import.meta.env.VITE_API_BASE_URL
  const jobOk = isValidUuid(jobId)

  const pushToast = useCallback((variant, message) => {
    const id = nextToastId()
    setToasts((prev) => [...prev, { id, variant, message }])
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 6000)
  }, [])

  const dismissToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const onPollError = useCallback(
    (message) => {
      pushToast('error', message)
    },
    [pushToast],
  )

  useCandidatePolling({
    jobId: jobOk ? jobId : '',
    setCandidates,
    onPollError,
    pollEpoch,
  })

  useEffect(() => {
    if (!jobOk) {
      setCandidates([])
    }
  }, [jobOk])

  useEffect(() => {
    return () => {
      uploadControllersRef.current.forEach((c) => c.abort())
    }
  }, [])

  const onRejectFile = useCallback(
    (message) => {
      pushToast('error', message)
    },
    [pushToast],
  )

  const onUploadFile = useCallback(
    async (file) => {
      if (!isValidUuid(jobId)) {
        pushToast('error', 'Enter a valid job UUID before uploading.')
        return
      }
      const ac = new AbortController()
      uploadControllersRef.current.add(ac)
      try {
        await uploadCandidateResume(jobId.trim(), file, ac.signal)
        pushToast('success', `Queued: ${file.name}`)
        setPollEpoch((n) => n + 1)
      } catch (error) {
        if (isCanceledError(error)) return
        const message =
          axios.isAxiosError(error) && error.response?.data?.message
            ? String(error.response.data.message)
            : error instanceof Error
              ? error.message
              : 'Upload failed'
        pushToast('error', `${file.name}: ${message}`)
      } finally {
        uploadControllersRef.current.delete(ac)
      }
    },
    [jobId, pushToast],
  )

  const selectPresetValue = JOB_ID_PRESETS.some((p) => p.value === jobId) ? jobId : ''

  return (
    <div className="flex flex-1 flex-col gap-8 px-4 py-8 sm:px-6">
      <ToastStack toasts={toasts} onDismiss={dismissToast} />

      <header className="text-left">
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50 sm:text-3xl">
          Insight Recruit
        </h1>
        <p className="mt-1 max-w-2xl text-sm text-zinc-600 dark:text-zinc-400">
          Upload resumes for a job and watch AI scoring move across the board. Polling runs only while
          candidates are pending or processing.
        </p>
      </header>

      {!apiBase && (
        <p className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-left text-sm text-amber-900 dark:border-amber-700 dark:bg-amber-950/50 dark:text-amber-100">
          Set <code className="rounded bg-black/5 px-1 dark:bg-white/10">VITE_API_BASE_URL</code> in{' '}
          <code className="rounded bg-black/5 px-1 dark:bg-white/10">.env</code> (see{' '}
          <code className="rounded bg-black/5 px-1 dark:bg-white/10">.env.example</code>).
        </p>
      )}

      <section className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm dark:border-zinc-700 dark:bg-zinc-950/50 sm:p-6">
        <div className="flex flex-wrap items-center gap-2 text-zinc-700 dark:text-zinc-300">
          <Briefcase className="h-5 w-5 shrink-0" aria-hidden />
          <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-50">Job</h2>
        </div>
        <div className="flex max-w-xl flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex min-w-0 flex-1 flex-col gap-1 text-left">
            <label htmlFor="job-id" className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Job ID (UUID)
            </label>
            <input
              id="job-id"
              type="text"
              autoComplete="off"
              placeholder="e.g. f47ac10b-58cc-4372-a567-0e02b2c3d479"
              value={jobId}
              onChange={(e) => setJobId(e.target.value)}
              className="w-full rounded-lg border border-zinc-300 bg-white px-3 py-2 font-mono text-sm text-zinc-900 outline-none ring-violet-500/40 focus:border-violet-500 focus:ring-2 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
            />
            {!jobOk && jobId.trim() !== '' && (
              <p className="text-xs text-red-600 dark:text-red-400">Enter a valid UUID.</p>
            )}
          </div>
          <div className="flex flex-col gap-1 text-left sm:w-56">
            <label htmlFor="job-preset" className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
              Preset
            </label>
            <select
              id="job-preset"
              value={selectPresetValue}
              onChange={(e) => {
                const v = e.target.value
                if (v) setJobId(v)
              }}
              className="rounded-lg border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-500/30 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
            >
              <option value="">Custom (use field)</option>
              {JOB_ID_PRESETS.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <FileUploadZone
          disabled={!jobOk || !apiBase}
          onReject={onRejectFile}
          onUploadFile={onUploadFile}
        />
      </section>

      <section className="text-left">
        <h2 className="mb-3 text-lg font-semibold text-zinc-900 dark:text-zinc-50">Pipeline</h2>
        {!jobOk ? (
          <p className="text-sm text-zinc-500">Set a valid job UUID to load the Kanban board.</p>
        ) : (
          <KanbanBoard candidates={candidates} />
        )}
      </section>
    </div>
  )
}

export default App
