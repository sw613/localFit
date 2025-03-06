import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useLocation } from "react-router-dom";

import {
  Box,
  Container,
  Button,
  Stack,
  Chip,
  TextField,
  Typography,
  FormHelperText,
} from "@mui/material";
import { FormLabel, RadioGroup, Radio, FormControlLabel } from "@mui/material";
import Grid from "@mui/material/Grid2";

import {
  OutlinedInput,
  InputLabel,
  MenuItem,
  FormControl,
  ListItemText,
  Select,
  Checkbox,
} from "@mui/material";

function UpdateMeetingPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const meetingId = location.state?.meetingId || null; // state가 없을 경우를 대비한 안전한 접근
  const [selectedDays, setSelectedDays] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [hashtags, setHashtags] = useState([]);
  const [inputHashtag, setInputHashtag] = useState("");
  const [isComposing, setIsComposing] = useState(false); // 한글 조합 상태 감지
  const [formErrors, setFormErrors] = useState({}); // 폼 에러 상태 관리

  const weeks = [
    "월요일",
    "화요일",
    "수요일",
    "목요일",
    "금요일",
    "토요일",
    "일요일",
  ];

  const [meetingReqDto, setMeetingReqDto] = useState({
    meetingTitle: "",
    content: "",
    numberPeopleMin: null,
    numberPeopleMax: null,
    meetingTime: "",
    numberAgeMin: null,
    numberAgeMax: null,
    applicationMethod: "",
    meetingWeek: "",
    facilityId: null,
    hashtags: null,
  });

  useEffect(() => {
    if (meetingId) {
      axios
        .get(`/api/meeting/${meetingId}`, {
          withCredentials: true,
        })
        .then((response) => {
          setMeetingReqDto(response.data);
          if (response.data.meetingWeek) {
            setSelectedDays(response.data.meetingWeek.split(",")); // 문자열을 배열로 변환하여 selectedDays에 설정
          }
        })
        .catch((error) => {
          console.error("Error fetching meeting:", error);
        });
    }
  }, [meetingId]);

// meetingReqDto에서 해시태그 값을 hashtags 상태로 설정
useEffect(() => {
    if (
      meetingReqDto.connectHashtags &&
      meetingReqDto.connectHashtags.length > 0 &&
      hashtags.length === 0 // 이미 설정된 경우 업데이트하지 않음
    ) {
      const hashtagsArray = meetingReqDto.connectHashtags.map(
        (item) => item.hashtag.hashTag
      );
      setHashtags(hashtagsArray);
    }
  }, [meetingReqDto]); // hashtags 의존성 제거
  

  // hashtags가 변경될 때 meetingReqDto를 업데이트 (불필요한 업데이트 방지)
  useEffect(() => {
    if (hashtags.length > 0) {
      setMeetingReqDto((prevDto) => {
        // 새로운 값과 기존 값이 같은 경우 업데이트하지 않음
        if (JSON.stringify(prevDto.hashtags) === JSON.stringify(hashtags)) {
          return prevDto;
        }
        return {
          ...prevDto,
          hashtags: hashtags,
        };
      });
    }
  }, [hashtags]);

  // 입력값 변경 핸들러
  const handleChange = (e) => {
    setMeetingReqDto({
      ...meetingReqDto,
      [e.target.name]: e.target.value,
    });
  };

  const handleMeetingweek = (meetingWeek) => {
    setMeetingReqDto({
      ...meetingReqDto,
      meetingWeek: meetingWeek,
    });
  };

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setMeetingReqDto((prevDto) => ({
        ...prevDto,
        thumbnail: file, // 파일을 상태에 반영
      }));
    }
  };

  const checkFormError = () => {
    let newErrors = {};

    if (!meetingReqDto.meetingTitle || meetingReqDto.meetingTitle.length < 5) {
      newErrors.meetingTitle = "제목은 최소 5자 이상 입력해주세요.";
    }

    if (!meetingReqDto.content || meetingReqDto.content.length < 10) {
      newErrors.content = "내용은 최소 10자 이상 입력해주세요.";
    }

    if (!meetingReqDto.meetingTime) {
      newErrors.meetingTime = "모임 시간을 선택해주세요.";
    }

    if (!meetingReqDto.numberPeopleMin || meetingReqDto.numberPeopleMin < 2) {
      newErrors.numberPeopleMin = "최소 인원은 2명 이상이어야 합니다.";
    }

    if (!meetingReqDto.numberPeopleMax || meetingReqDto.numberPeopleMax > 20) {
      newErrors.numberPeopleMax = "최대 인원은 20명 이하여야 합니다.";
    }

    if (!meetingReqDto.numberAgeMin || meetingReqDto.numberAgeMin < 15) {
      newErrors.numberAgeMin = "최소 연령은 15세 이상이어야 합니다.";
    }

    if (!meetingReqDto.numberAgeMax || meetingReqDto.numberAgeMax > 80) {
      newErrors.numberAgeMax = "최대 연령은 80세 이하여야 합니다.";
    }

    if (selectedDays.length === 0) {
      newErrors.meetingWeek = "최소 1개의 요일을 선택해주세요.";
    }

    setFormErrors(newErrors);
  };

  // 폼 제출 핸들러
  const handleSubmit = async (e) => {
    e.preventDefault(); // 기본 폼 제출 방지
  
    checkFormError();
    if (Object.keys(formErrors).length > 0) {
      return; // 에러가 있으면 제출 중단
    }
  
    // 상태 업데이트를 보장하기 위해 직접 meetingWeek 및 hashtags를 설정
    const updatedMeetingDto = {
      ...meetingReqDto,
      meetingWeek: selectedDays.join(","), // selectedDays 값을 meetingReqDto에 반영
      hashtags: hashtags, // 해시태그 값 반영
    };
  
    const formData = new FormData();
    formData.append(
      "meetingRequestDto",
      new Blob([JSON.stringify(updatedMeetingDto)], { type: "application/json" })
    );
    formData.append("thumbnail", selectedFile);
  
    try {
      const response = await axios.post(
        `/api/meeting/update/${meetingId}`,
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
          withCredentials: true,
        }
      );
      alert("모임 수정 성공!");
    } catch (error) {
      alert("모임 수정 실패!");
    }
  };
  
  const handleChangeWeeks = (event) => {
    const {
      target: { value },
    } = event;
  
    const newSelectedDays = typeof value === "string" ? value.split(",") : value;
  
    setSelectedDays(newSelectedDays);
    setMeetingReqDto((prev) => ({
      ...prev,
      meetingWeek: newSelectedDays.join(","),
    }));
  };
  

  const handleKeyDown = (event) => {
    if ((event.key === "Enter" || event.key === ",") && inputHashtag.trim() !== "" && !isComposing) {
      event.preventDefault(); // 폼 제출 방지
  
      const trimmedHashtag = inputHashtag.trim();
  
      if (hashtags.length >= 3) {
        return; // 해시태그 3개 이상 추가 방지
      }
  
      if (!hashtags.includes(trimmedHashtag)) { // 중복 체크
        setHashtags([...hashtags, trimmedHashtag]); // 해시태그 추가
        setInputHashtag(""); // 입력 필드 초기화
      } else {
        alert("이미 추가된 해시태그입니다!"); // 중복 입력 시 알림
      }
    }
  };  

  const handleDelete = (tagToDelete) => {
    setHashtags(hashtags.filter((tag) => tag !== tagToDelete)); // 삭제 기능
  };

  return (
    <Container maxWidth="sm">
      <form onSubmit={handleSubmit}>
        <FormControl component="fieldset">
          <FormLabel component="legend">신청 방식</FormLabel>
          <RadioGroup
            row
            name="applicationMethod"
            value={meetingReqDto.applicationMethod}
            onChange={handleChange}
          >
            <FormControlLabel
              value="FIRSTCOME"
              control={<Radio />}
              label="선착순"
            />
            <FormControlLabel
              value="APPLY"
              control={<Radio />}
              label="승인제"
            />
          </RadioGroup>
        </FormControl>
        <div>
          <FormLabel component="legend">참여 인원</FormLabel>
          <TextField
            type="number"
            label="최소 인원"
            name="numberPeopleMin"
            value={meetingReqDto.numberPeopleMin}
            onChange={handleChange}
            inputProps={{ min: 2, max: meetingReqDto.numberPeopleMax }}
            sx={{ width: "48%", marginRight: "4%" }}
            error={!!formErrors.numberPeopleMin}
            helperText={formErrors.numberPeopleMin}
          />

          <TextField
            type="number"
            label="최대 인원"
            name="numberPeopleMax"
            value={meetingReqDto.numberPeopleMax}
            onChange={handleChange}
            inputProps={{ min: meetingReqDto.numberPeopleMin, max: 20 }}
            sx={{ width: "48%" }}
            error={!!formErrors.numberPeopleMax}
            helperText={formErrors.numberPeopleMax}
          />
        </div>
        <div>
          <FormLabel component="legend">나이</FormLabel>
          <TextField
            type="number"
            label="최소 연령"
            name="numberAgeMin"
            value={meetingReqDto.numberAgeMin}
            onChange={handleChange}
            inputProps={{ min: 15, max: meetingReqDto.numberAgeMax }}
            sx={{ width: "48%", marginRight: "4%" }}
            error={!!formErrors.numberAgeMin}
            helperText={formErrors.numberAgeMin}
          />

          <TextField
            type="number"
            label="최대 연령"
            name="numberAgeMax"
            value={meetingReqDto.numberAgeMax}
            onChange={handleChange}
            inputProps={{ min: meetingReqDto.numberAgeMin, max: 80 }}
            sx={{ width: "48%" }}
            error={!!formErrors.numberAgeMax}
            helperText={formErrors.numberAgeMax}
          />
        </div>
        <div>
          <FormLabel component="legend">모임을 소개해 볼까요?</FormLabel>
          <input
            accept="image/*"
            style={{ display: "none" }}
            id="file-upload"
            type="file"
            onChange={handleFileChange}
          />
          <label htmlFor="file-upload">
            <Button variant="contained" component="span">
              사진 선택
            </Button>
          </label>
          <Typography variant="body2" sx={{ marginTop: 1 }}>
            {meetingReqDto.thumbnail ? (
              meetingReqDto.thumbnail instanceof File ? (
                <img
                  src={URL.createObjectURL(meetingReqDto.thumbnail)} // 파일일 경우 미리보기
                  alt="Selected Thumbnail"
                  style={{ width: "100px", height: "auto", marginTop: 1 }}
                />
              ) : (
                <img
                  src={meetingReqDto.thumbnail} // URL일 경우 바로 표시
                  alt="Thumbnail from server"
                  style={{ width: "100px", height: "auto", marginTop: 1 }}
                />
              )
            ) : (
              "선택된 파일 없음"
            )}
          </Typography>

          <TextField
            label="제목"
            name="meetingTitle"
            value={meetingReqDto.meetingTitle}
            onChange={handleChange}
            variant="outlined"
            fullWidth
            inputProps={{
              minLength: 5,
              maxLength: 50,
            }}
            sx={{ marginBottom: 2 }}
            error={!!formErrors.meetingTitle}
            helperText={formErrors.meetingTitle}
          />

          <TextField
            label="내용"
            name="content"
            multiline
            rows={4}
            value={meetingReqDto.content}
            onChange={handleChange}
            variant="outlined"
            fullWidth
            inputProps={{
              minLength: 10,
              maxLength: 1000,
            }}
            error={!!formErrors.content}
            helperText={formErrors.content}
          />
          <Box>
            <TextField
              label="태그 입력(최대 3개)"
              name="hashtag"
              value={inputHashtag}
              onChange={(e) => setInputHashtag(e.target.value)}
              onKeyDown={handleKeyDown}
              onCompositionStart={() => setIsComposing(true)} // 한글 입력 시작
              onCompositionEnd={() => setIsComposing(false)} // 한글 입력 완료
              fullWidth
              placeholder="#태그입력"
              inputProps={{
                minLength: 1,
                maxLength: 100,
              }}
              disabled={hashtags.length >= 3} // 3개 이상 입력 방지
            />
            <Box sx={{ marginTop: 2 }}>
              {hashtags.map((tag, index) => (
                <Chip
                  key={index}
                  label={tag}
                  onDelete={() => handleDelete(tag)}
                  sx={{ marginRight: 1 }}
                />
              ))}
            </Box>
          </Box>
        </div>
        <div>
          <FormControl fullWidth error={!!formErrors.meetingWeek}>
            <InputLabel>모임 요일 선택</InputLabel>
            <Select
              multiple
              value={selectedDays}
              onChange={handleChangeWeeks}
              renderValue={(selected) => selected.join(", ")}
            >
              {weeks.map((week) => (
                <MenuItem key={week} value={week}>
                  <Checkbox checked={selectedDays.includes(week)} />
                  <ListItemText primary={week} />
                </MenuItem>
              ))}
            </Select>
            <FormHelperText>{formErrors.meetingWeek}</FormHelperText>
          </FormControl>
        </div>
        <div>
          <TextField
            label="모임 시간"
            type="time"
            name="meetingTime"
            value={meetingReqDto.meetingTime}
            onChange={handleChange}
            fullWidth
            InputLabelProps={{ shrink: true }}
            error={!!formErrors.meetingTime}
            helperText={formErrors.meetingTime}
          />
        </div>
        <Button type="submit" variant="contained" fullWidth>
          수정 하기
        </Button>
      </form>
    </Container>
  );
}

export default UpdateMeetingPage;
