import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { FaCamera, FaTimes } from 'react-icons/fa';
import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import '../../styles/lounge/EditFeedPage.css';

function EditFeedPage() {
  const { feedId } = useParams();
  const navigate = useNavigate();

  const [description, setDescription] = useState('');
  const [hashtags, setHashtags] = useState([]);
  const [hashtagInput, setHashtagInput] = useState('');
  const [imageList, setImageList] = useState([]);

  useEffect(() => {
    axios
      .get(`/api/lounge/feed/${feedId}`)
      .then((res) => {
        const data = res.data;
        setDescription(data.description || '');
        setHashtags(data.hashtags || []);
        const existingImages = (data.images || []).map((url) => ({
          type: 'existing',
          url,
        }));
        setImageList(existingImages);
      })
      .catch((err) => {
        console.error('피드 불러오기 에러:', err);
        alert('피드 데이터를 불러오지 못했습니다.');
      });
  }, [feedId]);

  const handleFileChange = (e) => {
    const selectedFiles = e.target.files;
    const newImages = [];
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      const preview = URL.createObjectURL(file);
      newImages.push({ type: 'new', file, preview });
    }
    setImageList((prev) => [...prev, ...newImages]);
  };

  const removeImage = (index) => {
    setImageList((prev) => prev.filter((_, i) => i !== index));
  };

  const handleHashtagKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const trimmed = hashtagInput.trim();
      if (trimmed && !hashtags.includes(trimmed)) {
        const tag = trimmed.startsWith('#') ? trimmed : '#' + trimmed;
        setHashtags([...hashtags, tag]);
        setHashtagInput('');
      }
    }
  };

  const removeHashtag = (indexToRemove) => {
    setHashtags((prev) => prev.filter((_, idx) => idx !== indexToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const feedData = {
        description,
        hashtags,
        existingImages: imageList
          .filter((img) => img.type === 'existing')
          .map((img) => img.url),
      };

      const formData = new FormData();
      formData.append(
        'feedData',
        new Blob([JSON.stringify(feedData)], { type: 'application/json' })
      );
      imageList
        .filter((img) => img.type === 'new')
        .forEach((img) => formData.append('images', img.file));

      await axios.post(`/api/lounge/update-feed/${feedId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      alert('피드가 수정되었습니다!');
      navigate(`/lounge/feed/${feedId}`);
    } catch (error) {
      console.error('피드 수정 에러:', error);
      alert('피드 수정 중 에러가 발생했습니다.');
    }
  };

  return (
    <div className="edit-feed">
      <h1>피드 수정</h1>
      <form onSubmit={handleSubmit} className="edit-feed__form">
        <div className="edit-feed__file-input-container">
          <label htmlFor="fileInput" className="edit-feed__file-input-label">
            <FaCamera className="edit-feed__camera-icon" />
            <span>사진 추가하기</span>
          </label>
          <input
            id="fileInput"
            type="file"
            multiple
            onChange={handleFileChange}
            className="edit-feed__file-input"
          />
        </div>

        <div className="edit-feed__image-list">
          {imageList.map((img, idx) => (
            <div key={idx} className="edit-feed__image-item">
              <img
                src={img.type === 'existing' ? img.url : img.preview}
                alt="피드 이미지"
                className="edit-feed__image"
              />
              <button
                type="button"
                className="edit-feed__image-remove"
                onClick={() => removeImage(idx)}
                title="이미지 삭제"
              >
                <FaTimes />
              </button>
            </div>
          ))}
        </div>

        <div className="edit-feed__description-container">
          <label className="edit-feed__description-label">내용</label>
          <ReactQuill
            theme="snow"
            value={description}
            onChange={(content) => setDescription(content)}
            className="edit-feed__description-editor"
          />
        </div>

        <div className="edit-feed__hashtag-container">
          <label htmlFor="hashtagInput" className="edit-feed__hashtag-label">
            해시태그 (엔터 또는 콤마로 구분)
          </label>
          <input
            id="hashtagInput"
            type="text"
            placeholder="#태그"
            value={hashtagInput}
            onChange={(e) => setHashtagInput(e.target.value)}
            onKeyDown={handleHashtagKeyDown}
            className="edit-feed__hashtag-input"
          />
          <div className="edit-feed__hashtag-list">
            {hashtags.map((tag, idx) => (
              <div key={idx} className="edit-feed__hashtag-chip">
                {tag}
                <span
                  className="edit-feed__remove-hashtag"
                  onClick={() => removeHashtag(idx)}
                >
                  &times;
                </span>
              </div>
            ))}
          </div>
        </div>

        <button type="submit" className="edit-feed__submit-button">
          수정 완료
        </button>
      </form>
    </div>
  );
}

export default EditFeedPage;
