import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import PersonIcon from '@mui/icons-material/Person';
import LockIcon from '@mui/icons-material/Lock';
import PersonOffIcon from '@mui/icons-material/PersonOff';
import DynamicFeedIcon from '@mui/icons-material/DynamicFeed';
import GroupsIcon from '@mui/icons-material/Groups';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import {
    Container,
    Paper,
    Typography,
    Button,
    TextField,
    Select,
    MenuItem,
    FormControl,
    FormLabel,
    FormControlLabel,
    InputLabel,
    Box,
    Grid,
    Alert,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    DialogContentText,
    List,
    ListItem,
    ListItemText,
    ListItemButton,
    Divider,
    RadioGroup,
    Radio
} from '@mui/material';
import { styled } from '@mui/material/styles';
import useAuthStore from '../../stores/auth/useAuthStore';
import useMyPageStore from '../../stores/user/useMyPageStore';
import api from '../../api/axios';

const StyledPaper = styled(Paper)(({ theme }) => ({
    padding: theme.spacing(3),
    marginBottom: theme.spacing(3)
}));

const PageContainer = styled(Box)(({ theme }) => ({
    display: 'flex',
    gap: theme.spacing(4),
    padding: theme.spacing(4),
    backgroundColor: '#f5f5f5',
    minHeight: '100vh'
}));

const Navigation = styled(Paper)(({ theme }) => ({
    width: 300,
    height: 'fit-content',
    '& .MuiListItemButton-root': {
        padding: theme.spacing(2),
        '& .MuiListItemText-primary': {
            fontSize: '1.1rem',
            fontWeight: 500
        }
    },
    '& .Mui-selected': {
        backgroundColor: theme.palette.primary.light,
        '&:hover': {
            backgroundColor: theme.palette.primary.light
        }
    }
}));

const ContentArea = styled(Box)(({ theme }) => ({
    flex: 1,
    maxWidth: '800px'
}));

const MyPage = () => {
    const navigate = useNavigate();
    const [currentSection, setCurrentSection] = useState('basic');
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [nicknameError, setNicknameError] = useState('');

    // Auth Store
    const { user, checkAuthStatus, logout } = useAuthStore();

    // MyPage Store
    const {
        editData,
        passwordData,
        deletePassword,
        isEditing,
        passwordError,
        deleteError,
        setEditMode,
        updateEditData,
        updatePasswordData,
        setDeletePassword,
        handleEditSubmit,
        handlePasswordChange,
        handleDeleteAccount,
        clearErrors
    } = useMyPageStore();

    useEffect(() => {
        checkAuthStatus();
    }, []);

    // 정보 수정 시 현재 사용자 정보로 초기화
    const handleEditMode = () => {
        updateEditData({
            nickname: user.nickname,
            birth: user.birth,
            gender: user.gender
        });
        setEditMode(true);
    };

    // 닉네임 수정 시 공백 불가 처리
    const validateNickname = (nickname) => {
        if (nickname.trim() === '') {
            return '닉네임을 입력해주세요.';
        }
        if (nickname.includes(' ')) {
            return '닉네임에 공백을 포함할 수 없습니다.';
        }
        return '';
    };

    const handleEdit = async (e) => {
        e.preventDefault();
        
        // 닉네임 유효성 검사
        const nicknameError = validateNickname(editData.nickname);
        if (nicknameError) {
            alert(nicknameError);
            return;
        }

        const { success, error } = await handleEditSubmit(user.id);
        if (success) {
            alert('정보가 수정되었습니다.');
        } else {
            alert(error);
        }
    };

    const handlePassword = async (e) => {
        e.preventDefault();
        const { success } = await handlePasswordChange(user.id);
        if (success) {
            alert('비밀번호가 변경되었습니다. 다시 로그인해주세요.');
            await logout();
            navigate('/login');
        }
    };

    const handleDelete = async () => {
        const { success } = await handleDeleteAccount(user.id, user.provider === 'LOCAL');
        if (success) {
            setDeleteDialogOpen(false);
            alert("회원 탈퇴가 완료되었습니다.");
            navigate('/');
        }
    };

    const renderBasicInfo = () => (
        <StyledPaper elevation={3}>
            {!isEditing ? (
                <Box>
                    <Typography variant="h6" gutterBottom>기본 정보</Typography>
                    <Grid container spacing={2}>
                        <Grid item xs={12}>
                            <Typography><strong>이름:</strong> {user.name}</Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography><strong>닉네임:</strong> {user.nickname}</Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography><strong>이메일:</strong> {user.email}</Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography>
                                <strong>가입 유형:</strong> {user.provider === 'LOCAL' ? '일반 회원가입' : 'Google'}
                            </Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography><strong>생년월일:</strong> {user.birth}</Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Typography>
                                <strong>성별:</strong> {user.gender === 'MALE' ? '남성' : user.gender === 'FEMALE' ? '여성' : user.gender}
                            </Typography>
                        </Grid>
                        <Grid item xs={12}>
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleEditMode}
                            >
                                정보 수정
                            </Button>
                        </Grid>
                    </Grid>
                </Box>
            ) : (
                <form onSubmit={handleEdit}>
                    <Typography variant="h6" gutterBottom>정보 수정</Typography>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                        <TextField
                            fullWidth
                            label="닉네임"
                            value={editData.nickname}
                            onChange={(e) => updateEditData({ nickname: e.target.value })}
                            inputProps={{ maxLength: 20 }}
                        />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                type="date"
                                label="생년월일"
                                value={editData.birth}
                                onChange={(e) => updateEditData({ birth: e.target.value })}
                                InputLabelProps={{ shrink: true }}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <FormControl component="fieldset">
                                <FormLabel component="legend">성별</FormLabel>
                                <RadioGroup 
                                row 
                                value={editData.gender} 
                                onChange={(e) => updateEditData({ gender: e.target.value })}
                                >
                                <FormControlLabel value="MALE" control={<Radio />} label="남성" />
                                <FormControlLabel value="FEMALE" control={<Radio />} label="여성" />
                                </RadioGroup>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12}>
                            <Box display="flex" gap={2}>
                                <Button 
                                        type="submit" 
                                        variant="contained" 
                                        color="primary"
                                >
                                    저장
                                </Button>
                                <Button
                                    variant="contained"
                                    color="inherit"
                                    onClick={() => setEditMode(false)}
                                >
                                    취소
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                </form>
            )}
        </StyledPaper>
    );

    const renderPasswordChange = () => {
        if (user?.provider !== 'LOCAL') return null;

        return (
            <StyledPaper elevation={3}>
                <Typography variant="h6" gutterBottom>비밀번호 변경</Typography>
                {passwordError && (
                    <Alert severity="error" sx={{ mb: 2 }}>{passwordError}</Alert>
                )}
                <form onSubmit={handlePassword}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                type="password"
                                label="현재 비밀번호"
                                value={passwordData.currentPassword}
                                onChange={(e) => updatePasswordData({ currentPassword: e.target.value })}
                                required
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                type="password"
                                label="새 비밀번호"
                                value={passwordData.newPassword}
                                onChange={(e) => updatePasswordData({ newPassword: e.target.value })}
                                helperText="8~16자리의 영대소문자, 특수문자, 숫자 포함"
                                required
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                type="password"
                                label="새 비밀번호 확인"
                                value={passwordData.confirmPassword}
                                onChange={(e) => updatePasswordData({ confirmPassword: e.target.value })}
                                error={passwordData.newPassword !== passwordData.confirmPassword && passwordData.confirmPassword !== ''}
                                helperText={
                                    passwordData.newPassword !== passwordData.confirmPassword && passwordData.confirmPassword !== '' 
                                        ? "비밀번호가 일치하지 않습니다" 
                                        : ""
                                }
                                required
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Button 
                                type="submit" 
                                variant="contained" 
                                color="primary"
                                disabled={!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword}
                            >
                                비밀번호 변경
                            </Button>
                        </Grid>
                    </Grid>
                </form>
            </StyledPaper>
        );
    };

    const renderDeleteAccount = () => (
        <StyledPaper elevation={3}>
            <Typography variant="h6" gutterBottom>계정 삭제</Typography>
            <Typography color="error" paragraph>
                계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.
                {user.provider === 'LOCAL' 
                    ? " 계정 삭제를 위해 비밀번호를 입력해주세요." 
                    : " Google 계정 연동이 해제됩니다."}
            </Typography>

            {deleteError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {deleteError}
                </Alert>
            )}

            {user.provider === 'LOCAL' && (
                <Box mb={3}>
                    <TextField
                        fullWidth
                        type="password"
                        label="비밀번호"
                        value={deletePassword}
                        onChange={(e) => setDeletePassword(e.target.value)}
                        sx={{ mb: 2 }}
                    />
                </Box>
            )}

            <Button
                variant="contained"
                color="error"
                onClick={() => setDeleteDialogOpen(true)}
                disabled={user.provider === 'LOCAL' && !deletePassword}
            >
                계정 삭제
            </Button>
        </StyledPaper>
    );

    const renderNavigationItems = () => (
        <List component="nav">
            <ListItemButton 
                selected={currentSection === 'basic'}
                onClick={() => setCurrentSection('basic')}
            >
                <PersonIcon sx={{ mr: 2 }} />
                <ListItemText primary="기본 정보" />
            </ListItemButton>
            <Divider />
            
            {user?.provider === 'LOCAL' && (
                <>
                    <ListItemButton 
                        selected={currentSection === 'password'}
                        onClick={() => setCurrentSection('password')}
                    >
                        <LockIcon sx={{ mr: 2 }} />
                        <ListItemText primary="비밀번호 변경" />
                    </ListItemButton>
                    <Divider />
                </>
            )}
            
            <ListItemButton 
                selected={currentSection === 'delete'}
                onClick={() => setCurrentSection('delete')}
            >
                <PersonOffIcon sx={{ mr: 2 }} />
                <ListItemText primary="회원 탈퇴" />
            </ListItemButton>
            <Divider />
            <ListItemButton 
                onClick={() => navigate('/lounge/mypage')}
                sx={{ '&:hover': { backgroundColor: 'primary.light' } }}
            >
                <DynamicFeedIcon sx={{ mr: 2 }} />
                <ListItemText primary="피드 관리" />
                <ArrowForwardIcon color="action" />
            </ListItemButton>
            <Divider />
            <ListItemButton 
                onClick={() => navigate('/meetingmgmt')}
                sx={{ '&:hover': { backgroundColor: 'primary.light' } }}
            >
                <GroupsIcon sx={{ mr: 2 }} />
                <ListItemText primary="모임 관리" />
                <ArrowForwardIcon color="action" />
            </ListItemButton>
        </List>
    );

    if (!user) return <CircularProgress />;

    return (
        <PageContainer>
            <Navigation>
                {renderNavigationItems()}
            </Navigation>

            <ContentArea>
                {currentSection === 'basic' && renderBasicInfo()}
                {currentSection === 'password' && user.provider === 'LOCAL' && renderPasswordChange()}
                {currentSection === 'delete' && renderDeleteAccount()}
            </ContentArea>

            <Dialog
                open={deleteDialogOpen}
                onClose={() => {
                    setDeleteDialogOpen(false);
                    clearErrors();
                }}
            >
                <DialogTitle>계정 삭제 확인</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {user.provider === 'LOCAL' 
                            ? "정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다."
                            : "Google 계정 연동이 해제되고 계정이 삭제됩니다. 계속하시겠습니까?"}
                    </DialogContentText>
                    {deleteError && (
                        <Alert severity="error" sx={{ mt: 2 }}>
                            {deleteError}
                        </Alert>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => {
                        setDeleteDialogOpen(false);
                        clearErrors();
                    }} color="primary">
                        취소
                    </Button>
                    <Button onClick={handleDelete} color="error" variant="contained">
                        삭제
                    </Button>
                </DialogActions>
            </Dialog>
        </PageContainer>
    );
};

export default MyPage;