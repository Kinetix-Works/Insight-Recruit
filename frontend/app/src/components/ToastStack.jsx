import { X } from 'lucide-react'
import { useToasts } from './toast-context'

export function ToastStack() {
  const { toasts, dismissToast } = useToasts()
  if (toasts.length === 0) return null

  return (
    <div
      className="pointer-events-none fixed bottom-4 right-4 z-50 flex max-w-sm flex-col gap-2"
      aria-live="polite"
    >
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`pointer-events-auto flex items-start gap-2 rounded-lg border px-3 py-2 text-sm shadow-lg ${
            t.variant === 'error'
              ? 'border-red-300 bg-red-50 text-red-900 dark:border-red-800 dark:bg-red-950/90 dark:text-red-100'
              : 'border-emerald-300 bg-emerald-50 text-emerald-900 dark:border-emerald-800 dark:bg-emerald-950/90 dark:text-emerald-100'
          }`}
        >
          <p className="min-w-0 flex-1 text-left leading-snug">{t.message}</p>
          <button
            type="button"
            className="shrink-0 rounded p-0.5 hover:bg-black/10 dark:hover:bg-white/10"
            onClick={() => dismissToast(t.id)}
            aria-label="Dismiss"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  )
}
