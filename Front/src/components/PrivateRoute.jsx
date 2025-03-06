import { useState, useEffect } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '../stores/auth/useAuthStore';

const PrivateRoute = () => {
  const { checkAuthStatus } = useAuthStore();
  const [isChecking, setIsChecking] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const verifyAuth = async () => {
      try {
        // 서버 API를 호출하여 인증 상태 확인
        const authStatus = await checkAuthStatus();
        setIsAuthenticated(authStatus);
      } catch (error) {
        setIsAuthenticated(false);
      } finally {
        setIsChecking(false);
      }
    };
    
    verifyAuth();
  }, []);
  
  // 인증 확인 중에는 로딩 표시
  if (isChecking) {
    return <div>Loading...</div>;
  }
  
  // 인증 확인 후 결과에 따라 렌더링
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export default PrivateRoute;