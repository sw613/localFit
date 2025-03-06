import React, { useEffect, useState, useRef, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import {
  FaChevronLeft,
  FaChevronRight,
  FaHeart,
  FaRegHeart,
  FaRegComment,
  FaBars,
} from "react-icons/fa";
import "../../styles/lounge/FeedDetailPage.css";
import RelativeTime from "../../components/lounge/RelativeTime";
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";

function FeedDetailPage() {
  const { feedId } = useParams();
  const navigate = useNavigate();
  const [feed, setFeed] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [commentInput, setCommentInput] = useState('');
  const [parentCommentId, setParentCommentId] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);

  const commentInputRef = useRef(null);

  useEffect(() => {
    axios
      .get(`/api/lounge/feed/${feedId}`)
      .then((res) => {
        setFeed(res.data);
      })
      .catch((err) => {
        console.error('피드 상세 에러:', err);
      });
  }, [feedId, navigate]);

  if (!feed) return <div>로딩 중...</div>;

  const {
    userId,
    userNickname,
    userProfileImg,
    description,
    hashtags = [],
    comments = [],
    liked,
    heartCount,
    images = [],
    loggedIn,
    createdDate,
    editable, 
  } = feed;

  const handlePrev = () => {
    if (!images.length) return;
    setCurrentIndex((prevIndex) =>
      prevIndex === 0 ? images.length - 1 : prevIndex - 1
    );
  };

  const handleNext = () => {
    if (!images.length) return;
    setCurrentIndex((prevIndex) =>
      prevIndex === images.length - 1 ? 0 : prevIndex + 1
    );
  };

  const handleCommentSubmit = () => {
    if (!loggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    if (!commentInput.trim()) {
      alert('댓글 내용을 입력해주세요.');
      return;
    }
    axios
      .post(`/api/lounge/feed/${feedId}/comment`, { 
        content: commentInput,
        parentCommentId: parentCommentId,
      })
      .then((res) => {
        const newComment = res.data;
        if (parentCommentId) {
          const updatedComments = comments.map(comment => {
            if (comment.id === parentCommentId) {
              return {
                ...comment,
                replies: comment.replies ? [...comment.replies, newComment] : [newComment],
              };
            }
            return comment;
          });
          setFeed(prev => ({ ...prev, comments: updatedComments }));
        } else {
          setFeed(prev => ({ ...prev, comments: [...prev.comments, newComment] }));
        }
        setCommentInput('');
        setParentCommentId(null);
      })
      .catch((error) => {
        console.error('댓글 작성 에러:', error);
        alert('댓글 작성 중 에러가 발생했습니다.');
      });
  };

  const handleHeartToggle = () => {
    if (!loggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    axios
      .post(`/api/lounge/feed/${feedId}/heart/toggle`)
      .then((res) => {
        setFeed(res.data);
      })
      .catch((err) => {
        console.error('좋아요 토글 에러:', err);
        alert('좋아요 처리 중 에러 발생');
      });
  };

  const toggleDropdown = () => {
    setShowDropdown((prev) => !prev);
  };

  const handleEditFeed = () => {
    setShowDropdown(false);
    navigate(`/lounge/edit-feed/${feedId}`);
  };

  const handleDeleteFeed = () => {
    setShowDropdown(false);
    if (window.confirm("정말 삭제하시겠습니까?")) {
      axios
        .delete(`/api/lounge/delete-feed/${feedId}`)
        .then(() => {
          alert("피드가 삭제되었습니다.");
          navigate("/lounge");
        })
        .catch((err) => {
          console.error('피드 삭제 에러:', err);
          alert("삭제 중 오류가 발생했습니다.");
        });
    }
  };

  const CommentItem = ({ comment }) => {
    const navigate = useNavigate();
    const [showReplies, setShowReplies] = useState(false);

    const toggleReplies = () => {
      setShowReplies((prev) => !prev);
    };

    const handleReplyClick = () => {
      setParentCommentId(comment.id);
      setCommentInput(`@${comment.userNickName} `);
      // 입력창으로 포커스
      if(commentInputRef.current) {
        commentInputRef.current.focus();
      }
    };

    return (
      <div className="feed-detail__comment-item">
        <div
          className="comment-header"
          style={{ display: "flex", alignItems: "center" }}
        >
          {comment.userProfileImg && (
            <img
              src={comment.userProfileImg}
              alt="프로필 이미지"
              className="comment-profile-img"
              onClick={() => navigate(`/lounge/user/${comment.userId}`)}
              style={{
                cursor: "pointer",
                width: "30px",
                height: "30px",
                borderRadius: "50%",
                marginRight: "8px",
              }}
            />
          )}
          <strong>{comment.userNickName}</strong>
        </div>
        <p>{comment.content}</p>
        <div className="feed-detail__comment-actions">
          <span
            className="feed-detail__reply-button"
            onClick={handleReplyClick}
          >
            답글
          </span>
          {comment.replies &&
            comment.replies.length > 0 &&
            !showReplies && (
              <span
                className="feed-detail__replies-toggle"
                onClick={toggleReplies}
              >
                답글 {comment.replies.length}개 더 보기
              </span>
            )}
          {showReplies && comment.replies && comment.replies.length > 0 && (
            <div className="feed-detail__replies">
              {comment.replies.map((reply) => (
                <div key={reply.id} className="feed-detail__reply-item">
                  <div
                    className="reply-header"
                    style={{ display: "flex", alignItems: "center" }}
                  >
                    {reply.userProfileImg && (
                      <img
                        src={reply.userProfileImg}
                        alt="프로필 이미지"
                        className="comment-profile-img"
                        onClick={() => navigate(`/lounge/user/${reply.userId}`)}
                        style={{
                          cursor: "pointer",
                          width: "30px",
                          height: "30px",
                          borderRadius: "50%",
                          marginRight: "8px",
                        }}
                      />
                    )}
                    <strong>{reply.userNickName}</strong>
                  </div>
                  <p>{reply.content}</p>
                </div>
              ))}
              <span
                className="feed-detail__replies-toggle"
                onClick={toggleReplies}
              >
                답글 숨기기
              </span>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="feed-detail">
      <div className="feed-detail__header">
        <div className="profile-info">
          {userProfileImg && (
            <img
              src={userProfileImg}
              alt="프로필 이미지"
              className="profile-info__img"
              onClick={() => navigate(`/lounge/user/${feed.userId}`)}
            />
          )}
          <div className="profile-info__text">
            <h2 className="feed-detail__nickname">
              {userNickname || '사용자'}
            </h2>
            <RelativeTime date={createdDate} />
          </div>
        </div>
        {editable && (
          <div className="feed-detail__menu-container">
            <button
              className="feed-detail__menu-button"
              onClick={toggleDropdown}
              title="메뉴"
            >
              <FaBars />
            </button>
            {showDropdown && (
              <div className="feed-detail__menu-dropdown">
                <button
                  onClick={handleEditFeed}
                  className="feed-detail__menu-item"
                >
                  수정
                </button>
                <button
                  onClick={handleDeleteFeed}
                  className="feed-detail__menu-item"
                >
                  삭제
                </button>
              </div>
            )}
          </div>
        )}
      </div>
      
      <div className="feed-detail__slider">
        {images.length ? (
          <div className="feed-detail__slider-wrapper">
            {images.length > 1 && (
              <FaChevronLeft
                className="feed-detail__arrow feed-detail__arrow--left"
                onClick={handlePrev}
              />
            )}
            <img
              src={images[currentIndex]}
              alt="피드 이미지"
              className="feed-detail__image"
            />
            {images.length > 1 && (
              <FaChevronRight
                className="feed-detail__arrow feed-detail__arrow--right"
                onClick={handleNext}
              />
            )}
          </div>
        ) : (
          <div>이미지가 없습니다.</div>
        )}
      </div>
      
      <div className="feed-detail__description">
        <div dangerouslySetInnerHTML={{ __html: description }} />
      </div>
      
      <div className="feed-detail__hashtags">
        {hashtags.length
          ? hashtags.map((tag, i) => (
              <span key={i} className="feed-detail__hashtag">
                {tag}
              </span>
            ))
          : null}
      </div>
      
      <div className="feed-detail__actions">
        <div className="feed-detail__heart" onClick={handleHeartToggle}>
          {liked ? (
            <FaHeart className="feed-detail__heart-icon feed-detail__heart-icon--liked" />
          ) : (
            <FaRegHeart className="feed-detail__heart-icon" />
          )}
          <span className="feed-detail__heart-count">{heartCount}</span>
        </div>
        <div className="feed-detail__comments-info">
          <FaRegComment
            className="feed-detail__comment-icon"
            onClick={() => commentInputRef.current && commentInputRef.current.focus()}
          />
          <span className="feed-detail__comment-count">{comments.length}</span>
        </div>
      </div>
      
      <div className="feed-detail__comment-list">
        {comments.length ? (
          comments.map((comment) => (
            <CommentItem key={comment.id} comment={comment} />
          ))
        ) : (
          <p>댓글이 없습니다.</p>
        )}
      </div>
      
      <div className="feed-detail__comment-form">
        <input
          type="text"
          ref={commentInputRef}
          value={commentInput}
          onChange={(e) => setCommentInput(e.target.value)}
          placeholder="댓글 달기"
          className="feed-detail__comment-form-input"
        />
        <button
          onClick={handleCommentSubmit}
          className="feed-detail__comment-form-button"
        >
          작성
        </button>
      </div>
    </div>
  );
}

export default FeedDetailPage;
