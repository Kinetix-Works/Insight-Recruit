import { useCallback, useRef, useState } from 'react'
import { Upload } from 'lucide-react'

const MAX_BYTES = 5 * 1024 * 1024

function validateResumeFile(file) {
  const name = file.name.toLowerCase()
  if (!name.endsWith('.pdf') && !name.endsWith('.docx')) {
    return 'Only .pdf and .docx files are allowed.'
  }
  if (file.size > MAX_BYTES) {
    return 'File must be 5MB or smaller.'
  }
  return null
}

export function FileUploadZone({ disabled, onReject, onUploadFile }) {
  const inputRef = useRef(null)
  const [dragOver, setDragOver] = useState(false)

  const handleFiles = useCallback(
    async (fileList) => {
      const files = Array.from(fileList || [])
      for (const file of files) {
        const err = validateResumeFile(file)
        if (err) {
          onReject(err)
          continue
        }
        await onUploadFile(file)
      }
    },
    [onReject, onUploadFile],
  )

  const onDrop = (e) => {
    e.preventDefault()
    setDragOver(false)
    if (disabled) return
    void handleFiles(e.dataTransfer.files)
  }

  const onDragOver = (e) => {
    e.preventDefault()
    if (!disabled) setDragOver(true)
  }

  const onDragLeave = () => setDragOver(false)

  const onInputChange = (e) => {
    void handleFiles(e.target.files)
    e.target.value = ''
  }

  return (
    <div className="w-full max-w-xl">
      <label className="mb-2 block text-left text-sm font-medium text-zinc-700 dark:text-zinc-300">
        Upload resumes
      </label>
      <button
        type="button"
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
        onDrop={onDrop}
        onDragOver={onDragOver}
        onDragLeave={onDragLeave}
        className={`flex w-full cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-6 py-10 transition-colors ${
          disabled
            ? 'cursor-not-allowed border-zinc-200 bg-zinc-100/50 opacity-60 dark:border-zinc-700 dark:bg-zinc-800/30'
            : dragOver
              ? 'border-violet-500 bg-violet-50 dark:border-violet-400 dark:bg-violet-950/40'
              : 'border-zinc-300 bg-zinc-50 hover:border-violet-400 hover:bg-violet-50/40 dark:border-zinc-600 dark:bg-zinc-900/50 dark:hover:border-violet-500'
        }`}
      >
        <Upload className="h-8 w-8 text-zinc-500 dark:text-zinc-400" aria-hidden />
        <span className="text-sm text-zinc-600 dark:text-zinc-300">
          Drag and drop files here, or click to browse
        </span>
        <span className="text-xs text-zinc-500">PDF or DOCX · max 5MB each</span>
      </button>
      <input
        ref={inputRef}
        type="file"
        className="sr-only"
        accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        multiple
        disabled={disabled}
        onChange={onInputChange}
      />
    </div>
  )
}
