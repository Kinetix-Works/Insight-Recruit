import { createContext, useCallback, useContext, useMemo, useState } from 'react'

const ToastContext = createContext(null)

let toastSeq = 0

function nextToastId() {
  toastSeq += 1
  return `toast-${toastSeq}`
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismissToast = useCallback((id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id))
  }, [])

  const pushToast = useCallback((variant, message, timeoutMs = 5000) => {
    const id = nextToastId()
    setToasts((prev) => [...prev, { id, variant, message }])
    window.setTimeout(() => {
      setToasts((prev) => prev.filter((toast) => toast.id !== id))
    }, timeoutMs)
  }, [])

  const value = useMemo(
    () => ({
      toasts,
      dismissToast,
      pushToast,
    }),
    [dismissToast, pushToast, toasts],
  )

  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>
}

export function useToasts() {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToasts must be used within ToastProvider')
  }
  return context
}
