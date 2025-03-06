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

function CreateMeetingPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const facilityId = location.state?.facilityId || null; // state가 없을 경우를 대비한 안전한 접근
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
    applicationMethod: "FIRSTCOME",
    meetingWeek: "",
    facilityId: null,
    hashtags: null,
  });

  useEffect(() => {
    console.log("selectedFacilityId : " + facilityId);
    setMeetingReqDto({
      ...meetingReqDto,
      facilityId: facilityId,
    });
  }, [facilityId]);

  useEffect(() => {
    setMeetingReqDto((meetingReqDto) => ({
      ...meetingReqDto,
      hashtags: hashtags,
    }));
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
      setSelectedFile(file); // selectedFile 상태 업데이트
      setMeetingReqDto((prevDto) => ({
        ...prevDto,
        thumbnail: file,
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
    e.preventDefault();

    checkFormError();
    if (Object.keys(formErrors).length > 0) {
      return;
    }

    // selectedDays 배열을 ","로 구분된 문자열로 변환하여 formData에 추가
    const meetingWeek = selectedDays.join(","); // ["월요일", "화요일"] => "월요일,화요일"

    const updatedMeetingReqDto = {
      ...meetingReqDto,
      meetingWeek: meetingWeek, // 최신값 반영
    };

    const formData = new FormData();
    formData.append(
      "meetingRequestDto",
      new Blob([JSON.stringify(updatedMeetingReqDto)], {
        type: "application/json",
      })
    );

    if (selectedFile) {
      formData.append("thumbnail", selectedFile);
    }

    try {
      const response = await axios.post("/api/meeting/create", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
        withCredentials: true,
      });
      alert("모임 생성 성공!");
      setTimeout(() => {
        navigate("/chatrooms");
      }, 1500);
    } catch (error) {
      console.error(error);
      alert("모임 생성 실패!");
    }
  };

  const handleChangeWeeks = (event) => {
    const {
      target: { value },
    } = event;
    setSelectedDays(typeof value === "string" ? value.split(",") : value);
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
    <Container maxWidth="sm" sx={{ pt: 2, pb: 2 }}>
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
        <Box sx={{ pt: 2, pb: 1 }}>
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
        </Box>
        <Box sx={{ pb: 2 }}>
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
        </Box>
        <Box sx={{ pb: 2 }}>
          <FormLabel component="legend" sx={{ pb: 1 }}>
            모임을 소개해 볼까요?
          </FormLabel>
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
              <img
                src={URL.createObjectURL(meetingReqDto.thumbnail)} // 선택된 파일을 임시 URL로 변환
                alt="Selected Thumbnail"
                style={{ width: "100px", height: "auto", marginTop: 1 }}
              />
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
        </Box>
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
          모임 만들기
        </Button>
      </form>
    </Container>
  );
}

export default CreateMeetingPage;
