import React, { useEffect, useState } from 'react';
import api from '../../api/axios';
import '../../styles/lounge/AdminFeedPage.css';

function AdminFeedPage() {
  const [feeds, setFeeds] = useState([]);

  const fetchFeeds = async () => {
    try {
      const response = await axios.get('/admin/lounge/feeds');
      console.log("응답 데이터:", response.data);
      setFeeds(response.data);
    } catch (error) {
      console.error("피드 목록을 불러오는 중 오류 발생:", error);
      alert("피드 목록을 불러오지 못했습니다.");
    }
  };

  useEffect(() => {
    fetchFeeds();
  }, []);

  const handleDelete = async (feedId) => {
    if (window.confirm("정말 삭제하시겠습니까?")) {
      try {
        await axios.delete(`/admin/lounge/feeds/${feedId}`);
        alert("피드가 삭제되었습니다.");
        fetchFeeds(); 
      } catch (error) {
        console.error("피드 삭제 중 오류 발생:", error);
        alert("삭제 중 오류가 발생했습니다.");
      }
    }
  };

  return (
    <div className="admin-feed-page">
      <h1>관리자 - 피드 목록</h1>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>작성자</th>
            <th>내용</th>
            <th>작성일</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {feeds && feeds.length > 0 ? (
            feeds.map((feed) => (
              <tr key={feed.id}>
                <td>{feed.id}</td>
                <td>{feed.userNickname}</td>
                <td>
                  {feed.description && feed.description.length > 100
                    ? feed.description.substring(0, 100) + '...'
                    : feed.description}
                </td>
                <td>{new Date(feed.createdDate).toLocaleString()}</td>
                <td>
                  <button onClick={() => handleDelete(feed.id)}>삭제</button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="5">피드가 없습니다.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default AdminFeedPage;
