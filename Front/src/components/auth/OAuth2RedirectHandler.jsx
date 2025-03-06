import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import useAuthStore from '../../stores/auth/useAuthStore'

const OAuth2RedirectHandler = () => {
    const navigate = useNavigate()
    const { checkAuthStatus } = useAuthStore()

    useEffect(() => {
        const handleOAuthRedirect = async () => {
            const params = new URLSearchParams(window.location.search)
            const token = params.get('token')
            const needsAdditionalInfo = params.get('needsAdditionalInfo')

            if (token && window.location.pathname === '/oauth/callback') {
                try {
                    // 토큰이 있으면 인증 상태를 체크하고 업데이트
                    await checkAuthStatus()

                    // 추가 정보 입력 필요 여부에 따라 리다이렉트
                    if (needsAdditionalInfo === 'true') {
                        navigate('/additional-info', { replace: true })
                    } else {
                        navigate('/', { replace: true })
                    }
                } catch (error) {
                    console.error('OAuth 인증 처리 중 오류:', error)
                    navigate('/login', { replace: true })
                }
            }
        }

        handleOAuthRedirect()
    }, [navigate, checkAuthStatus])

    return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-lg">인증 처리중...</div>
        </div>
    )
}

export default OAuth2RedirectHandler