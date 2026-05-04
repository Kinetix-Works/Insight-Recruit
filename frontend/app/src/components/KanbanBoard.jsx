import { useMemo } from 'react'
import { CandidateCard } from './CandidateCard'
import { KanbanColumn } from './KanbanColumn'

const COLUMNS = [
  { status: 'PENDING', title: 'Pending' },
  { status: 'PROCESSING', title: 'Processing' },
  { status: 'COMPLETED', title: 'Completed' },
  { status: 'FAILED', title: 'Failed' },
]

export function KanbanBoard({ candidates }) {
  const grouped = useMemo(() => {
    const map = { PENDING: [], PROCESSING: [], COMPLETED: [], FAILED: [] }
    for (const c of candidates) {
      const bucket = map[c.status]
      if (bucket) bucket.push(c)
    }
    return map
  }, [candidates])

  return (
    <div className="flex w-full flex-col gap-4 lg:flex-row lg:items-start">
      {COLUMNS.map((col) => (
        <KanbanColumn key={col.status} title={col.title} count={grouped[col.status].length}>
          {grouped[col.status].length === 0 ? (
            <p className="px-1 py-4 text-center text-xs text-zinc-400">No candidates</p>
          ) : (
            grouped[col.status].map((c) => <CandidateCard key={c.candidateId} candidate={c} />)
          )}
        </KanbanColumn>
      ))}
    </div>
  )
}
