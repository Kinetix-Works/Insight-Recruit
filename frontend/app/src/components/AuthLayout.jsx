import { ShieldCheck } from 'lucide-react'

export function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="min-h-screen bg-zinc-100 px-4 py-10 dark:bg-zinc-950">
      <div className="mx-auto flex w-full max-w-md flex-col gap-6 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
        <div className="flex items-center gap-2 text-violet-600">
          <ShieldCheck className="h-5 w-5" />
          <span className="text-sm font-semibold uppercase tracking-wide">Insight Recruit Auth</span>
        </div>
        <div>
          <h1 className="text-2xl font-semibold text-zinc-900 dark:text-zinc-50">{title}</h1>
          <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">{subtitle}</p>
        </div>
        {children}
      </div>
    </div>
  )
}
