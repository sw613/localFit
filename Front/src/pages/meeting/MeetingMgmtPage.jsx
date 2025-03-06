import { useEffect, useState } from "react";
import axios from "axios";
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
} from "@mui/material";

function MeetingMgmtPage() {
  const navigate = useNavigate();

  const [meetings, setMeetings] = useState([]);
  const [signups, setSignups] = useState([]);
  const [curSignups, setCurSignups] = useState([]);

  const goToUpdateMeetingPage = (meetingId) => {
    navigate(`/updatemeeting`, {
      state: { meetingId },
    });
  };

  // 신청 목록 가져오는 함수
  const fetchSignups = () => {
    axios
      .get(`/api/signup_meeting/list`, {
        withCredentials: true,
      })
      .then((response) => {
        setSignups(response.data);
      })
      .catch((error) => {
        console.error("Error fetching signups:", error);
      });
  };

  // 미팅 목록 가져오는 함수
  const fetchMeetings = () => {
    axios
      .get(`/api/meeting/list/user`, {
        withCredentials: true,
      })
      .then((response) => {
        setMeetings(response.data);
      })
      .catch((error) => {
        console.error("Error fetching meeting:", error);
      });
  };

  // 초기 데이터 로딩
  useEffect(() => {
    fetchMeetings();
    fetchSignups();
  }, []);

  useEffect(() => {
    if (signups.length > 0) {
      onClickMgmtBtn(curSignups[0]?.meetingId); // or use the appropriate condition
    }
  }, [signups]); // signups 상태 변경 후 동작 실행

  // 특정 모임의 신청 목록만 필터링
  const onClickMgmtBtn = (meetingId) => {
    const filteredSignups = signups.filter(
      (signup) => signup.meetingId === meetingId
    );
    setCurSignups([...filteredSignups]);
  };

  // 신청 수락
  const onClickAgreeBtn = (signup) => {
    const isConfirmed = window.confirm("수락하시겠습니까?");

    if (!isConfirmed) {
      return; // 사용자가 취소하면 함수 종료
    }

    axios
      .post(
        `/api/signup_meeting/agree/${signup.id}`,
        {},
        {
          withCredentials: true,
        }
      )
      .then(() => {
        alert("수락되었습니다.");
        // 신청 목록 다시 불러오기
        axios
          .get(`/api/signup_meeting/list`, {
            withCredentials: true,
          })
          .then((response) => {
            setSignups(response.data);
            const filteredSignups = response.data.filter(
              (s) => s.meetingId === signup.meetingId
            );
            setCurSignups(filteredSignups);
          })
          .catch((error) => {
            console.error("Error fetching signups:", error);
          });
      })
      .catch((error) => {
        alert("현재 수락할 수 없습니다.");
        console.error("Error fetching agree signup:", error);
      });
  };

  // 신청 거절
  const onClickRejectBtn = (signup) => {
    const isConfirmed = window.confirm("정말 거절하시겠습니까?");

    if (!isConfirmed) {
      return; // 사용자가 취소하면 함수 종료
    }

    axios
      .delete(`/api/signup_meeting/delete/${signup.id}`, {
        withCredentials: true,
      })
      .then(() => {
        alert("거절되었습니다.");
        // 신청 목록 다시 불러오기
        axios
          .get(`/api/signup_meeting/list`, {
            withCredentials: true,
          })
          .then((response) => {
            setSignups(response.data);
            const filteredSignups = response.data.filter(
              (s) => s.meetingId === signup.meetingId
            );
            setCurSignups(filteredSignups);
          })
          .catch((error) => {
            console.error("Error fetching signups:", error);
          });
      })
      .catch((error) => {
        console.error("Error fetching Reject signup:", error);
      });
  };

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

  return (
    <Container>
      <Box sx={{ display: "flex", justifyContent: "space-between", gap: 3 }}>
        {/* 나의 개설 모임 */}
        <Box
          sx={{
            flex: 1,
            maxWidth: "50%",
            backgroundColor: "#f9f9f9",
            padding: 2,
            maxHeight: "80vh",
            overflow: "auto",
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: 2 }}>
            나의 모임 목록
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
                maxHeight: 300,
              }}
            >
              <CardMedia
                component="img"
                sx={{ width: 200, height: 300, objectFit: "cover" }}
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
                <Box sx={{ display: "flex", flexWrap: "wrap", gap: 1, mb: 2 }}>
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
                    variant="contained"
                    color="primary"
                    onClick={() => onClickMgmtBtn(meeting.meetingId)}
                  >
                    멤버 관리
                  </Button>
                  <Button
                    variant="outlined"
                    color="secondary"
                    onClick={() => goToUpdateMeetingPage(meeting.meetingId)}
                  >
                    수정
                  </Button>
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
        </Box>

        {/* 신청 목록 */}
        <Box
          sx={{
            flex: 1,
            maxWidth: "50%",
            backgroundColor: "#f9f9f9",
            padding: 2,
            maxHeight: "80vh",
            overflow: "auto",
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: "bold", marginBottom: 2 }}>
            가입 신청 목록
          </Typography>
          {curSignups.map((signup) => (
            <Card
              key={signup.id}
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
                  {signup.nickname}
                </Typography>
                <Typography
                  variant="body2"
                  sx={{ color: "text.secondary", marginBottom: 1 }}
                >
                  {signup.greeting}
                </Typography>
                <Stack spacing={2} direction="row">
                  <Button
                    variant="contained"
                    color="success"
                    onClick={() => onClickAgreeBtn(signup)}
                    sx={{ width: "100%" }}
                  >
                    수락
                  </Button>
                  <Button
                    variant="outlined"
                    color="error"
                    onClick={() => onClickRejectBtn(signup)}
                    sx={{ width: "100%" }}
                  >
                    거절
                  </Button>
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Box>
      </Box>
    </Container>
  );
}

export default MeetingMgmtPage;
