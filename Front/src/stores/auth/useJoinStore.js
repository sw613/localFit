import { create } from 'zustand';
import api from '../../api/axios';

const useJoinStore = create((set, get) => ({
    formData: {
        name: '',
        nickname: '',
        birth: '',
        gender: '',
        email: '',
        password: '',
        confirmPassword: ''
    },
    errors: {},
    isLoading: false,

    setFormData: (data) => 
        set((state) => ({
            formData: { ...state.formData, ...data }
        })),

    validateForm: () => {
        const newErrors = {};
        const { formData } = get();

        // 이름 검증
        if (!formData.name) {
            newErrors.name = '이름을 입력해주세요';
        } else if (formData.name.length > 25) {
            newErrors.name = '25자 이하로 작성해 주세요';
        }

        // 닉네임 검증
        if (!formData.nickname) {
            newErrors.nickname = '닉네임을 입력해주세요';
        } else if (formData.nickname.length > 20) {
            newErrors.nickname = '20자 이하로 작성해 주세요';
        }

        // 생년월일 검증
        if (!formData.birth) {
            newErrors.birth = '생년월일을 입력해주세요';
        }

        // 성별 검증
        if (!formData.gender) {
            newErrors.gender = '성별을 선택해주세요';
        }

        // 이메일 검증
        const emailRegex = /^[a-zA-Z0-9]+@[a-zA-Z0-9]+(\.[a-z]+)+$/;
        if (!formData.email) {
            newErrors.email = '이메일을 입력해주세요';
        } else if (!emailRegex.test(formData.email)) {
            newErrors.email = '올바른 이메일 형식이 아닙니다';
        } else if (formData.email.length > 50) {
            newErrors.email = '50자 이하로 작성해 주세요';
        }

        // 비밀번호 검증
        const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\W)(?!.* ).{8,16}$/;
        if (!formData.password) {
            newErrors.password = '비밀번호를 입력해주세요';
        } else if (!passwordRegex.test(formData.password)) {
            newErrors.password = '8~16자리의 영대소문자, 특수문자, 숫자를 포함해주세요';
        }

        // 비밀번호 확인 검증
        if (!formData.confirmPassword) {
            newErrors.confirmPassword = '비밀번호 확인을 입력해주세요';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = '비밀번호가 일치하지 않습니다';
        }

        set({ errors: newErrors });
        return Object.keys(newErrors).length === 0;
    },

    submitJoin: async () => {
        const store = get();
        if (!store.validateForm()) {
            console.log('유효성 검사 실패');
            return false;
        }

        set({ isLoading: true });
        try {
            const response = await api.post('/users/join', store.formData);
            console.log('회원가입 성공:', response.data);
            return true;
        } catch (error) {
            console.error('에러 상세:', error);
            const errorMessage = error.response?.data?.message;
            
            if (errorMessage?.includes('이메일')) {
                set(state => ({
                    errors: { ...state.errors, email: errorMessage }
                }));
            } else if (errorMessage?.includes('닉네임')) {
                set(state => ({
                    errors: { ...state.errors, nickname: errorMessage }
                }));
            } else {
                set(state => ({
                    errors: {
                        ...state.errors,
                        submit: errorMessage || '회원가입에 실패했습니다.'
                    }
                }));
            }
            return false;
        } finally {
            set({ isLoading: false });
        }
    }
}));

export default useJoinStore;