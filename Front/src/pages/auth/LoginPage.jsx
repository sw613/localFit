import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import useAuthStore from '../../stores/auth/useAuthStore.js';
import { LoginContainer, LoginCard } from '../../styles/auth/LoginPage.styles.js';
import {
  TextField,
  Button,
  FormControlLabel,
  Checkbox,
  Typography,
  Divider,
  Box
} from '@mui/material';
import { GoogleIcon } from '../../components/auth/CustomIcons.jsx';  

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const {
    credentials,
    error,
    loading,
    isLoggedIn,
    setCredentials,
    login,
    loginWithGoogle,
    checkAuthStatus
  } = useAuthStore();

  const [rememberMe, setRememberMe] = useState(false);

  useEffect(() => {
    // 저장된 이메일이 있으면 불러오기
    const savedEmail = localStorage.getItem('rememberedEmail');
    if (savedEmail) {
      setCredentials({ email: savedEmail });
      setRememberMe(true);
    }
    
    // 로그인 상태 확인하여 리다이렉트
    const checkLogin = async () => {
      // API를 통해 인증 상태 확인
      const isAuthenticated = await checkAuthStatus();
      if (isAuthenticated) {
        // 로그인된 사용자의 정보를 확인
        const { user } = useAuthStore.getState();
        // 관리자인 경우 관리자 페이지로 리다이렉트
        if (user && user.role === 'ADMIN') {
          navigate('/admin/users', { replace: true });
        } else {
          navigate('/', { replace: true });
        }
      }
    };
    
    checkLogin();
  }, [checkAuthStatus, navigate, setCredentials]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials({ [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Remember me 처리
    if (rememberMe) {
      localStorage.setItem('rememberedEmail', credentials.email);
    } else {
      localStorage.removeItem('rememberedEmail');
    }

    try {
      console.log("로그인 시도:", credentials);
      const result = await login(credentials);
      console.log("로그인 결과:", result);
      
      if (result && result.success) {
          console.log("리다이렉션 경로:", result.redirectTo);
          navigate(result.redirectTo || '/', { replace: true });
      }
  } catch (error) {
      console.error("로그인 오류:", error);
  }
};

  const handleGoogleLogin = () => {
    window.location.href = import.meta.env.VITE_APP_GOOGLE_AUTH_URL;
  };

  return (
    <LoginContainer>
      <LoginCard>
        <Typography variant="h5" component="h1" align="center" gutterBottom>
          로그인
        </Typography>

        {error && (
          <Typography color="error" align="center" gutterBottom>
            {error}
          </Typography>
        )}

        <form onSubmit={handleSubmit}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="email"
            label="이메일"
            name="email"
            autoComplete="email"
            autoFocus
            value={credentials.email}
            onChange={handleChange}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="비밀번호"
            type="password"
            id="password"
            autoComplete="current-password"
            value={credentials.password}
            onChange={handleChange}
          />
          
          <FormControlLabel
            control={
              <Checkbox
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                color="primary"
              />
            }
            label="Remember Me"
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={loading}
            sx={{ mt: 2 }}
          >
            {loading ? '로그인 중...' : '로그인'}
          </Button>
        </form>

        <Divider sx={{ my: 2 }}>또는</Divider>

        <Button
          fullWidth
          variant="outlined"
          startIcon={<GoogleIcon />}
          onClick={handleGoogleLogin}
          sx={{ mb: 2 }}
        >
          Google로 로그인
        </Button>

        <Box textAlign="center">
          <Typography variant="body2">
            계정이 없으신가요?{' '}
            <Button
              onClick={() => navigate('/join')}
              color="primary"
            >
              회원가입
            </Button>
          </Typography>
        </Box>
      </LoginCard>
    </LoginContainer>
  );
};

export default LoginPage;