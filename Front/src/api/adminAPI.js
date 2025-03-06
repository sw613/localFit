import api from '../api';

// 관리자 API 함수
export const adminAPI = {
  // 대시보드 데이터 조회
  getDashboardData: async () => {
    try {
      const response = await api.get('/admin/dashboard');
      return response.data;
    } catch (error) {
      console.error('대시보드 데이터 조회 실패:', error);
      throw error;
    }
  },

  // 사용자 목록 조회
  getAllUsers: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/admin/users?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('사용자 목록 조회 실패:', error);
      throw error;
    }
  },

  // 사용자 삭제
  deleteUser: async (userId) => {
    try {
      const response = await api.delete(`/admin/users/${userId}`);
      return response.data;
    } catch (error) {
      console.error(`사용자 삭제 실패 (ID: ${userId}):`, error);
      throw error;
    }
  },

  // 미팅 목록 조회
  getAllMeetings: async (page = 0, size = 6) => {
    try {
      const response = await api.get(`/admin/meeting/listAll?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('미팅 목록 조회 실패:', error);
      throw error;
    }
  },

  // 미팅 삭제
  deleteMeeting: async (meetingId) => {
    try {
      const response = await api.delete(`/admin/meeting/delete/${meetingId}`);
      return response.data;
    } catch (error) {
      console.error(`미팅 삭제 실패 (ID: ${meetingId}):`, error);
      throw error;
    }
  },

  // 피드 목록 조회
  getAllFeeds: async (page = 0, size = 10) => {
    try {
      const response = await api.get(`/admin/lounge/feeds?page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      console.error('피드 목록 조회 실패:', error);
      throw error;
    }
  },

  // 피드 삭제
  deleteFeed: async (feedId) => {
    try {
      const response = await api.delete(`/admin/lounge/feeds/${feedId}`);
      return response.data;
    } catch (error) {
      console.error(`피드 삭제 실패 (ID: ${feedId}):`, error);
      throw error;
    }
  }
};

export default adminAPI;