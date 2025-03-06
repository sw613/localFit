import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/auth/useAuthStore';
import '../../styles/common/Header.css';

function Header() {
  const { isLoggedIn, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <header className="site-header">
      <div className="header-container">
        <div className="left-section">
          <div className="logo">
            <Link to="/">
              <img
                src="https://localfitbucket.s3.ap-northeast-2.amazonaws.com/_LocalFit.png"
                alt="Logo"
              />
            </Link>
          </div>
          <nav className="header-nav">
            <ul>
              <li><Link to="/meeting">시설</Link></li>
              <li><Link to="/lounge">라운지</Link></li>
              <li><Link to="/facilitymap">운동맵</Link></li>
              <li><Link to="/chatrooms">모임톡</Link></li>
            </ul>
          </nav>
        </div>

        <div className="right-section">
          <ul>
            {isLoggedIn ? (
              <>
                <li><Link to="/mypage">마이페이지</Link></li>
                <li>
                  <button onClick={handleLogout} className="logout-button">
                    로그아웃
                  </button>
                </li>
              </>
            ) : (
              <>
                <li><Link to="/login">로그인</Link></li>
                <li><Link to="/join">회원가입</Link></li>
              </>
            )}
          </ul>
        </div>
      </div>
    </header>
  );
}

export default Header;
