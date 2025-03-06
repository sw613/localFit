import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

function UpdateFeedPage() {
  const { feedId } = useParams();
  const navigate = useNavigate();
  const [description, setDescription] = useState('');

  useEffect(() => {
    if (!feedId) {
      alert('피드 ID가 제공되지 않았습니다.');
      return;
    }
    
    axios.get(`/api/lounge/feed/${feedId}`)
      .then((response) => {
        setDescription(response.data.description);
      })
      .catch((error) => {
        console.error('피드 불러오기 에러:', error);
        alert('피드 정보를 불러오지 못했습니다.');
      });
  }, [feedId]);

  const handleUpdate = async (e) => {
    e.preventDefault();

    try {
      const response = await axios.put(`/api/lounge/update-feed/${feedId}`, {
        description,
      });

      alert('수정되었습니다.');
      navigate('/');
    } catch (error) {
      console.error('수정 에러:', error);
      alert('수정 중 에러 발생');
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>피드 수정</h1>
      <form onSubmit={handleUpdate}>
        <label htmlFor="description">내용</label>
        <br />
        <textarea
          id="description"
          name="description"
          placeholder="피드 내용을 입력하세요"
          required
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ width: '300px', height: '100px' }}
        />
        <br />
        <br />
        <button type="submit">수정 완료</button>
      </form>
    </div>
  );
}

export default UpdateFeedPage;
