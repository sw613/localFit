import React, { useEffect, useState } from "react";
import axios from "axios";
import { FaPencilAlt } from "react-icons/fa";
import { useNavigate, useParams } from "react-router-dom";
import "../../styles/lounge/MyLoungePage.css";
import "react-quill/dist/quill.snow.css";
import ReactQuill from "react-quill";
import useAuthStore from "../../stores/auth/useAuthStore";

function MyLoungePage() {
  const { userId: paramUserId } = useParams();
  const navigate = useNavigate();
  const { user, isLoggedIn, checkAuthStatus } = useAuthStore();

  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  const currentUserId = user ? user.id : null;
  const isOwnPage = !paramUserId || Number(paramUserId) === currentUserId;

  // 라운지 페이지 데이터
  const [loungeData, setLoungeData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 프로필 수정 관련
  const [isEditing, setIsEditing] = useState(false);
  const [newIntro, setNewIntro] = useState("");
  const [newProfileImage, setNewProfileImage] = useState(null);
  const [newProfilePreview, setNewProfilePreview] = useState(null);

  const [isFollowing, setIsFollowing] = useState(false);

  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState("");
  const [modalList, setModalList] = useState([]);

  useEffect(() => {
    const fetchLoungeData = async () => {
      try {
        setLoading(true);
        setError(null);
        let apiUrl = "/api/lounge/mypage";
        if (!isOwnPage) {
          apiUrl = `/api/lounge/user/${paramUserId}`;
        }
        const response = await axios.get(apiUrl);
        setLoungeData(response.data);
        if (!isOwnPage) {
          setIsFollowing(response.data.following);
        }
      } catch (err) {
        console.error("라운지 페이지 불러오기 실패:", err);
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchLoungeData();
  }, [paramUserId, isOwnPage]);

  // 프로필 수정 버튼 클릭
  const handleEditClick = () => {
    if (!loungeData) return;
    setIsEditing(true);
    setNewIntro(loungeData.intro || "");
    setNewProfilePreview(loungeData.profileImageUrl || "/default-profile.png");
    setNewProfileImage(null);
  };

  // 편집 취소
  const handleCancelEdit = () => {
    setIsEditing(false);
    setNewIntro("");
    setNewProfileImage(null);
    setNewProfilePreview(null);
  };

  // 파일 선택
  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setNewProfileImage(file);
      const reader = new FileReader();
      reader.onload = (event) => {
        setNewProfilePreview(event.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // 프로필 저장
  const handleSaveProfile = async () => {
    try {
      const formData = new FormData();
      if (newProfileImage) {
        formData.append("profileImage", newProfileImage);
      }
      formData.append("intro", newIntro);
      const res = await axios.post("/api/lounge/update-profile", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      setLoungeData(res.data);
      setIsEditing(false);
    } catch (err) {
      console.error("프로필 업데이트 실패:", err);
      alert("프로필 업데이트 중 오류가 발생했습니다.");
    }
  };

  const handleFollowToggle = () => {
    if (!paramUserId) return;
    if (!isFollowing) {
      axios
        .post(`/api/lounge/follow/${paramUserId}`)
        .then(() => {
          alert("팔로우 하였습니다.");
          setIsFollowing(true);
          setLoungeData((prev) => ({
            ...prev,
            followerCount: prev.followerCount + 1,
          }));
        })
        .catch((err) => {
          console.error("팔로우 실패:", err);
          alert("팔로우 중 오류가 발생했습니다.");
        });
    } else {
      axios
        .post(`/api/lounge/unfollow/${paramUserId}`)
        .then(() => {
          alert("언팔로우 하였습니다.");
          setIsFollowing(false);
          setLoungeData((prev) => ({
            ...prev,
            followerCount: prev.followerCount - 1,
          }));
        })
        .catch((err) => {
          console.error("언팔로우 실패:", err);
          alert("언팔로우 중 오류가 발생했습니다.");
        });
    }
  };

  const handleFeedClick = (feedId) => {
    navigate(`/lounge/feed/${feedId}`);
  };

  const fetchFollowers = async (userId) => {
    try {
      const res = await axios.get(`/api/lounge/user/${userId}/followers`);
      setModalList(res.data);
    } catch (err) {
      console.error("팔로워 목록 불러오기 실패:", err);
    }
  };

  const fetchFollowings = async (userId) => {
    try {
      const res = await axios.get(`/api/lounge/user/${userId}/followings`);
      setModalList(res.data);
    } catch (err) {
      console.error("팔로잉 목록 불러오기 실패:", err);
    }
  };

  const handleShowFollowers = async () => {
    if (!loungeData) return;
    setModalType("followers");
    await fetchFollowers(loungeData.userId);
    setShowModal(true);
  };

  const handleShowFollowings = async () => {
    if (!loungeData) return;
    setModalType("followings");
    await fetchFollowings(loungeData.userId);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  if (loading) {
    return <div className="my-lounge-page__loading">로딩 중...</div>;
  }
  if (error) {
    return (
      <div className="my-lounge-page__error">
        에러가 발생했습니다. 잠시 후 다시 시도해주세요.
      </div>
    );
  }
  if (!loungeData) {
    return null;
  }

  const { userId, nickname, profileImageUrl, intro, followerCount, followingCount, feedList } = loungeData;

  return (
    <div className="my-lounge-page">
      <div className="my-lounge-header">
        <div className="my-lounge-profile" style={{ position: "relative" }}>
          <div className="profile-image-wrapper" style={{ position: "relative" }}>
            <img
              src={isEditing ? newProfilePreview || "/default-profile.png" : profileImageUrl || "/default-profile.png"}
              alt="프로필 이미지"
              className="my-lounge-profile__image"
            />
            {isEditing && (
              <>
                <label
                  htmlFor="profileImageInput"
                  className="profile-image-edit-label"
                  style={{
                    position: "absolute",
                    right: "0",
                    bottom: "0",
                    backgroundColor: "rgba(0,0,0,0.6)",
                    color: "#fff",
                    padding: "4px",
                    borderRadius: "50%",
                    cursor: "pointer",
                  }}
                >
                  수정
                </label>
                <input
                  id="profileImageInput"
                  type="file"
                  accept="image/*"
                  style={{ display: "none" }}
                  onChange={handleFileChange}
                />
              </>
            )}
          </div>

          <h2 className="my-lounge-profile__nickname">{nickname}</h2>

          {isEditing ? (
            <ReactQuill
              theme="snow"
              value={newIntro}
              onChange={(content) => setNewIntro(content)}
              className="my-lounge-profile__intro-editor"
              placeholder="소개말을 입력하세요..."
            />
          ) : (
            <div
              className="my-lounge-profile__intro"
              dangerouslySetInnerHTML={{ __html: intro || "아직 소개말이 없습니다." }}
            />
          )}

          {isOwnPage ? (
            isEditing ? (
              <div className="profile-edit-buttons">
                <button onClick={handleSaveProfile} className="save-profile-button">
                  저장
                </button>
                <button onClick={handleCancelEdit} className="cancel-profile-button">
                  취소
                </button>
              </div>
            ) : (
              <button
                onClick={handleEditClick}
                className="edit-profile-button"
                style={{
                  position: "absolute",
                  top: "10px",
                  right: "10px",
                  background: "none",
                  border: "none",
                  cursor: "pointer",
                  fontSize: "24px",
                  color: "#333",
                }}
                title="프로필 수정"
              >
                <FaPencilAlt />
              </button>
            )
          ) : (
            <button
              onClick={handleFollowToggle}
              style={{
                position: "absolute",
                top: "10px",
                right: "10px",
                background: "#0275d8",
                border: "none",
                cursor: "pointer",
                fontSize: "14px",
                color: "#fff",
                padding: "8px 12px",
                borderRadius: "4px",
              }}
            >
              {isFollowing ? "언팔로우" : "팔로우"}
            </button>
          )}
        </div>

        <div className="my-lounge-stats">
          <span onClick={handleShowFollowers} style={{ cursor: "pointer" }}>
            팔로워 {followerCount}
          </span>
          <span onClick={handleShowFollowings} style={{ cursor: "pointer", marginLeft: "10px" }}>
            팔로잉 {followingCount}
          </span>
          <span style={{ marginLeft: "10px" }}>피드 {feedList ? feedList.length : 0}</span>
        </div>
      </div>

      <div className="my-lounge-feed-list">
        {feedList && feedList.length > 0 ? (
          <div className="my-lounge-feed-grid">
            {feedList.map((feed) => (
              <div
                key={feed.id}
                className="my-lounge-feed-card"
                onClick={() => handleFeedClick(feed.id)}
                style={{ cursor: "pointer" }}
              >
                <div className="my-lounge-feed__image-container">
                  <img
                    src={feed.images && feed.images.length > 0 ? feed.images[0] : "/no-image.png"}
                    alt="피드 썸네일"
                    className="my-lounge-feed__thumbnail"
                  />
                  <div className="my-lounge-feed__like-info">
                    ♥ {feed.heartCount || 0}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="my-lounge-feed-list__empty">아직 작성된 피드가 없습니다.</p>
        )}
      </div>

      {/* 모달 영역 */}
      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>{modalType === "followers" ? "팔로워" : "팔로잉"}</h3>
            <ul>
              {modalList.map((u) => (
                <li key={u.id} style={{ display: "flex", alignItems: "center", marginBottom: "8px" }}>
                  <img
                    src={u.profileImageUrl || "/default-profile.png"}
                    alt="프로필"
                    style={{ width: "30px", height: "30px", borderRadius: "50%", marginRight: "8px" }}
                    onClick={() => {
                      navigate(`/lounge/user/${u.id}`);
                      handleCloseModal();
                    }}
                  />
                  <span>{u.nickname}</span>
                </li>
              ))}
            </ul>
            <button onClick={handleCloseModal}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyLoungePage;
