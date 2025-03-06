import { create } from 'zustand';
import api from '../../api/axios';

const useAuthStore = create((set) => ({
    credentials: {
        email: '',
        password: ''
    },
    user: null,
    error: null,
    loading: false,
    isLoggedIn: false,

    setCredentials: (newCredentials) => set((state) => ({
        credentials: { ...state.credentials, ...newCredentials }
    })),

    checkAuthStatus: async () => {
        try {
          // API 호출을 통해 인증 상태 직접 확인
          const { data: userData } = await api.get('/users/mypage');
          // 성공 시 사용자 정보 저장 및 인증 상태 설정
          set({
            user: userData,
            isLoggedIn: true
          });
          return true;
        } catch (error) {
          console.error('Auth check error:', error);
          // API 호출 실패 시 비인증 상태로 설정
          set({
            user: null,
            isLoggedIn: false
          });
          // 오류가 401인 경우 로그아웃 처리는 axios 인터셉터에서 담당
          return false;
        }
    },

    login: async (credentials) => {
        set({ loading: true, error: null });
        try {
            await api.post('/auth/login', credentials);
            const { data: userData } = await api.get('/users/mypage');

            console.log("사용자 데이터:", userData);
            console.log("역할 값:", userData.role);

            set({
                user: userData,
                isLoggedIn: true,
                error: null
            });
            // 관리자 여부에 따라 리다이렉션 정보 반환
            const userRole = userData.role;
        const isAdmin = 
            userRole === 'ADMIN' || 
            userRole === 'ROLE_ADMIN' ||
            (typeof userRole === 'object' && (userRole.value === 'ADMIN' || userRole.name === 'ADMIN')) ||
            String(userRole).includes('ADMIN');
        
        console.log("관리자 여부:", isAdmin);
        
        if (isAdmin) {
            return { success: true, redirectTo: '/admin/dashboard' };
        }
        return { success: true, redirectTo: '/' };
    } catch (err) {
        // 기존 에러 처리 코드...
    } finally {
        set({ loading: false });
    }
},

    logout: async () => {
        try {
            await api.post('/auth/logout');
        } catch (error) {
            console.error('Logout error:', error);
        } finally {
            set({
              user: null,
              isLoggedIn: false,
              error: null,
              // credentials 상태도 초기화
              credentials: {
                email: '',
                password: ''
              }
            });
        }
    },
    // 사용자가 관리자인지 확인하는 헬퍼 함수
    isAdmin: () => {
        const state = useAuthStore.getState();
        return state.user?.role === 'ADMIN';
    }
}));

export default useAuthStore;