import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CssBaseline,
  Divider,
  FormControl,
  FormLabel,
  TextField,
  Typography,
  Stack,
  styled,
  Snackbar,
  Alert
} from '@mui/material';
import { GoogleIcon } from '../../components/auth/CustomIcons.jsx';
import useJoinStore from '../../stores/auth/useJoinStore.js';


const StyledCard = styled(Card)(({ theme }) => ({
    display: 'flex',
    flexDirection: 'column',
    width: '100%',
    padding: theme.spacing(2),
    gap: theme.spacing(1.5),
    margin: 'auto',
    boxShadow:
      'hsla(220, 30%, 5%, 0.05) 0px 5px 15px 0px, hsla(220, 25%, 10%, 0.05) 0px 15px 35px -5px',
    [theme.breakpoints.up('sm')]: {
      width: '450px',
      padding: theme.spacing(3),
    }
  }));

const JoinContainer = styled(Stack)(({ theme }) => ({
    minHeight: '100vh',
    padding: theme.spacing(1),
    [theme.breakpoints.up('sm')]: {
      padding: theme.spacing(2),
    },
    '&::before': {
      content: '""',
      display: 'block',
      position: 'absolute',
      zIndex: -1,
      inset: 0,
      backgroundImage:
        'radial-gradient(ellipse at 50% 50%, hsl(210, 100%, 97%), hsl(0, 0%, 100%))',
      backgroundRepeat: 'no-repeat',
    }
  }));

const JoinPage = () => {
  const navigate = useNavigate();
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const { formData, errors, isLoading, setFormData, submitJoin } = useJoinStore();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ [name]: value });
  };

  const handleGoogleLogin = () => {
    window.location.href = import.meta.env.VITE_APP_GOOGLE_AUTH_URL;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const success = await submitJoin();
    
    if (success) {
      setOpenSnackbar(true);
      // 1.5초 후 로그인 페이지로 이동
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    }
  };

  return (
    <JoinContainer direction="column" justifyContent="space-between">
      <CssBaseline />
      <StyledCard variant="outlined">
        <Typography
          component="h1"
          variant="h5"
          sx={{ textAlign: 'center', mb: 1 }}
        >
          회원가입
        </Typography>
        
        <Box
          component="form"
          onSubmit={handleSubmit}
          sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}
        >
          <FormControl size="small">
            <FormLabel htmlFor="name">이름</FormLabel>
            <TextField
              size="small"
              name="name"
              required
              fullWidth
              value={formData.name}
              onChange={handleChange}
              placeholder="이름은 변경이 불가능합니다"
              error={!!errors.name}
              helperText={errors.name}
            />
          </FormControl>

          <FormControl size="small">
            <FormLabel htmlFor="nickname">닉네임</FormLabel>
            <TextField
              size="small"
              name="nickname"
              required
              fullWidth
              value={formData.nickname}
              onChange={handleChange}
              placeholder=""
              error={!!errors.nickname}
              helperText={errors.nickname}
            />
          </FormControl>

          <FormControl size="small">
            <FormLabel htmlFor="birth">생년월일</FormLabel>
            <TextField
              size="small"
              type="date"
              name="birth"
              required
              fullWidth
              value={formData.birth}
              onChange={handleChange}
              error={!!errors.birth}
              helperText={errors.birth}
              InputLabelProps={{ shrink: true }}
            />
          </FormControl>

          <FormControl size="small">
            <FormLabel htmlFor="gender">성별</FormLabel>
            <TextField
              size="small"
              select
              name="gender"
              required
              fullWidth
              value={formData.gender}
              onChange={handleChange}
              SelectProps={{ native: true }}
              error={!!errors.gender}
              helperText={errors.gender}
            >
              <option value="">선택해주세요</option>
              <option value="MALE">남성</option>
              <option value="FEMALE">여성</option>
            </TextField>
          </FormControl>

          <FormControl size="small">
            <FormLabel htmlFor="email">이메일</FormLabel>
            <TextField
              size="small"
              required
              fullWidth
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="example@email.com"
              error={!!errors.email}
              helperText={errors.email}
            />
          </FormControl>

          <FormControl size="small">
            <FormLabel htmlFor="password">비밀번호</FormLabel>
            <TextField
              size="small"
              required
              fullWidth
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="8~16자리 영대소문자, 특수문자"
              error={!!errors.password}
              helperText={errors.password}
            />
          </FormControl>

          <FormControl size="small">
              <FormLabel htmlFor="confirmPassword">비밀번호 확인</FormLabel>
              <TextField
                  size="small"
                  required
                  fullWidth
                  name="confirmPassword"
                  type="password"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder=""
                  error={!!errors.confirmPassword}
                  helperText={errors.confirmPassword}
              />
          </FormControl>

          {errors.submit && (
            <Typography color="error" variant="caption">
              {errors.submit}
            </Typography>
          )}

          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={isLoading}
            size="medium"
          >
            {isLoading ? '처리중...' : '회원가입'}
          </Button>
        </Box>

        <Divider sx={{ my: 1 }}>또는</Divider>

        <Button
          fullWidth
          variant="outlined"
          startIcon={<GoogleIcon />}
          onClick={handleGoogleLogin}
          sx={{ mb: 1 }}
        >
          Google로 회원가입
        </Button>
      </StyledCard>

      <Snackbar 
        open={openSnackbar} 
        autoHideDuration={1500} 
        onClose={() => setOpenSnackbar(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert severity="success" sx={{ width: '100%' }}>
          회원가입이 완료되었습니다!
        </Alert>
      </Snackbar>
    </JoinContainer>
  ); 
};

export default JoinPage;