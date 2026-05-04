import api from '../services/api'

/**
 * @param {string} jobId
 * @param {AbortSignal} [signal]
 */
export async function fetchCandidatesByJob(jobId, signal) {
  const { data } = await api.get('/candidates', {
    params: { jobId },
    signal,
  })
  return data
}

/**
 * @param {string} jobId
 * @param {File} file
 * @param {AbortSignal} [signal]
 */
export async function uploadCandidateResume(jobId, file, signal) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('jobId', jobId)
  const { data } = await api.post('/candidates/upload', formData, { signal })
  return data
}
