import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useLocation, useParams } from "react-router-dom";

import { Box, Container, Button, Stack } from "@mui/material";
import Grid from "@mui/material/Grid2";

import {
  Card,
  CardContent,
  CardMedia,
  CardActionArea,
  CardActions,
  Typography,
} from "@mui/material";

function DetailFacilityPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const { facilityId } = useParams(); // url 에서 facilityId 가져 오기

  const [facility, setFacility] = useState({});
  const [meetings, setMeetings] = useState([]);

  const goToCreateMeetingPage = () => {
    navigate("/createmeeting", { state: { facilityId: facilityId } });
  };

  const goToDetailMeetingPage = (meetingId, facilityId) => {
    navigate(`/meeting/${meetingId}`, {
      state: { facilityId },
    });
  };

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
  }, [facilityId]);

  useEffect(() => {
    axios
      .get(`/api/meeting/list?facilityId=${facilityId}`, {
        withCredentials: true,
      }) // 시설 정보 받아옴
      .then((response) => {
        setMeetings(response.data); // 데이터 상태 업데이트
      })
      .catch((error) => {
        console.error("Error fetching facility:", error);
      });
  }, []);

  const onClickCreateMeetingBtn = () => {
    goToCreateMeetingPage();
  };

  return (
    <Container>
      {/* 시설 정보 | 모임 목록 */}
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          gap: 3, // 구역 사이 간격
          marginTop: 2,
          marginBottom: 2,
        }}
      >
        {/* 시설 정보 */}
        <Box sx={{ flex: 1, maxWidth: "50%" }}>
          <Card sx={{ maxWidth: "100%" }}>
            <CardMedia
              component="img"
              width="auto"
              height="500"
              image={facility.IMGURL}
              alt={facility.AREANM}
            />
          </Card>

          {/* 시설 정보 */}
          <Box sx={{ marginTop: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: "bold" }}>
              {facility.SVCNM}
            </Typography>
            <Typography variant="body1" sx={{ color: "text.secondary" }}>
              📍 {facility.PLACENM} · {facility.AREANM}
            </Typography>

            {/* 이용 기간 */}
            <Typography
              variant="body2"
              sx={{ color: "text.secondary", marginTop: 1 }}
            >
              📝 접수기간:{" "}
              {facility.SVCOPNBGNDT
                ? new Date(facility.SVCOPNBGNDT).toISOString().split("T")[0]
                : "정보 없음"}{" "}
              ~{" "}
              {facility.SVCOPNENDDT
                ? new Date(facility.SVCOPNENDDT).toISOString().split("T")[0]
                : "정보 없음"}
            </Typography>

            {/* 이용시간 */}
            <Typography
              variant="body2"
              sx={{ color: "text.secondary", marginTop: 1 }}
            >
              ⏰ 이용시간: {facility.V_MIN} ~ {facility.V_MAX}
            </Typography>

            {/* 모임 만들기 버튼 */}
            <Button
              variant="contained"
              sx={{ width: "100%", marginTop: 2 }}
              onClick={onClickCreateMeetingBtn}
            >
              모임 만들기
            </Button>
          </Box>
        </Box>

        {/* 모임 목록 */}
        <Box sx={{ flex: 1, maxWidth: "50%", maxHeight: "80vh" }}>
          <Box>
            {meetings.map((meeting) => (
              <Card
                key={meeting.meetingId}
                sx={{
                  display: "flex",
                  flexDirection: "row",
                  marginBottom: 2,
                  cursor: "pointer",
                  boxShadow: 3,
                }}
                onClick={() =>
                  goToDetailMeetingPage(meeting.meetingId, facilityId)
                }
              >
                {/* 썸네일 이미지 */}
                <CardMedia
                  component="img"
                  sx={{ width: 200, height: 200 }}
                  image={meeting.thumbnail}
                  alt={meeting.meetingTitle}
                />

                {/* 카드 내용 */}
                <CardContent
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                  }}
                >
                  <Typography
                    variant="h6"
                    sx={{ fontWeight: "bold", marginBottom: 1 }}
                  >
                    {meeting.meetingTitle}
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{ color: "text.secondary", marginBottom: 1 }}
                  >
                    👥 {meeting.numberPeopleCur} /{" "}
                    {meeting.numberPeopleMax}
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{ color: "text.secondary", marginBottom: 1 }}
                  >
                    📅 매주 {meeting.meetingWeek} ⏰ {meeting.meetingTime.slice(0, 5)}
                  </Typography>
                  {/* 해시태그 */}
                  <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1 }}>
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
                </CardContent>
              </Card>
            ))}
          </Box>
        </Box>
      </Box>
    </Container>
  );
}

export default DetailFacilityPage;
