import React, { useEffect, useState, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { FaHeart, FaRegHeart } from "react-icons/fa";
import { MdAddCircleOutline } from "react-icons/md"; 
import "../../styles/lounge/LoungePage.css";

import useAuthStore from '../../stores/auth/useAuthStore';

function LoungePage() {
  const navigate = useNavigate();
  
  const [activeTab, setActiveTab] = useState("popular");
  
  const [popularFeeds, setPopularFeeds] = useState([]);
  const [recentFeeds, setRecentFeeds] = useState([]);
  
  const [popPage, setPopPage] = useState(0);
  const [recentPage, setRecentPage] = useState(0);
  const [popLoading, setPopLoading] = useState(false);
  const [recentLoading, setRecentLoading] = useState(false);
  const [popHasMore, setPopHasMore] = useState(true);
  const [recentHasMore, setRecentHasMore] = useState(true);

  const popObserver = useRef(null);
  const recentObserver = useRef(null);

  const { isLoggedIn, checkAuthStatus } = useAuthStore();

  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  const lastPopRef = useCallback((node) => {
    if (popLoading) return;
    if (popObserver.current) popObserver.current.disconnect();
    popObserver.current = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && popHasMore) {
          setPopPage((prev) => prev + 1);
        }
      },
      { threshold: 1.0 }
    );
    if (node) popObserver.current.observe(node);
  }, [popLoading, popHasMore]);

  const lastRecentRef = useCallback((node) => {
    if (recentLoading) return;
    if (recentObserver.current) recentObserver.current.disconnect();
    recentObserver.current = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && recentHasMore) {
          setRecentPage((prev) => prev + 1);
        }
      },
      { threshold: 1.0 }
    );
    if (node) recentObserver.current.observe(node);
  }, [recentLoading, recentHasMore]);

  // 인기 피드 로드 함수
  const loadPopularFeeds = async (page) => {
    setPopLoading(true);
    try {
      const res = await axios.get(
        `/api/lounge/list?page=${page}&size=8&sortType=HEART`
      );
      const data = res.data;
      setPopularFeeds((prev) => [...prev, ...data]);
      setPopHasMore(data.length > 0);
    } catch (err) {
      console.error("인기 피드 로드 에러:", err);
    } finally {
      setPopLoading(false);
    }
  };

  // 모임후기 피드 로드 함수
  const loadRecentFeeds = async (page) => {
    setRecentLoading(true);
    try {
      const res = await axios.get(
        `/api/lounge/list?page=${page}&size=8&sortType=CREATED_DATE`
      );
      const data = res.data;
      setRecentFeeds((prev) => [...prev, ...data]);
      setRecentHasMore(data.length > 0);
    } catch (err) {
      console.error("모임후기 로드 에러:", err);
    } finally {
      setRecentLoading(false);
    }
  };

  // 중복 로딩 방지
  const popLoadedPages = useRef(new Set());
  const recentLoadedPages = useRef(new Set());

  useEffect(() => {
    if (popLoadedPages.current.has(popPage)) return;
    popLoadedPages.current.add(popPage);
    loadPopularFeeds(popPage);
  }, [popPage]);

  useEffect(() => {
    if (recentLoadedPages.current.has(recentPage)) return;
    recentLoadedPages.current.add(recentPage);
    loadRecentFeeds(recentPage);
  }, [recentPage]);

  // 탭 전환
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  // 피드 생성 아이콘 클릭
  const handleCreateFeed = () => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    navigate("/lounge/create");
  };

  // 좋아요 토글
  const handleHeartToggle = async (e, feedId, isPopular) => {
    e.stopPropagation();
    //const isLoggedIn = popularFeeds[0]?.loggedIn || recentFeeds[0]?.loggedIn || false;
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    try {
      const res = await axios.post(`/api/lounge/feed/${feedId}/heart/toggle`);
      const updatedFeed = res.data;

      if (isPopular) {
        setPopularFeeds((prev) =>
          prev.map((f) => (f.id === feedId ? updatedFeed : f))
        );
      } else {
        setRecentFeeds((prev) =>
          prev.map((f) => (f.id === feedId ? updatedFeed : f))
        );
      }
    } catch (err) {
      console.error("좋아요 토글 에러:", err);
      alert("좋아요 처리 중 에러 발생");
    }
  };

  const slideStyle = {
    transform: activeTab === "popular" ? "translateX(0%)" : "translateX(-50%)",
  };

  return (
    <div className="lounge-page">
      {/* 피드 생성 아이콘 */}
      <div className="lounge-page__create-icon" onClick={handleCreateFeed}>
        <MdAddCircleOutline />
      </div>

      {/* 탭 버튼 영역 */}
      <div className="lounge-page__tabs">
        <div
          className={`lounge-page__tab ${activeTab === "popular" ? "active" : ""}`}
          onClick={() => handleTabChange("popular")}
        >
          인기피드
        </div>
        <div
          className={`lounge-page__tab ${activeTab === "recent" ? "active" : ""}`}
          onClick={() => handleTabChange("recent")}
        >
          최신피드
        </div>
      </div>

      {/* 슬라이드 컨테이너 */}
      <div className="lounge-page__slider-container">
        <div className="lounge-page__slider-wrapper" style={slideStyle}>
          {/* 인기피드 목록 */}
          <div className="lounge-page__grid-container">
            <div className="lounge-page__grid">
              {popularFeeds.length > 0 ? (
                popularFeeds.map((feed, index) => {
                  const { id, thumbnail, description, liked, heartCount } = feed;
                  const isLast = index === popularFeeds.length - 1;
                  return (
                    <div
                      key={id}
                      ref={isLast ? lastPopRef : null}
                      className="lounge-page__card"
                      onClick={() => navigate(`/lounge/feed/${id}`)}
                    >
                      <div className="lounge-page__image-container">
                        <img
                          src={thumbnail || ""}
                          alt="썸네일"
                          className="lounge-page__thumbnail"
                        />
                        <div
                          className="lounge-page__heart"
                          onClick={(e) => handleHeartToggle(e, id, true)}
                        >
                          {liked ? (
                            <FaHeart className="lounge-page__heart-icon lounge-page__heart-icon--liked" />
                          ) : (
                            <FaRegHeart className="lounge-page__heart-icon" />
                          )}
                          <span className="lounge-page__heart-count">{heartCount}</span>
                        </div>
                      </div>
                      <div
                          className="lounge-page__content"
                          dangerouslySetInnerHTML={{ __html: description }}
                        />
                      </div>
                  );
                })
              ) : (
                <p>피드가 없습니다.</p>
              )}
              {popLoading && <p>로드 중...</p>}
            </div>
          </div>

          {/* 모임후기 목록 */}
          <div className="lounge-page__grid-container">
            <div className="lounge-page__grid">
              {recentFeeds.length > 0 ? (
                recentFeeds.map((feed, index) => {
                  const { id, thumbnail, description, liked, heartCount } = feed;
                  const isLast = index === recentFeeds.length - 1;
                  return (
                    <div
                      key={id}
                      ref={isLast ? lastRecentRef : null}
                      className="lounge-page__card"
                      onClick={() => navigate(`/lounge/feed/${id}`)}
                    >
                      <div className="lounge-page__image-container">
                        <img
                          src={thumbnail || ""}
                          alt="썸네일"
                          className="lounge-page__thumbnail"
                        />
                        <div
                          className="lounge-page__heart"
                          onClick={(e) => handleHeartToggle(e, id, false)}
                        >
                          {liked ? (
                            <FaHeart className="lounge-page__heart-icon lounge-page__heart-icon--liked" />
                          ) : (
                            <FaRegHeart className="lounge-page__heart-icon" />
                          )}
                          <span className="lounge-page__heart-count">{heartCount}</span>
                        </div>
                      </div>
                      <div
                          className="lounge-page__content"
                          dangerouslySetInnerHTML={{ __html: description }}
                        />
                      </div>
                    //</div>
                  );
                })
              ) : (
                <p>피드가 없습니다.</p>
              )}
              {recentLoading && <p>로드 중...</p>}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoungePage;
