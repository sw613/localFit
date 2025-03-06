import React from 'react';
import { Box, Alert, Button } from '@mui/material';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error('Error caught by boundary:', error, errorInfo);
    }

    handleReset = () => {
        // 상태 초기화
        this.setState({ hasError: false, error: null });
        // 홈으로 리다이렉트
        window.location.href = '/';
    };

    render() {
        if (this.state.hasError) {
            return (
                <Box
                    display="flex"
                    flexDirection="column"
                    alignItems="center"
                    justifyContent="center"
                    minHeight="100vh"
                    gap={2}
                    p={3}
                    sx={{ bgcolor: '#f5f5f5' }}
                >
                    <Alert 
                        severity="error"
                        sx={{
                            width: '100%',
                            maxWidth: '400px'
                        }}
                    >
                        예상치 못한 오류가 발생했습니다.
                    </Alert>
                    <Button 
                        variant="contained" 
                        onClick={this.handleReset}
                        sx={{
                            mt: 2,
                            bgcolor: 'primary.main',
                            '&:hover': {
                                bgcolor: 'primary.dark'
                            }
                        }}
                    >
                        메인으로 돌아가기
                    </Button>
                </Box>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;