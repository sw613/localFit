import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import {
    Box,
    Button,
    Card,
    FormControl,
    FormLabel,
    TextField,
    Typography,
    Stack,
    styled
  } from '@mui/material';

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

const AdditionalInfoContainer = styled(Stack)(({ theme }) => ({
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


const AdditionalInfoPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        nickname: '',
        birth: '',
        gender: ''
    });
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log('Submitting form data:', formData);
        try {
            // response 변수 추가
            const response = await api.put('/users/additional-info', formData);
            console.log('Success:', response.data);  // 성공 응답 확인
            
            if (response.status === 200) {
                navigate('/');
            }
        } catch (err) {
            console.error('Error:', err);
            setError(err.response?.data?.message || '추가 정보 입력에 실패했습니다.');
        }
    };

    return (
        <AdditionalInfoContainer direction="column" justifyContent="space-between">
            <StyledCard variant="outlined">
                <Typography variant="h5" component="h1" sx={{ textAlign: 'center', mb: 3 }}>
                    추가 정보 입력
                </Typography>

                <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <FormControl>
                        <FormLabel>닉네임</FormLabel>
                        <TextField
                            name="nickname"
                            required
                            fullWidth
                            value={formData.nickname}
                            onChange={handleChange}
                            error={!!error}
                            helperText={error}
                        />
                    </FormControl>

                    <FormControl>
                        <FormLabel>생년월일</FormLabel>
                        <TextField
                            type="date"
                            name="birth"
                            required
                            fullWidth
                            value={formData.birth}
                            onChange={handleChange}
                            InputLabelProps={{ shrink: true }}
                        />
                    </FormControl>

                    <FormControl>
                        <FormLabel>성별</FormLabel>
                        <TextField
                            select
                            name="gender"
                            required
                            fullWidth
                            value={formData.gender}
                            onChange={handleChange}
                            SelectProps={{ native: true }}
                        >
                            <option value="">선택해주세요</option>
                            <option value="MALE">남성</option>
                            <option value="FEMALE">여성</option>
                        </TextField>
                    </FormControl>

                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        size="large"
                    >
                        완료
                    </Button>
                </Box>
            </StyledCard>
        </AdditionalInfoContainer>
    );
};

export default AdditionalInfoPage;