import { create } from 'zustand';

const useFeedStore = create((set) => ({
  // 초기 상태: 빈 배열로 시작하는 피드 목록
  feeds: [],

  // API 호출 등을 통해 받아온 피드 목록 전체를 저장하는 함수
  setFeeds: (feeds) => set({ feeds }),

  // 특정 피드 ID에 해당하는 피드를 목록에서 제거하는 함수
  removeFeed: (feedId) =>
    set((state) => ({
      feeds: state.feeds.filter((feed) => feed.id !== feedId),
    })),
  // 새로운 피드를 목록에 추가하는 함수
  addFeed: (feed) =>
    set((state) => ({
      feeds: [...state.feeds, feed],
    })),
}));

export default useFeedStore;