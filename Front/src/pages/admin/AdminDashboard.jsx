import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axios';
import useAuthStore from '../../stores/auth/useAuthStore';
import { Snackbar, Alert } from '@mui/material';

import { 
  Container, 
  Typography, 
  Box, 
  Tabs, 
  Tab, 
  Paper, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Button, 
  IconButton,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Pagination,
  CircularProgress
} from '@mui/material';

import DeleteIcon from '@mui/icons-material/Delete';
import RefreshIcon from '@mui/icons-material/Refresh';

function TabPanel(props) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`admin-tabpanel-${index}`}
      aria-labelledby={`admin-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const AdminDashboard = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [tabValue, setTabValue] = useState(0);
  const [users, setUsers] = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [feeds, setFeeds] = useState([]);
  
  const [meetingPage, setMeetingPage] = useState(0);
  const [meetingPageCount, setMeetingPageCount] = useState(0);
  const [meetingPageSize] = useState(6);
  
  const [userPage, setUserPage] = useState(0);
  const [userPageCount, setUserPageCount] = useState(0);
  const [userPageSize] = useState(10);
  
  const [feedPage, setFeedPage] = useState(0);
  const [feedPageCount, setFeedPageCount] = useState(0);
  const [feedPageSize] = useState(10);
  
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState({ type: '', id: null });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);

  
  const navigateToPage = (path) => {
    navigate(path);
  };

  
  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const [successMessage, setSuccessMessage] = useState("");
  const [openSnackbar, setOpenSnackbar] = useState(false);

  
  const loadUsers = async () => {
    try {
      setIsLoading(true);
      const response = await api.get(`/admin/users?page=${userPage}&size=${userPageSize}`);
      setUsers(response.data.content || response.data);
      setUserPageCount(response.data.totalPages || 1);
      setError(null);
    } catch (err) {
      setError('사용자 데이터를 불러오는데 실패했습니다');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  // Load meetings data
  const loadMeetings = async () => {
    try {
      setIsLoading(true);
      const response = await api.get(`/admin/meeting/listAll?page=${meetingPage}&size=${meetingPageSize}`);
      setMeetings(response.data.content || response.data);
      setMeetingPageCount(response.data.totalPages || 1);
      setError(null);
    } catch (err) {
      setError('미팅 데이터를 불러오는데 실패했습니다');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  // Load feeds data
  const loadFeeds = async () => {
    try {
      setIsLoading(true);
      const response = await api.get(`/admin/lounge/feeds?page=${feedPage}&size=${feedPageSize}`);
      setFeeds(response.data.content || response.data);
      setFeedPageCount(response.data.totalPages || 1);
      setError(null);
    } catch (err) {
      setError('피드 데이터를 불러오는데 실패했습니다');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  // Load data based on current tab
  useEffect(() => {
    switch (tabValue) {
      case 0:
        loadUsers();
        break;
      case 1:
        loadMeetings();
        break;
      case 2:
        loadFeeds();
        break;
      default:
        break;
    }
  }, [tabValue, userPage, meetingPage, feedPage]);

  // Handle delete confirmation dialog
  const openDeleteDialog = (type, id) => {
    setItemToDelete({ type, id });
    setDeleteDialogOpen(true);
  };

  const closeDeleteDialog = () => {
    setDeleteDialogOpen(false);
  };

  // Perform delete operation
  const handleDelete = async () => {
    try {
      setIsLoading(true);
      if (itemToDelete.type === 'user') {
        await api.delete(`/admin/users/${itemToDelete.id}`);
        setSuccessMessage('사용자가 성공적으로 탈퇴되었습니다.');
        loadUsers();
      } else if (itemToDelete.type === 'meeting') {
        await api.delete(`/admin/meeting/delete/${itemToDelete.id}`);
        setSuccessMessage('미팅이 성공적으로 삭제되었습니다.');
        loadMeetings();
      } else if (itemToDelete.type === 'feed') {
        await api.delete(`/admin/lounge/feeds/${itemToDelete.id}`);
        setSuccessMessage('피드가 성공적으로 삭제되었습니다.');
        loadFeeds();
      }
      setOpenSnackbar(true);
      setError(null);
    } catch (err) {
      setError(`${itemToDelete.type} 삭제에 실패했습니다`);
      console.error(err);
    } finally {
      setIsLoading(false);
      closeDeleteDialog();
    }
  };

  const handleCloseSnackbar = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenSnackbar(false);
  };

  // Handle meeting pagination
  const handleMeetingPageChange = (event, value) => {
    setMeetingPage(value - 1);
  };

  // Handle user pagination
  const handleUserPageChange = (event, value) => {
    setUserPage(value - 1);
  };

  // Handle feed pagination
  const handleFeedPageChange = (event, value) => {
    setFeedPage(value - 1);
  };

  // Users component
  const renderUsers = () => {
    return (
      <Box>
        <Typography variant="h5" gutterBottom>
          사용자 관리
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button 
            startIcon={<RefreshIcon />} 
            onClick={loadUsers}
            disabled={isLoading}
          >
            새로고침
          </Button>
        </Box>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>이메일</TableCell>
              <TableCell>이름</TableCell>
              <TableCell>닉네임</TableCell>
              <TableCell>가입유형</TableCell>
              <TableCell align="center">탈퇴</TableCell>
            </TableRow>
            </TableHead>
            <TableBody>
            {users.length > 0 ? (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.name}</TableCell>
                  <TableCell>{user.nickname}</TableCell>
                  <TableCell>{user.provider}</TableCell>
                  <TableCell align="center">
                    <IconButton 
                      color="error" 
                      onClick={() => openDeleteDialog('user', user.id)}
                      disabled={isLoading}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  {isLoading ? <CircularProgress size={24} /> : '사용자 데이터가 없습니다.'}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
        {userPageCount > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <Pagination 
              count={userPageCount} 
              page={userPage + 1} 
              onChange={handleUserPageChange} 
              color="primary" 
              disabled={isLoading}
            />
          </Box>
        )}
      </Box>
    );
  };

  // Meetings component
  const renderMeetings = () => {
    return (
      <Box>
        <Typography variant="h5" gutterBottom>
          모임 관리
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button 
            startIcon={<RefreshIcon />} 
            onClick={loadMeetings}
            disabled={isLoading}
          >
            새로고침
          </Button>
        </Box>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>제목</TableCell>
                <TableCell>날짜</TableCell>
                <TableCell>참가자 수</TableCell>
                <TableCell>삭제</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {meetings.length > 0 ? (
                meetings.map((meeting) => (
                  <TableRow key={meeting.meetingId}>
                    <TableCell>{meeting.meetingId}</TableCell>
                    <TableCell>{meeting.meetingTitle}</TableCell>
                    <TableCell>{"매주 "}{meeting.meetingWeek}{" "}{meeting.meetingTime.slice(0, 5)}{"분"}</TableCell>
                    <TableCell>{meeting.numberPeopleCur}{" / "}{meeting.numberPeopleMax}</TableCell>
                    <TableCell>
                      <IconButton 
                        edge="end" 
                        color="error" 
                        onClick={() => openDeleteDialog('meeting', meeting.meetingId)}
                        disabled={isLoading}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    {isLoading ? <CircularProgress size={24} /> : '미팅 데이터가 없습니다.'}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        {meetingPageCount > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <Pagination 
              count={meetingPageCount} 
              page={meetingPage + 1} 
              onChange={handleMeetingPageChange} 
              color="primary" 
              disabled={isLoading}
            />
          </Box>
        )}
      </Box>
    );
  };

  // Feeds component
  const renderFeeds = () => {
    return (
      <Box>
        <Typography variant="h5" gutterBottom>
          피드 관리
        </Typography>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button 
            startIcon={<RefreshIcon />} 
            onClick={loadFeeds}
            disabled={isLoading}
          >
            새로고침
          </Button>
        </Box>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
              <TableCell>ID</TableCell>
                <TableCell>작성자</TableCell>
                <TableCell>내용</TableCell>
                <TableCell>작성일</TableCell>
                <TableCell>삭제</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {feeds.length > 0 ? (
                feeds.map((feed) => (
                  <TableRow key={feed.id}>
                    <TableCell>{feed.id}</TableCell>
                    <TableCell>{feed.userName || feed.userNickname}</TableCell>
                    <TableCell>
                      {feed.description && feed.description.length > 100
                        ? feed.description.substring(0, 100) + '...'
                        : feed.description || feed.title}
                    </TableCell>
                    <TableCell>{new Date(feed.createdAt || feed.createdDate).toLocaleString()}</TableCell>
                    <TableCell>
                      <IconButton 
                        edge="end" 
                        color="error" 
                        onClick={() => openDeleteDialog('feed', feed.id)}
                        disabled={isLoading}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    {isLoading ? <CircularProgress size={24} /> : '피드 데이터가 없습니다.'}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        {feedPageCount > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
            <Pagination 
              count={feedPageCount} 
              page={feedPage + 1} 
              onChange={handleFeedPageChange}
              color="primary" 
              disabled={isLoading}
            />
          </Box>
        )}
      </Box>
    );
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4" component="h1">
          관리자 페이지
        </Typography>
        <Button 
          variant="contained" 
          color="primary"
          onClick={async () => {
            await logout();
            navigate('/login');
          }}
        >
          로그아웃
        </Button>
      </Box>
      
      <Paper sx={{ p: 2 }}>
        {error && (
          <Box sx={{ mb: 2, p: 2, bgcolor: 'error.light', color: 'error.contrastText', borderRadius: 1 }}>
            <Typography>{error}</Typography>
          </Box>
        )}
        
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={handleTabChange} aria-label="admin tabs">
            <Tab label="사용자 관리" id="admin-tab-0" aria-controls="admin-tabpanel-0" />
            <Tab label="미팅 관리" id="admin-tab-1" aria-controls="admin-tabpanel-1" />
            <Tab label="피드 관리" id="admin-tab-2" aria-controls="admin-tabpanel-2" />
          </Tabs>
        </Box>
        
        <TabPanel value={tabValue} index={0}>
          {renderUsers()}
        </TabPanel>
        <TabPanel value={tabValue} index={1}>
          {renderMeetings()}
        </TabPanel>
        <TabPanel value={tabValue} index={2}>
          {renderFeeds()}
        </TabPanel>
      </Paper>
      
      {/* Delete confirmation dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={closeDeleteDialog}
      >
        <DialogTitle>삭제 확인</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {itemToDelete.type === 'user' && '이 사용자를 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.'}
            {itemToDelete.type === 'meeting' && '이 미팅을 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.'}
            {itemToDelete.type === 'feed' && '이 피드를 정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.'}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDeleteDialog} color="primary">
            취소
          </Button>
          <Button onClick={handleDelete} color="error" disabled={isLoading}>
            삭제
          </Button>
        </DialogActions>
      </Dialog>
      <Snackbar 
        open={openSnackbar} 
        autoHideDuration={3000} 
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity="success" sx={{ width: '100%' }}>
          {successMessage}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default AdminDashboard;