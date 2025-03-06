import { useEffect, useState } from "react";
import api from '../../api/axios';
import { useNavigate } from "react-router-dom";
import {
  Box,
  Container,
  Button,
  Stack,
  Typography,
  Card,
  CardContent,
  CardMedia,
  Pagination,
} from "@mui/material";

function MeetingMgmtPage() {
  const [meetings, setMeetings] = useState([]);
  const [totalPages, setTotalPages] = useState(1); // 전체 페이지 수
  const [totalElements, setTotalElements] = useState(0); // 전체 데이터 개수
  const [currentPage, setCurrentPage] = useState(1); // 현재 페이지

  // 미팅 목록 가져오는 함수
  const fetchMeetings = () => {
    axios
      .get(`/api/meeting/listAll?page=${currentPage - 1}`, {
        withCredentials: true,
      })
      .then((response) => {
        setMeetings(response.data.content || []);
        setTotalPages(response.data.totalPages); // 전체 페이지 수 저장
        setTotalElements(response.data.totalElements); // 전체 요소 개수 저장
      })
      .catch((error) => {
        console.error("Error fetching meeting:", error);
      });
  };

  // 초기 데이터 로딩
  useEffect(() => {
    fetchMeetings();
  }, []);

  // 모임 삭제
  const onClickDeleteBtn = (meetingId) => {
    const isConfirmed = window.confirm("정말 삭제하시겠습니까?");

    if (!isConfirmed) {
      return; // 사용자가 취소하면 함수 종료
    }

    axios
      .delete(`/api/meeting/delete/${meetingId}`, {
        withCredentials: true,
      })
      .then(() => {
        alert("모임을 삭제하였습니다.");
        // 모임 목록 다시 불러오기
        fetchMeetings();
      })
      .catch((error) => {
        console.error("Error fetching delete meeting:", error);
      });
  };

  // 페이지 변경 핸들러
  const handlePageChange = (event, value) => {
    setCurrentPage(value);
  };

  return (
    <Container>
      <Box sx={{ display: "flex", justifyContent: "space-between", gap: 3 }}>
        {/* 개설된 모임 */}
        <Box
          sx={{
            flex: 1,
            maxWidth: "100%",
            backgroundColor: "#f9f9f9",
            padding: 4,
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: 2 }}>
            모든 모임 목록
          </Typography>
          {meetings.map((meeting) => (
            <Card
              key={meeting.meetingId}
              sx={{
                display: "flex",
                flexDirection: "row",
                marginBottom: 2,
                cursor: "pointer",
                boxShadow: 3,
                borderRadius: 1,
                overflow: "hidden",
              }}
            >
              <CardMedia
                component="img"
                sx={{ width: 200, height: 200, objectFit: "cover" }}
                image={meeting.thumbnail}
                alt={meeting.meetingTitle}
              />
              <CardContent
                sx={{
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "center",
                  padding: 2,
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
                  참여 인원 {meeting.numberPeopleCur} /{" "}
                  {meeting.numberPeopleMax}
                </Typography>
                <Typography
                  variant="body2"
                  sx={{ color: "text.secondary", marginBottom: 1 }}
                >
                  매주 {meeting.meetingWeek} {meeting.meetingTime.slice(0, 5)}
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
                <Stack spacing={2} direction="row">
                  <Button
                    variant="outlined"
                    color="error"
                    onClick={() => onClickDeleteBtn(meeting.meetingId)}
                  >
                    삭제
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          ))}
          {/* 페이지네이션 */}
          <Stack spacing={2} alignItems="center" sx={{ marginTop: 2 }}>
            <Pagination
              count={totalPages} // 전체 페이지 수
              page={currentPage} // 현재 페이지
              onChange={handlePageChange} // 페이지 변경 이벤트
              color="primary"
            />
          </Stack>
        </Box>
      </Box>
    </Container>
  );
}

export default MeetingMgmtPage;
