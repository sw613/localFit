import { useEffect, Suspense } from 'react';
import { Routes, Route, useLocation, useNavigate } from 'react-router-dom';
import useAuthStore from './stores/auth/useAuthStore';
import ErrorBoundary from './components/ErrorBoundary';
import PrivateRoute from './components/PrivateRoute';

import AdminRoute from './components/admin/AdminRoute';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminFeedPage from './pages/admin/AdminFeedPage';

import OAuth2RedirectHandler from './components/auth/OAuth2RedirectHandler';
import AdditionalInfoPage from './pages/auth/AdditionalInfoPage';
import JoinPage from './pages/auth/JoinPage';
import LoginPage from './pages/auth/LoginPage';
import MyPage from './pages/user/MyPage';

import LoungePage from './pages/lounge/LoungePage';
import CreateFeedPage from './pages/lounge/CreateFeedPage';
import FeedDetailPage from './pages/lounge/FeedDetailPage';
import MyLoungePage from './pages/lounge/MyLoungePage';
import EditFeedPage from './pages/lounge/EditFeedPage';


import ChatPage from './pages/comm/ChatPage';
import Chatrooms from './pages/comm/Chatrooms';

import MeetingPage from "./pages/meeting/MeetingPage";
import CreateMeetingPage from "./pages/meeting/CreateMeetingPage";
import DetailFacilityPage from "./pages/meeting/DetailFacilityPage";
import DetailMeetingPage from "./pages/meeting/DetailMeetingPage";
import MeetingMgmtPage from "./pages/meeting/MeetingMgmtPage";
import UpdateMeetingPage from "./pages/meeting/UpdateMeetingPage";
import Main from "./pages/home/Main";
import AdminMeetingPage from "./pages/admin/AdminMeetingPage";



import Header from './components/common/Header';
import Footer from './components/common/Footer';

import FacilityMap from './pages/place/FacilityMap';

function App() {
  const { checkAuthStatus } = useAuthStore()
  const location = useLocation();
  const navigate = useNavigate();

  // 앱 초기 로드 시 무조건 한 번 인증 상태 확인
  useEffect(() => {
    const initAuth = async () => {
      try {
        await checkAuthStatus();
      } catch (error) {
        console.error('Initial auth check failed:', error);
      }
    };
    
    initAuth();
  }, []); // 빈 의존성 배열로 앱 마운트 시 한 번만 실행됨

  // 라우팅 변경 시 인증 로직
  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // 인증 확인을 건너뛸 경로 목록 (로그인, 회원가입 등)
        const skipAuthCheckPaths = ['/login', '/join', '/oauth/callback', `facilitymap`];
        
        // 현재 경로가 인증 확인을 건너뛸 경로인지 확인
        const shouldSkipAuthCheck = skipAuthCheckPaths.includes(location.pathname);
        
        // 로그인/회원가입 페이지에서 이미 로그인된 사용자 처리
        if ((location.pathname === '/login' || location.pathname === '/join')) {
          const isAuthenticated = await checkAuthStatus();
          if (isAuthenticated) {
            navigate('/', { replace: true });
          }
          return;
        }
        
        // 그 외 모든 경로에서는 인증 상태 확인
        if (!shouldSkipAuthCheck) {
          await checkAuthStatus();
        }
      } catch (error) {
        console.error('Authentication check failed:', error);
      }
    };
    
    initializeAuth();
  }, [location.pathname, checkAuthStatus, navigate]);
  
  return (
    <ErrorBoundary>
      <div className="app-container">
      <Header />
        <main>
          <Suspense fallback={<div>Loading...</div>}>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Main />} />
              <Route path="/join" element={<JoinPage />} />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/additional-info" element={<AdditionalInfoPage />} />
              <Route path="/oauth/callback" element={<OAuth2RedirectHandler />} />

              <Route path="/lounge/create" element={<CreateFeedPage />} />
              <Route path="/lounge/mypage" element={<MyLoungePage />} />
              <Route path="/lounge/user/:userId" element={<MyLoungePage />} />
              <Route path="/lounge/feed/:feedId" element={<FeedDetailPage />} />
              <Route path="/lounge/edit-feed/:feedId" element={<EditFeedPage />} />
              <Route path="/admin/lounge/feeds" element={<AdminFeedPage />} />

              <Route path="/meeting" element={<MeetingPage />} />
              <Route path="/facility/:facilityId" element={<DetailFacilityPage />} />
              <Route path="/meeting/:meetingId" element={<DetailMeetingPage />} />
              <Route path="/meetingmgmt" element={<MeetingMgmtPage />} />
              <Route path="/updatemeeting" element={<UpdateMeetingPage />} />

              <Route path="/facilitymap" element={<FacilityMap />} />
              <Route path="/chatrooms" element={<Chatrooms />} />


              {/* Admin Routes - 관리자 전용 라우트들 */}
              <Route element={<AdminRoute />}>
                <Route path="/admin/dashboard" element={<AdminDashboard />} />
              </Route>

              {/* Protected Routes */}
              <Route element={<PrivateRoute />}>
                <Route path="/mypage" element={<MyPage />} />
                <Route path="/chat/:roomId" element={<ChatPage />} />
                
                <Route path="/createmeeting" element={<CreateMeetingPage />} />
                <Route path="/meetingmgmt" element={<MeetingMgmtPage />} />
                <Route path="/updatemeeting" element={<UpdateMeetingPage />} />
                <Route path="/lounge" element={<LoungePage />} />

                <Route path="/admin/meeting" element={<AdminMeetingPage />} />
              </Route>
            </Routes>
          </Suspense>
        </main>
        <div style={{ height: "150px" }}></div>
      <Footer />
      </div>
    </ErrorBoundary>
  );      
}

export default App;
