import { create } from 'zustand';
import api from '../../api/axios';
import useAuthStore from '../../stores/auth/useAuthStore';

const useMyPageStore = create((set, get) => ({
    // State
    editData: {
        nickname: '',
        birth: '',
        gender: ''
    },
    passwordData: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    },
    deletePassword: '',
    isEditing: false,
    passwordError: '',
    deleteError: '',

    // Actions
    setEditMode: (isEditing) => set({ isEditing }),
    
    updateEditData: (data) => set({ editData: { ...get().editData, ...data } }),
    
    updatePasswordData: (data) => set({ 
        passwordData: { ...get().passwordData, ...data } 
    }),
    
    setDeletePassword: (password) => set({ deletePassword: password }),
    
    handleEditSubmit: async (userId) => {
        try {
            const response = await api.put(`/users/${userId}`, get().editData);
            useAuthStore.getState().checkAuthStatus(); // 유저 정보 갱신
            set({ isEditing: false });
            return { success: true };
        } catch (err) {
            return { 
                success: false, 
                error: err.response?.data?.message || '정보 수정에 실패했습니다.' 
            };
        }
    },

    validatePassword: () => {
        const { passwordData } = get();
        if (passwordData.newPassword !== passwordData.confirmPassword) {
            set({ passwordError: '새 비밀번호가 일치하지 않습니다.' });
            return false;
        }

        const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\W)(?!.* ).{8,16}$/;
        if (!passwordRegex.test(passwordData.newPassword)) {
            set({ passwordError: '비밀번호는 8~16자리의 영대소문자, 특수문자, 숫자를 포함해야 합니다.' });
            return false;
        }

        return true;
    },

    handlePasswordChange: async (userId) => {
        const { passwordData, validatePassword } = get();
        
        if (!validatePassword()) return false;

        try {
            await api.put(`/users/${userId}/password`, {
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword
            });

            set({
                passwordData: {
                    currentPassword: '',
                    newPassword: '',
                    confirmPassword: ''
                },
                passwordError: ''
            });
            return { success: true };
        } catch (err) {
            set({ passwordError: err.response?.data?.message || '비밀번호 변경에 실패했습니다.' });
            return { success: false };
        }
    },

    handleDeleteAccount: async (userId, isLocal) => {
        const { deletePassword } = get();
        try {
            if (isLocal) {
                if (!deletePassword) {
                    set({ deleteError: "비밀번호를 입력해주세요." });
                    return { success: false };
                }
                await api.delete(`/users/${userId}`, {
                    data: { password: deletePassword }
                });
            } else {
                await api.delete(`/users/${userId}`);
            }
            // MyPage 스토어 초기화
            get().resetStore();    
            // Auth 스토어 초기화
            await useAuthStore.getState().logout();

            return { success: true };
        } catch (err) {
            const errorMessage = err.response?.data?.message || "회원 탈퇴에 실패했습니다.";
            set({ deleteError: errorMessage });
            return { success: false };
        }
    },

    clearErrors: () => set({
        passwordError: '',
        deleteError: ''
    }),

    resetStore: () => set({
        editData: { nickname: '', birth: '', gender: '' },
        passwordData: { currentPassword: '', newPassword: '', confirmPassword: '' },
        deletePassword: '',
        isEditing: false,
        passwordError: '',
        deleteError: ''
    })
}));

export default useMyPageStore;