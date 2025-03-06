import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

import {
  Box,
  Container,
  Button,
  Chip,
  Typography,
  Card,
  CardContent,
  CardMedia,
} from "@mui/material";
import Grid from "@mui/material/Grid2";

function Main() {
  const navigate = useNavigate();

  const [facilities, setFacilities] = useState([]); // 시설 목록
  const [feeds, setFeeds] = useState([]);

  useEffect(() => {
    axios
      .get(`/api/facility/list/main`, {
        withCredentials: true,
      })
      .then((response) => {
        setFacilities(response.data || []); // 실제 데이터 리스트
      })
      .catch((error) => {
        console.error("Error fetching facilities:", error);
      });
  }, []);

  useEffect(() => {
    axios
      .get(`/api/lounge/list/main`, {
        withCredentials: true,
      })
      .then((response) => {
        setFeeds(response.data || []); // 실제 데이터 리스트
      })
      .catch((error) => {
        console.error("인기 피드 로드 에러:", error);
      });
  }, []);

  const onClickfaciltyBtn = () => {
    navigate(`/meeting`);
  };

  const onClickfeedBtn = () => {
    navigate(`/lounge`);
  };

  return (
    <Box sx={{ backgroundColor: "#f9f9f9"}}> 
      {/* 배너 섹션 */}
      <Box sx={{ textAlign: "center" }}>
        <Box
          component="img"
          src="src/images/banner.jpg" // 이미지 경로
          alt="배너 이미지"
          sx={{
            width: "100%",
            maxWidth: 800, // 최대 너비 설정
            height: "auto",
            borderRadius: 1, // 모서리 둥글게
            marginBottom: 2,
          }}
        />
        <Typography variant="h4" gutterBottom>
         매주 정기적으로 만나는 모임, 함께해요! 
        </Typography>
        <Typography variant="subtitle1">
        원데이 모임이 아닌, 매주 만나는 멤버들과 함께하는 정기 모임!
        취미를 나누고, 소통하며 더 즐겁게! 
        </Typography>
      </Box>

      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        width="100%"
      >
        <Container sx={{ bgcolor: "#f9f9f9" }}>
          {/* 체육시설 섹션 */}
          <Box sx={{ textAlign: "center", p: 4 }}>
            {/* 섹션 제목 */}
            <Typography variant="h6" color="error" fontWeight="bold">
              🔴 체육시설
            </Typography>
            <Typography variant="h4" fontWeight="bold" mt={1}>
              지속형 모임으로 <br /> 계속해서 친하게 지내요
            </Typography>

            <Box
              sx={{
                display: "flex",
                flexWrap: "wrap",
                justifyContent: "center",
                gap: 2,
                mt: 3,
              }}
            >
              {facilities.length > 0 ? (
                facilities.map((facility) => (
                  <Card
                    key={facility.id}
                    sx={{
                      width: "48%",
                      minWidth: 300,
                      display: "flex",
                      borderRadius: 3,
                      boxShadow: 2,
                    }}
                  >
                    {/* 카드 이미지 */}
                    <CardMedia
                      component="img"
                      image={facility.IMGURL}
                      alt={facility.AREANM}
                      sx={{ width: 140, borderRadius: "8px 0 0 8px" }}
                    />

                    {/* 카드 내용 */}
                    <CardContent sx={{ flex: 1 }}>
                      <Box sx={{ display: "flex", gap: 1 }}>
                        <Chip label={facility.MINCLASSNM} size="small" />
                        <Chip label="추천" size="small" color="error" />
                      </Box>
                      <Typography fontWeight="bold" mt={1}>
                        {facility.SVCNM}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        📍 {facility.PLACENM} · {facility.AREANM}
                      </Typography>
                    </CardContent>
                  </Card>
                ))
              ) : (
                <Typography>데이터가 없습니다.</Typography>
              )}
            </Box>
            {/* 더보기 버튼 */}
            <Button
              variant="outlined"
              sx={{ mt: 3, borderRadius: 3 }}
              onClick={onClickfaciltyBtn}
            >
              더보기 &gt;
            </Button>
          </Box>

          {/* 라운지 섹션 */}
          <Box sx={{  py: 6, textAlign: "center" }}>
            <Container>
              {/* 섹션 헤더 */}
              <Typography variant="h6" color="error" fontWeight="bold">
                🔴 라운지
              </Typography>
              <Typography variant="h4" fontWeight="bold" mt={1}>
                취향이 통하는 멤버들의 <br /> 피드를 구독해요
              </Typography>

              {/* 라운지 카드 리스트 */}
              <Grid container spacing={3} mt={3} justifyContent="center">
                {feeds.map((feed) => (
                  <Grid item xs={12} sm={6} md={3} key={feed.id}>
                    <Card
                      sx={{
                        height: 250,
                        width: 250,
                        borderRadius: 3,
                        boxShadow: 2,
                      }}
                    >
                      {/* 이미지 섹션 */}
                      <CardMedia
                        component="img"
                        image={feed.thumbnail}
                        alt="라운지 이미지"
                        sx={{ height: 250, width: 250, objectFit: "cover" }}
                      />
                    </Card>
                    {/* 피드 내용 */}
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        display: "-webkit-box",
                        WebkitLineClamp: 2, // 최대 2줄 표시
                        WebkitBoxOrient: "vertical",
                      }}
                      dangerouslySetInnerHTML={{ __html: feed.description }}
                    />
                  </Grid>
                ))}
              </Grid>

              {/* 더보기 버튼 */}
              <Button
                variant="outlined"
                sx={{ mt: 3, borderRadius: 3 }}
                onClick={onClickfeedBtn}
              >
                더보기 &gt;
              </Button>
            </Container>
          </Box>
        </Container>
      </Box>
    </Box>
  );
}

export default Main;
