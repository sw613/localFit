
export const handleLogout = () => {
    alert("로그인이 만료되었습니다. 다시 로그인해 주세요.");
    window.location.replace('/login');
};

export const checkIsAuthenticated = async (apiClient) => {
    try {
        await apiClient.get('/users/mypage');
        return true;
    } catch (error) {
        return false;
    }
};