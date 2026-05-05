import axios from 'axios';
import { clearAccessToken, getAccessToken } from './tokenStorage';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL as string,
    timeout: 15000,
    withCredentials: true,
});

const authApi = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL as string,
    timeout: 15000,
    withCredentials: true,
});

api.interceptors.request.use((config) => {
    const accessToken = getAccessToken();
    if (accessToken) {
        config.headers = config.headers ?? {};
        config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config as (typeof error.config & { _retry?: boolean });
        if (error.response?.status !== 401 || originalRequest?._retry) {
            return Promise.reject(error);
        }
        originalRequest._retry = true;
        try {
            const refreshResponse = await authApi.post('/auth/refresh');
            const newAccessToken = refreshResponse.data?.accessToken as string | undefined;
            if (!newAccessToken) {
                throw new Error('Missing access token in refresh response');
            }
            localStorage.setItem('insight_access_token', newAccessToken);
            originalRequest.headers = originalRequest.headers ?? {};
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            return api(originalRequest);
        } catch (refreshError) {
            clearAccessToken();
            window.location.assign('/login');
            return Promise.reject(refreshError);
        }
    }
);

export default api;