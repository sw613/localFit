import React, { useState } from 'react';
import { Navigate, Outlet, useLocation, Link } from 'react-router-dom';
import useAuthStore from '../../stores/auth/useAuthStore';
import { AppBar, Toolbar, Typography, Button, IconButton, Avatar, Menu, MenuItem } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';

const AdminRoute = () => {
    const location = useLocation();
    const { user, isLoggedIn } = useAuthStore();
    const [anchorEl, setAnchorEl] = useState(null);

    console.log("AdminRoute - 사용자:", user);
    console.log("AdminRoute - 역할:", user?.role);
    
    // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
    if (!isLoggedIn) {
      return <Navigate to="/login" state={{ from: location.pathname }} replace />;
    }
    
    // 로그인했지만 관리자가 아닌 경우 홈으로 리다이렉트
    const userRole = user?.role;
    const isAdmin = 
        userRole === 'ADMIN' || 
        userRole === 'ROLE_ADMIN' ||
        (typeof userRole === 'object' && (userRole.value === 'ADMIN' || userRole.name === 'ADMIN')) ||
        String(userRole).includes('ADMIN');
    
    console.log("AdminRoute - 관리자 여부:", isAdmin);
    
    // 로그인했지만 관리자가 아닌 경우 홈으로 리다이렉트
    if (!isAdmin) {
        return <Navigate to="/" replace />;
    }

    // 유저 메뉴 열고 닫기
    const handleUserMenuOpen = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleUserMenuClose = () => {
        setAnchorEl(null);
    };
    
    // 로그아웃 처리
    const onLogout = () => {
        handleLogout();
        handleUserMenuClose();
    };


    return (
        <div className="admin-layout">
            {/* 기존 헤더와 푸터를 숨기는 스타일 */}
            <style>
                {`
                    header, footer {
                        display: none !important;
                    }
                    .admin-layout {
                        min-height: 100vh;
                        display: flex;
                        flex-direction: column;
                    }
                    .admin-content {
                        flex: 1;
                        padding-top: 64px; /* AppBar 높이만큼 패딩 추가 */
                    }
                `}
            </style>
            {/* 메인 컨텐츠 영역 */}
            <div className="admin-content">
                <Outlet />
            </div>
        </div>
    );
};

export default AdminRoute;