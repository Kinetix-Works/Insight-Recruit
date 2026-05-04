import { useEffect, useRef } from 'react'
import axios from 'axios'
import { fetchCandidatesByJob } from '../api/candidates'

const ACTIVE = new Set(['PENDING', 'PROCESSING'])

function hasActiveStatuses(list) {
  return list.some((c) => ACTIVE.has(c.status))
}

function isCanceledError(error) {
  return axios.isAxiosError(error) && error.code === 'ERR_CANCELED'
}

/**
 * Polls GET /candidates while any row is PENDING or PROCESSING.
 * Stops when all are COMPLETED or FAILED. Restarts when `pollEpoch` increments (e.g. after upload).
 */
export function useCandidatePolling({ jobId, setCandidates, onPollError, pollEpoch }) {
  const jobIdRef = useRef(jobId)
  const onPollErrorRef = useRef(onPollError)
  jobIdRef.current = jobId
  onPollErrorRef.current = onPollError

  useEffect(() => {
    const trimmed = jobId?.trim()
    if (!trimmed) {
      return undefined
    }

    const abortController = new AbortController()
    let timeoutId
    let stopped = false

    const scheduleNext = (delayMs) => {
      if (stopped) return
      timeoutId = window.setTimeout(runTick, delayMs)
    }

    const runTick = async () => {
      if (stopped) return
      const id = jobIdRef.current?.trim()
      if (!id) return

      try {
        const rows = await fetchCandidatesByJob(id, abortController.signal)
        if (stopped) return
        setCandidates(rows)
        if (hasActiveStatuses(rows) && !stopped) {
          scheduleNext(3000)
        }
      } catch (error) {
        if (isCanceledError(error)) return
        const message =
          axios.isAxiosError(error) && error.response?.data?.message
            ? String(error.response.data.message)
            : error instanceof Error
              ? error.message
              : 'Failed to refresh candidates'
        onPollErrorRef.current(message)
        if (!stopped) {
          scheduleNext(5000)
        }
      }
    }

    runTick()

    return () => {
      stopped = true
      abortController.abort()
      if (timeoutId) window.clearTimeout(timeoutId)
    }
  }, [jobId, pollEpoch, setCandidates])
}
