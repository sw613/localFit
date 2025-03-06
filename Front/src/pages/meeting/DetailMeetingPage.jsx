import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useLocation, useParams } from "react-router-dom";

import { Box, Container, Button, Stack, TextField } from "@mui/material";
import Grid from "@mui/material/Grid2";
import {
  Card,
  CardContent,
  CardMedia,
  CardActionArea,
  CardActions,
  Typography,
} from "@mui/material";

function DetailMeetingPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    greeting: "",
    meetingId: null,
  });

  const { meetingId } = useParams(); // url 에서 facilityId 가져 오기
  const facilityId = location.state?.facilityId || null;

  const [meeting, setMeeting] = useState({});
  const [facility, setFacility] = useState({});

  const goToPage = () => {
    navigate("/", { state: { facilityId: facilityId } });
  };

  useEffect(() => {
    axios
      .get(`/api/meeting/${meetingId}`, {
        withCredentials: true,
      }) // 미팅 정보 받아옴
      .then((response) => {
        setMeeting(response.data); // 데이터 상태 업데이트
      })
      .catch((error) => {
        console.error("Error fetching meeting:", error);
      });
  }, [meetingId]);

  useEffect(() => {
    axios
      .get(`/api/facility/${facilityId}`, {
        withCredentials: true,
      }) // 시설 정보 받아옴
      .then((response) => {
        setFacility(response.data); // 데이터 상태 업데이트
      })
      .catch((error) => {
        console.error("Error fetching facility:", error);
      });
  }, []);

  // 폼데이터에 meetingId 넣어줌
  useEffect(() => {
    setFormData({
      ...formData,
      meetingId: meetingId,
    });
  }, [meetingId]);

  // 입력값 변경 핸들러
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault(); // 기본 폼 제출 방지

    try {
      const response = await axios.post(
        "/api/signup_meeting/create",
        formData,
        { withCredentials: true }
      );
      alert("모임가입신청 성공!");
      setTimeout(() => {
        navigate("/chatrooms");
      }, 1500);
    } catch (error) {
      alert("모임가입신청 실패!");
    }
  };

  return (
    <Container maxWidth="md" sx={{ backgroundColor: "#f9f9f9", pt: 4, pb: 4 }}>
      {/* 썸네일 및 타이틀 */}
      <Card sx={{ maxWidth: "100%", mb: 3 }}>
        <CardMedia
          component="img"
          height="300"
          image={meeting.thumbnail}
          alt={facility.AREANM}
        />
      </Card>
      {/* 모임 설명 */}
      <Typography
        variant="h4"
        sx={{ fontWeight: "bold", textAlign: "center", mb: 3 }}
      >
        {meeting.meetingTitle ? meeting.meetingTitle : "제목 없음"}
      </Typography>
      <div
        style={{
          minHeight: "100px", // 최소 높이 설정
          backgroundColor: "#f4f4f4",
          display: "flex", // flex 사용
          alignItems: "center", // 세로 중앙 정렬
          padding: "15px", // 내부 여백 추가
        }}
      >
        <Typography variant="body1">
          📍 {facility.PLACENM} · {facility.AREANM} 📆{" "}
          {meeting.meetingWeek ? meeting.meetingWeek : "없음"} ⏰{" "}
          {meeting.meetingTime
            ? meeting.meetingTime.substring(0, 5)
            : "시간 없음"}{" "}
          👥 {meeting.numberPeopleCur} / {meeting.numberPeopleMax}{" "}
          {meeting.applicationMethod === "FIRSTCOME" ? "· 선착순 가입" : "· 승인제 가입" }
          {/* 해시태그 */}
          <Box sx={{ mt: 1, display: "flex", flexWrap: "wrap", gap: 1 }}>
            {meeting.connectHashtags &&
              meeting.connectHashtags.map((tagObj, index) => (
                <Typography
                  key={index}
                  variant="body2"
                  sx={{
                    backgroundColor: "#e0f2f1",
                    color: "#00796b",
                    padding: "4px 8px",
                    borderRadius: "12px",
                    fontSize: "0.875rem",
                  }}
                >
                  #{tagObj.hashtag.hashTag}
                </Typography>
              ))}
          </Box>
        </Typography>
      </div>

      {/* 모임 설명 */}
      <div style={{ display: "flex", gap: "20px", paddingTop: "20px" }}>
        <div style={{ width: "70%" }}>
          <Typography
            variant="body1"
            sx={{
              whiteSpace: "pre-line", // 개행 유지
              wordBreak: "break-word", // 긴 단어 줄바꿈
            }}
          >
            {meeting.content}
          </Typography>
        </div>

        {/* 모임 신청 */}
        <div
          style={{
            width: "30%",
            padding: "20px",
          }}
        >
          <Typography variant="h6" sx={{ mb: 2, fontWeight: "bold" }}>
            모임 신청
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              label="소개말을 입력하세요."
              name="greeting"
              multiline
              rows={4}
              onChange={handleChange}
              variant="outlined"
              fullWidth
              sx={{ mb: 2 }}
            />
            <Button type="submit" variant="contained" fullWidth>
              참여하기
            </Button>
          </form>
        </div>
      </div>
    </Container>
  );
}

export default DetailMeetingPage;
