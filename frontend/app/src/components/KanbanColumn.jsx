export function KanbanColumn({ title, count, emptyMessage, children }) {
  return (
    <section className="flex min-h-[220px] min-w-0 flex-1 flex-col rounded-xl border border-zinc-200 bg-zinc-50/80 dark:border-zinc-700 dark:bg-zinc-900/40">
      <header className="flex items-center justify-between border-b border-zinc-200 px-3 py-2 dark:border-zinc-700">
        <div className="min-w-0">
          <h2 className="text-sm font-semibold text-zinc-800 dark:text-zinc-100">{title}</h2>
          {emptyMessage ? <p className="text-xs text-zinc-400 dark:text-zinc-500">Stage status</p> : null}
        </div>
        <span className="rounded-full bg-zinc-200 px-2 py-0.5 text-xs font-medium text-zinc-700 dark:bg-zinc-700 dark:text-zinc-200">
          {count}
        </span>
      </header>
      <div className="flex flex-1 flex-col gap-2 p-2">{children}</div>
    </section>
  )
}
