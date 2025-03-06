import axios from 'axios';
import { handleLogout } from '../utils/authUtils';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 5000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    }
});

api.interceptors.response.use(
    response => {
        return response;
    },
    async error => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            
            try {
                await api.post('/auth/refresh');
                return api(originalRequest);
            } catch (refreshError) {
                try {
                    await api.post('/auth/logout');
                } catch (logoutError) {
                    console.error('Logout failed:', logoutError);
                }
                
            
                handleLogout();
                return Promise.reject(refreshError);
            }
        }
        
        return Promise.reject(error);
    }
);

export default api;
