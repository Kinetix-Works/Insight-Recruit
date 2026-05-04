import { useCallback, useMemo, useState } from 'react'
import { CandidateCard } from './CandidateCard'
import { KanbanColumn } from './KanbanColumn'
import { useToasts } from './toast-context'
import { useCandidatePolling } from '../hooks/useCandidatePolling'

const COLUMNS = [
  { status: 'PENDING', title: 'Pending', emptyMessage: 'New resumes wait here before processing starts.' },
  { status: 'PROCESSING', title: 'Processing', emptyMessage: 'No resumes are currently being analyzed.' },
  { status: 'COMPLETED', title: 'Completed', emptyMessage: 'Completed screenings will appear here.' },
  { status: 'FAILED', title: 'Failed', emptyMessage: 'Failures will be listed here for follow-up.' },
]

export function KanbanBoard({ jobId, pollEpoch }) {
  const [candidates, setCandidates] = useState([])
  const { pushToast } = useToasts()

  const onPollError = useCallback(
    (message) => {
      pushToast('error', `Polling error: ${message}`)
    },
    [pushToast],
  )

  useCandidatePolling({
    jobId,
    pollEpoch,
    setCandidates,
    onPollError,
  })

  const grouped = useMemo(() => {
    const map = { PENDING: [], PROCESSING: [], COMPLETED: [], FAILED: [] }
    for (const c of candidates) {
      const bucket = map[c.status]
      if (bucket) bucket.push(c)
    }
    return map
  }, [candidates])

  const hasCandidates = candidates.length > 0

  return (
    <div className="flex w-full flex-col gap-4">
      {!hasCandidates && (
        <div className="rounded-xl border border-zinc-200 bg-zinc-50 px-4 py-3 text-sm text-zinc-500 dark:border-zinc-700 dark:bg-zinc-900/40 dark:text-zinc-400">
          Waiting for resumes in this job. Upload files to start asynchronous screening.
        </div>
      )}
      <div className="flex w-full flex-col gap-4 lg:flex-row lg:items-start">
      {COLUMNS.map((col) => (
        <KanbanColumn
          key={col.status}
          title={col.title}
          count={grouped[col.status].length}
          emptyMessage={col.emptyMessage}
        >
          {grouped[col.status].length === 0 ? (
            <div className="rounded-lg border border-dashed border-zinc-200 px-3 py-8 text-center text-xs text-zinc-400 dark:border-zinc-700 dark:text-zinc-500">
              {col.emptyMessage}
            </div>
          ) : (
            grouped[col.status].map((c) => <CandidateCard key={c.candidateId} candidate={c} />)
          )}
        </KanbanColumn>
      ))}
      </div>
    </div>
  )
}
