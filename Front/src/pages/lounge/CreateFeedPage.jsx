import React, { useState, useEffect } from 'react';
import { FaCamera } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import ImageItem from '../../components/lounge/ImageItem';
import '../../styles/lounge/CreateFeedPage.css';


import 'react-quill/dist/quill.snow.css';
import ReactQuill from 'react-quill';   

function CreateFeedPage() {
  const navigate = useNavigate();
  const [description, setDescription] = useState('');
  const [files, setFiles] = useState([]);
  const [previewURLs, setPreviewURLs] = useState([]);
  const [hashtags, setHashtags] = useState([]); // 선택한 해시태그 리스트
  const [hashtagInput, setHashtagInput] = useState(''); // 해시태그 입력
  const [suggestions, setSuggestions] = useState([]);  // 자동 완성 리스트

  useEffect(() => { // 해시태그 입력 딜레이 
    if (hashtagInput.trim().length > 0) {
      const delayDebounce = setTimeout(() => {
        fetchHashtagSuggestions(hashtagInput);
      }, 300); 

      return () => clearTimeout(delayDebounce);
    } else {
      setSuggestions([]);
    }
  }, [hashtagInput]);

  const apiClient = axios.create({
    baseURL: "http://localhost:8080/api",
  });
  
  apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem("token"); 
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  const fetchHashtagSuggestions = async (query) => { // 자동완성 쿼리 날리기 
    try {
      const response = await axios.get(`/api/v1/search/hashtag/auto-complete?query=${query}`);
      setSuggestions(response.data);
    } catch (error) {
      console.error("해시태그 자동완성 요청 실패:", error);
    }
  };

  const addHashtag = () => { // 해시태그 추가
    const trimmed = hashtagInput.trim();
    if (trimmed && !hashtags.includes(trimmed)) { // #가 앞에 안붙었다면 # 붙여줌
      const formattedTag = trimmed.startsWith("#") ? trimmed : `#${trimmed}`;
      setHashtags([...hashtags, formattedTag]);
      setHashtagInput("");
      setSuggestions([]);
    }
  };

  const handleSuggestionClick = (suggestedTag) => { // 자동 완성 추천 선택
    setHashtagInput(suggestedTag.Hashtag); 
  };

  const handleFileChange = (e) => {
    const selectedFiles = e.target.files;
    setFiles(selectedFiles);

    const newPreviewURLs = [];
    for (let i = 0; i < selectedFiles.length; i++) {
      const url = URL.createObjectURL(selectedFiles[i]);
      newPreviewURLs.push(url);
    }
    setPreviewURLs(newPreviewURLs);
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

  const removeHashtag = (indexToRemove) => { // 해시태그 삭제
    setHashtags(hashtags.filter((_, idx) => idx !== indexToRemove));
  };

  // 이미지 드래그 앤 드롭 정렬
  const moveImage = (fromIndex, toIndex) => {
    setPreviewURLs((prev) => {
      const updated = [...prev];
      const [movedItem] = updated.splice(fromIndex, 1);
      updated.splice(toIndex, 0, movedItem);
      return updated;
    });
    setFiles((prev) => {
      const arr = Array.from(prev);
      const [movedFile] = arr.splice(fromIndex, 1);
      arr.splice(toIndex, 0, movedFile);
      return arr;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const existingImages = previewURLs.filter(url => url.startsWith("http"));
  
      const feedData = {
        description,
        hashtags,
        existingImages 
      };
  
      const formData = new FormData();
      formData.append(
        'feedData',
        new Blob([JSON.stringify(feedData)], { type: 'application/json' })
      );

      Array.from(files).forEach((file) => {
        if (file instanceof File) {
          formData.append('images', file);
        }
      });
  
      const response = await axios.post('/api/lounge/create-feed', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
  
      console.log('생성된 피드:', response.data);
      alert('피드가 생성되었습니다!');
      navigate('/lounge');
    } catch (error) {
      console.error('에러 발생:', error);
      alert('피드 생성 중 에러가 발생했습니다.');
    }
  };
  

  return (
    <div className="create-feed">
      <h1>피드 생성</h1>
      <form onSubmit={handleSubmit} className="create-feed__form">
        <div className="create-feed__file-input-container">
          <label htmlFor="fileInput" className="create-feed__file-input-label">
            <FaCamera className="create-feed__camera-icon" />
            <span className="create-feed__file-input-text">사진 첨부하기</span>
          </label>
          <input
            id="fileInput"
            type="file"
            multiple
            onChange={(e) => {
              const selectedFiles = e.target.files;
              setFiles(selectedFiles);

              const newPreviewURLs = [];
              for (let i = 0; i < selectedFiles.length; i++) {
                const url = URL.createObjectURL(selectedFiles[i]);
                newPreviewURLs.push(url);
              }
              setPreviewURLs(newPreviewURLs);
            }}
            className="create-feed__file-input"
          />
        </div>

        <DndProvider backend={HTML5Backend}>
          <div className="create-feed__image-list">
            {previewURLs.map((url, idx) => (
              <ImageItem
                key={url}
                url={url}
                index={idx}
                moveImage={(fromIndex, toIndex) => {
                  setPreviewURLs((prev) => {
                    const updated = [...prev];
                    const [movedItem] = updated.splice(fromIndex, 1);
                    updated.splice(toIndex, 0, movedItem);
                    return updated;
                  });
                  setFiles((prev) => {
                    const arr = Array.from(prev);
                    const [movedFile] = arr.splice(fromIndex, 1);
                    arr.splice(toIndex, 0, movedFile);
                    return arr;
                  });
                }}
              />
            ))}
          </div>
        </DndProvider>

        {/* <textarea> 대신 ReactQuill 사용 */}
        <div className="create-feed__description-container">
          <label htmlFor="description" className="create-feed__description-label">
            내용
          </label>
          <ReactQuill
            theme="snow"
            value={description}
            onChange={(content) => setDescription(content)}
            className="create-feed__description-editor" // 필요시 CSS 클래스로 스타일링
          />
        </div>

        <div className="hashtag-container">
          <label htmlFor="hashtagInput" className="hashtag-label">
            해시태그 검색 후 추가
          </label>
          <div className="hashtag-input-wrapper">
            <input
              id="hashtagInput"
              type="text"
              placeholder="#태그"
              value={hashtagInput}
              onChange={(e) => setHashtagInput(e.target.value)}
              className="hashtag-input"
            />
            <button type="button" onClick={addHashtag} className="add-hashtag-button">
              추가
            </button>
          </div>

          {suggestions.length > 0 && (
            <ul className="autocomplete-list">
              {suggestions.map((tag) => (
                <li key={tag.HashtagId} onClick={() => handleSuggestionClick(tag)}>
                  {tag.Hashtag}
                </li>
              ))}
            </ul>
          )}

          <div className="hashtag-list">
            {hashtags.map((tag, idx) => (
              <div key={idx} className="hashtag-chip">
                {tag}
                <span className="remove-hashtag" onClick={() => removeHashtag(idx)}>
                  &times;
                </span>
              </div>
            ))}
          </div>
        </div>


        <button type="submit" className="create-feed__submit-button">
          작성하기
        </button>
      </form>
    </div>
  );
}

export default CreateFeedPage;