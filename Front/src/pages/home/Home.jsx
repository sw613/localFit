import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/auth/useAuthStore.js';
import { homeData } from './Home.data.js';
import './Home.css';

function Home() {
    const navigate = useNavigate();
    const { isLoggedIn, logout } = useAuthStore();
 
    const handleLogin = () => {
        navigate('/login');
    };
 
    const handleJoin = () => {
        navigate('/join');
    };

    const handleMyPage = () => {
        navigate('/mypage');
    };

    const handleLogout = async () => {
        await logout();
    };

    return (
        <div className="home-container">
            <h1>LocalFit</h1>
            <p>메인 페이지</p>
            <div>
            {isLoggedIn ? (
                    <div className="auth-buttons">
                        <button onClick={handleMyPage}>마이페이지</button>
                        <button onClick={handleLogout}>로그아웃</button>
                    </div>
                ) : (
                    <div className="auth-buttons">
                        <button onClick={handleLogin}>로그인</button>
                        <button onClick={handleJoin}>회원가입</button>
                    </div>
                )}
            </div>
        </div>
    );
 }

export default Home;