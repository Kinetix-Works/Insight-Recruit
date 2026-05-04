import { AlertCircle, CheckCircle2, Clock, Loader2 } from 'lucide-react'

const STATUS_META = {
  PENDING: {
    label: 'Pending',
    icon: Clock,
    iconClass: 'text-amber-600 dark:text-amber-400',
  },
  PROCESSING: {
    label: 'Processing',
    icon: Loader2,
    iconClass: 'text-sky-600 animate-spin dark:text-sky-400',
  },
  COMPLETED: {
    label: 'Completed',
    icon: CheckCircle2,
    iconClass: 'text-emerald-600 dark:text-emerald-400',
  },
  FAILED: {
    label: 'Failed',
    icon: AlertCircle,
    iconClass: 'text-red-600 dark:text-red-400',
  },
}

function scoreClass(score) {
  if (score > 80) return 'text-emerald-700 dark:text-emerald-300'
  if (score >= 50) return 'text-amber-700 dark:text-amber-300'
  return 'text-red-700 dark:text-red-300'
}

export function CandidateCard({ candidate }) {
  const meta = STATUS_META[candidate.status] ?? STATUS_META.PENDING
  const Icon = meta.icon
  const numericScore = candidate.score == null ? null : Number(candidate.score)

  return (
    <article className="rounded-lg border border-zinc-200 bg-white p-3 shadow-sm dark:border-zinc-700 dark:bg-zinc-900">
      <div className="flex items-start gap-2">
        <Icon className={`mt-0.5 h-4 w-4 shrink-0 ${meta.iconClass}`} aria-hidden />
        <div className="min-w-0 flex-1 text-left">
          <p className="truncate font-medium text-zinc-900 dark:text-zinc-100" title={candidate.fileName}>
            {candidate.fileName}
          </p>
          <p className="text-xs text-zinc-500 dark:text-zinc-400">{meta.label}</p>
        </div>
      </div>
      {candidate.status === 'COMPLETED' && numericScore != null && !Number.isNaN(numericScore) && (
        <p className={`mt-2 text-left text-sm font-semibold ${scoreClass(numericScore)}`}>
          AI score: {numericScore.toFixed(1)}
        </p>
      )}
    </article>
  )
}
