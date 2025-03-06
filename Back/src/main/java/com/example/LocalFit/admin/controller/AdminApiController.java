package com.example.LocalFit.admin.controller;

import com.example.LocalFit.admin.service.AdminService;
import com.example.LocalFit.meeting.entity.MeetingResponseDto;
import com.example.LocalFit.meeting.service.MeetingService;
import com.example.LocalFit.user.dto.UserResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {
    private final AdminService adminService;
    private final MeetingService meetingService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = adminService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/meeting/listAll")
    public Page<MeetingResponseDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        return meetingService.findAll(PageRequest.of(page, size));
    }

    @DeleteMapping("/meeting/delete/{meetingId}")
    public void deleteMeeting(@PathVariable Long meetingId) {
        meetingService.deleteMeeting(meetingId);
    }
}






