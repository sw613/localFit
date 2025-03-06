import React, { useState, useEffect } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  Typography,
  Box
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import api from '../../api/axios';

const AdminPage = () => {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  const fetchUsers = async () => {
    try {
      const response = await api.get('/admin/users');
      setUsers(response.data);
      setError(null);
    } catch (err) {
      setError('사용자 목록을 불러오는데 실패했습니다.');
      console.error('Error fetching users:', err);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDeleteClick = (user) => {
    setSelectedUser(user);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      await api.delete(`/admin/users/${selectedUser.id}`);
      setUsers(users.filter(user => user.id !== selectedUser.id));
      setError(null);
      setDeleteDialogOpen(false);
    } catch (err) {
      setError('사용자 삭제에 실패했습니다.');
      console.error('Error deleting user:', err);
    }
  };

  return (
    <Box sx={{ maxWidth: 1200, margin: '0 auto', p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        사용자 관리
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

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
            {users.map((user) => (
              <TableRow key={user.id}>
                <TableCell>{user.id}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>{user.name}</TableCell>
                <TableCell>{user.nickname}</TableCell>
                <TableCell>{user.provider}</TableCell>
                <TableCell align="center">
                  <IconButton
                    color="error"
                    onClick={() => handleDeleteClick(user)}
                  >
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>사용자 삭제</DialogTitle>
        <DialogContent>
          <Typography>
            {selectedUser?.email} 사용자를 삭제하시겠습니까?
          </Typography>
          <Typography color="error" sx={{ mt: 1 }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            취소
          </Button>
          <Button
            onClick={handleDeleteConfirm}
            color="error"
            variant="contained"
          >
            삭제
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminPage;