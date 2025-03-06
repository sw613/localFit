import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

import {
  Box,
  Container,
  Button,
  Stack,
  TextField,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from "@mui/material";
import Grid from "@mui/material/Grid2";
import {
  Card,
  CardContent,
  CardMedia,
  CardActionArea,
  Typography,
  Pagination,
} from "@mui/material";

function MeetingPage() {
  const [facilities, setFacilities] = useState([]); // 시설 목록
  const [groundCategory, setGroundCategory] = useState("축구장"); // 선택된 groundCategory 저장
  const [areaNames, setAreaNames] = useState([]); // 지역구 목록
  const [curAreaName, setAreaName] = useState(""); // 현재 선택된 지역구
  const navigate = useNavigate();
  const [totalPages, setTotalPages] = useState(1); // 전체 페이지 수
  const [totalElements, setTotalElements] = useState(0); // 전체 데이터 개수
  const [currentPage, setCurrentPage] = useState(1); // 현재 페이지

  const [searchKeyword, setSearchKeyword] = useState(""); // 자동 완성 키워드
  const [searchQuery, setSearchQuery] = useState(""); // 검색 클릭했을 떄 보낼 내용
  const [autoCompleteSuggestions, setAutoCompleteSuggestions] = useState([]); //자동 완성 검색 리스트

  useEffect(() => {
    // 딜레이
    const timer = setTimeout(() => {
      setDebouncedSearch(searchKeyword);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchKeyword]);

  useEffect(() => {
    if (searchKeyword.trim().length > 0) {
      axios
        .get(`/api/v1/search/facility/auto-complete?query=${searchKeyword}`)
        .then((response) => {
          setAutoCompleteSuggestions(response.data);
        })
        .catch((error) => {
          console.error("자동완성 에러:", error);
        });
    } else {
      setAutoCompleteSuggestions([]);
    }
  }, [searchKeyword]);

  useEffect(() => {
    // 검색 필터
    if (searchQuery !== "") {
      axios
        .get(
          `/api/facility/list/search?groundCategory=${groundCategory}&areaName=${curAreaName}&search=${searchQuery}&page=${
            currentPage - 1
          }`
        )
        .then((response) => {
          setFacilities(response.data.content);
          setTotalPages(response.data.totalPages);
          setTotalElements(response.data.totalElements);
        })
        .catch((error) => {
          console.error("시설 목록 에러:", error);
        });
    }
  }, [searchQuery, groundCategory, curAreaName, currentPage]);

  useEffect(() => {
    // 검색 창 데이터 입력 후 딜레이
    const timer = setTimeout(() => {
      setDebouncedSearch(searchKeyword);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchKeyword]);

  useEffect(() => {
    axios
      .get(`/api/facility/list/areaNames`, {
        withCredentials: true,
      }) // 지역구 목록 받아옴
      .then((response) => {
        setAreaNames(response.data); // 데이터 상태 업데이트
      })
      .catch((error) => {
        console.error("Error fetching areaNames:", error);
      });
  }, []);

  useEffect(() => {
    if (searchKeyword.trim().length > 0) {
      axios
        .get(`/api/v1/search/facility/auto-complete?query=${searchKeyword}`)
        .then((response) => {
          setAutoCompleteSuggestions(response.data);
        })
        .catch((error) => {
          console.error("자동완성 에러:", error);
        });
    } else {
      setAutoCompleteSuggestions([]); 
    }
  }, [searchKeyword]);

  useEffect(() => {
    // 체육시설, 지역구를 선택하면 그에 해당하는 체육시설 목록 받아옴
    axios
      .get(
        `/api/facility/list?groundCategory=${groundCategory}&areaName=${curAreaName}&page=${
          currentPage - 1
        }`,
        {
          withCredentials: true,
        }
      )
      .then((response) => {
        setFacilities(response.data.content); // 실제 데이터 리스트
        setTotalPages(response.data.totalPages); // 전체 페이지 수 저장
        setTotalElements(response.data.totalElements); // 전체 요소 개수 저장
      })
      .catch((error) => {
        console.error("Error fetching facilities:", error);
      });
  }, [groundCategory, curAreaName, currentPage]); // groundCategory 가 바뀔 때마다 실행

  const handleSearch = () => {
    if (searchKeyword.trim() !== "") {
      setSearchQuery(searchKeyword); 
    }
  };

  const goToDetailFacilityPage = (facilityId) => {
    navigate(`/facility/${facilityId}`);
  };


  // 페이지 변경 핸들러
  const handlePageChange = (event, value) => {
    setCurrentPage(value);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSearch(); 
    }
  };

  const onClickGroundCategory = (e) => {
    setGroundCategory(e.target.value);
  };

  const onClickAreaName = (e) => {
    setAreaName(e.target.value);
  };

  const handleAutoCompleteSelect = (facilityName) => {
    setSearchKeyword(facilityName);
    setSearchQuery(facilityName);
    setAutoCompleteSuggestions([]);
  };

  const handleSearchInputChange = (e) => {
    const value = e.target.value;
    setSearchKeyword(value);
  
    if (value.trim() === "") {
      setAutoCompleteSuggestions([]); 
    }
  };

  return (
    <Container>
      <h1>시설 목록</h1>

      <Box sx={{ position: "relative", paddingBottom: 2 }}>
        <TextField
          label="시설 검색"
          variant="outlined"
          fullWidth
          value={searchKeyword}
          onChange={handleSearchInputChange}
          onKeyDown={handleKeyDown} 
        />

        {autoCompleteSuggestions.length > 0 && (
          <List
            sx={{
              position: "absolute",
              top: "100%",
              left: 0,
              width: "100%",
              backgroundColor: "white",
              boxShadow: "0px 4px 6px rgba(0,0,0,0.1)",
              borderRadius: "5px",
              zIndex: 10,
            }}
          >
            {autoCompleteSuggestions.map((facility) => (
              <ListItem key={facility.id} disablePadding>
                <ListItemButton onClick={() => handleAutoCompleteSelect(facility.name)}>

                  <ListItemText primary={facility.name} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        )}
      </Box>

      {/* 카테고리 목록 */}
      {/* 카테고리 목록 */}
      <Box display="flex" justifyContent="center">
  <Stack spacing={2} direction="row">
    {["축구장", "야구장", "테니스장", "족구장", "풋살장", "기타"].map((category) => (
      <Button
        key={category}
        variant={groundCategory === category ? "contained" : "outlined"} // 선택된 버튼 강조
        value={category}
        onClick={() => setGroundCategory(category)}
        sx={{
          backgroundColor: groundCategory === category ? "#1976d2" : "white", // 선택된 버튼 배경색 변경
          color: groundCategory === category ? "white" : "black", // 글자 색상 변경
          fontWeight: groundCategory === category ? "bold" : "normal",
          border: groundCategory === category ? "none" : "1px solid #1976d2",
        }}
      >
        {category}
      </Button>
    ))}
  </Stack>
  </Box>

    {/* 지역 목록 | 시설 목록 분리 */}
<Box
  sx={{
    display: "grid",
    gridTemplateColumns: "1fr 3fr", // 왼쪽(지역 선택) 1 비율, 오른쪽(시설 목록) 3 비율
    gap: 3,
    mt: 3,
  }}
>
  {/* 지역 목록 */}
  <Box
    sx={{
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      padding: "16px",
      backgroundColor: "#f5f5f5",
      borderRadius: "8px",
      minWidth: "200px",
      maxHeight: "500px",
      overflowY: "auto",
    }}
  >
    <Typography variant="h6" sx={{ fontWeight: "bold", mb: 1 }}>
      지역 선택
    </Typography>
    <Box
  sx={{
    display: "grid",
    gridTemplateColumns: "repeat(3, 1fr)", // 3열 배치
    gap: 1,
    width: "100%",
  }}
>
  {areaNames.map((areaName) => (
    <Button
      key={areaName}
      variant={curAreaName === areaName ? "contained" : "outlined"}
      value={areaName}
      onClick={() => setAreaName((prev) => (prev === areaName ? "" : areaName))}
      sx={{
        backgroundColor: curAreaName === areaName ? "#1976d2" : "white",
        color: curAreaName === areaName ? "white" : "black",
        fontWeight: curAreaName === areaName ? "bold" : "normal",
        border: curAreaName === areaName ? "none" : "1px solid #1976d2",
        minWidth: "80px",  
        maxWidth: "100px", 
        height: "40px", 
        textAlign: "center",
        whiteSpace: "nowrap", 
        overflow: "hidden", 
        textOverflow: "ellipsis",
        fontSize: "12px", 
        padding: "5px 10px", 
      }}
    >
      {areaName}
    </Button>
  ))}
</Box>
  </Box>

  {/* 시설 목록 */}
  <Box
    sx={{
      backgroundColor: "#f9f9f9",
      padding: "20px",
      borderRadius: "8px",
      boxShadow: "0px 4px 6px rgba(0, 0, 0, 0.1)",
      minHeight: "500px",
    }}
  >
    <Typography variant="h6" sx={{ fontWeight: "bold", mb: 2, textAlign: "center" }}>
      시설 목록
    </Typography>

    <Grid container spacing={3} justifyContent="center">
      {facilities.length > 0 ? (
        facilities.map((facility) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={facility.id}>
            <Card
              sx={{ width: 350, height: 350, cursor: "pointer" }}
              onClick={() => goToDetailFacilityPage(facility.id)}
            >
              <CardActionArea>
                <CardMedia
                  component="img"
                  height="160"
                  image={facility.IMGURL || "/default-image.jpg"} // 이미지 없을 때 기본값 추가
                  alt={facility.AREANM}
                />
                <CardContent>
                  <Typography gutterBottom variant="h6" component="div" textAlign="center">
                    {facility.SVCNM}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    📍 {facility.PLACENM} · {facility.AREANM}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" textAlign="center">
                    📆 {new Date(facility.SVCOPNBGNDT).toISOString().split("T")[0]} ~{" "}
                    {new Date(facility.SVCOPNENDDT).toISOString().split("T")[0]}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))
      ) : (
        <Typography
          variant="h6"
          sx={{
            color: "gray",
            textAlign: "center",
            width: "100%",
            mt: 3,
          }}
        >
          등록된 시설이 없습니다.
        </Typography>
      )}
    </Grid>

    {/* 페이지네이션 */}
    <Stack spacing={2} alignItems="center" sx={{ marginTop: 3 }}>
      <Pagination
        count={totalPages}
        page={currentPage}
        onChange={handlePageChange}
        color="primary"
      />
    </Stack>
  </Box>
</Box>

    </Container>
  );
}

export default MeetingPage;
