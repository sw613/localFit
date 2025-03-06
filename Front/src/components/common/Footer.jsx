// src/components/common/Footer.jsx
import React from 'react';
import '../../styles/common/Footer.css';

function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-container">
        <div className="footer-info">
          <div className="footer-section company-info">
            <h4>회사 정보</h4>
            <p>대표: 홍길동</p>
            <p>사업자등록번호: 123-45-67890</p>
            <p>주소: 서울특별시 강남구 테헤란로 123, 10층</p>
          </div>
          <div className="footer-section contact-info">
            <h4>고객 지원</h4>
            <p>이메일: support@localfit.com</p>
            <p>대표번호: 02-1234-5678</p>
            <p>고객센터: 02-8765-4321</p>
            <p>운영시간: 평일 09:00 ~ 18:00</p>
          </div>
          <div className="footer-section misc-info">
            <h4>기타 정보</h4>
            <p>개인정보 처리방침</p>
            <p>이용 약관</p>
            <p>© 2025 LocalFit. All rights reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
