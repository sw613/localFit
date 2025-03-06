package com.example.LocalFit.meeting.entity;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class MeetingRequestDto {

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(min = 5, max = 50, message = "5 ~ 50자 까지 입력해 주세요.")
    private String meetingTitle;

    @NotBlank(message = "내용을 입력해주세요")
    @Size(min = 10, max = 1000, message = "10 ~ 1000자 까지 입력해주세요.")
    private String content;

    @NotNull(message = "최소인원을 입력해주세요.")
    @Min(value = 2, message = "최소인원은 2명 이상이어야 합니다.")
    @Max(value = 20, message = "최대인원은 20명 이하여야 합니다.")
    private Long numberPeopleMin;

    @NotNull(message = "최대인원을 입력해주세요.")
    @Min(value = 2, message = "최소인원은 2명 이상이어야 합니다.")
    @Max(value = 20, message = "최대인원은 20명 이하여야 합니다.")
    private Long numberPeopleMax;

    @NotNull(message = "모임시간은 반드시 입력해야 합니다.")
    private LocalTime meetingTime;

    @NotNull(message = "최소연령을 입력해주세요.")
    @Min(value = 15, message = "최소연령은 15세 이상이어야 합니다.")
    @Max(value = 80, message = "최대연령은 80세 이하여야 합니다.")
    private Long numberAgeMin;

    @NotNull(message = "최대연령을 입력해주세요.")
    @Min(value = 15, message = "최소연령은 15세 이상이어야 합니다.")
    @Max(value = 80, message = "최대연령은 80세 이하여야 합니다.")
    private Long numberAgeMax;

    private String applicationMethod;

    @NotBlank(message = "모임요일은 반드시 입력해야 합니다.")
    private String meetingWeek;

    private Long facilityId;
    
    private List<String> hashtags;
}
