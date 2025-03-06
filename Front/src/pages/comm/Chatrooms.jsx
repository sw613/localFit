import React, { useEffect, useState, useRef } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { Client } from "@stomp/stompjs";
import useAuthStore from '../../stores/auth/useAuthStore';
import '../../styles/chat/Chatrooms.css';
import { Box, Button, Card, CardContent, Container, List, ListItem, Typography } from "@mui/material";


const Chatrooms = () => {
  const [chatrooms, setChatrooms] = useState([]);
  const navigate = useNavigate();

  const [unreadMessage, setUnreadMessage] = useState(new Map());
  const clientRef = useRef(null);
  const { user, checkAuthStatus } = useAuthStore();



  // 채팅방 목록 가져오기
  const getChatrooms = async () => {
   if(!user) return;

    try {
        const response = await axios.get(`http://localhost:8080/api/chat/list`, {
            withCredentials: true  
        });
        setChatrooms(response.data); // 응답 데이터 저장
    } catch (error) {
        console.error('채팅방 목록을 불러오는 중 오류 발생:', error);
    }

};

  // 안읽은 메세지 불러오기
  const getUnreadMessages = async () => {
    if(!user) return;

    try {
      const response = await axios.get("http://localhost:8080/api/chat/unread-messages", { 
        withCredentials: true 
      });
        
      setUnreadMessage((prevMap) => {
        const newMap = new Map(prevMap);        

        Object.entries(response.data).forEach(([roomId, count]) => {
          newMap.set(roomId, count);
        });
        
        // 새로운 Map과 기존 prevMap을 비교하여 다를 때만 업데이트
        return newMap.size !== prevMap.size || [...newMap.keys()].some(key => newMap.get(key) !== prevMap.get(key))
          ? newMap
          : prevMap; // 동일한 경우 업데이트 방지
            });

    } catch (error) {
      console.error("안읽은 메시지를 불러오는 중 오류 발생:", error);
    }
  };

    useEffect(() => {
      getChatrooms();
      checkAuthStatus();
      getUnreadMessages();
    }, []);

  // 웹소켓 연결 
  useEffect(() => {
    if (!user) return;

    if (clientRef.current) {
      return;
    }
    
      const stompClient = new Client({
        brokerURL: "ws://localhost:8080/ws-stomp/websocket",
        onConnect: () => {
          console.log("Connected to WebSocket");

          stompClient.subscribe(`/sub/${String(user.id)}/updateUnread`, (message) => {
            const updatedUnread = JSON.parse(message.body);

            setUnreadMessage((prevMap) => {
              const newMap = new Map(prevMap);

              Object.entries(updatedUnread).forEach(([roomId, count]) => {
                newMap.set(roomId, count);
              });
              return new Map(newMap);
            });

          });
        },

        onDisconnect: () => {
          console.log("Disconnected from WebSocket");
        },
      });

      stompClient.activate();
      clientRef.current = stompClient;
    

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        console.log("WebSocket deactivated");
      }
   };
  }, [user]); 


  // 채팅방으로 이동
  function clickBtn(roomId) {
      navigate(`/chat/${roomId}`);
  }

  // 로그인 안 된 경우
  if (!user) {
    return (
      <Box display="flex" flexDirection="column" alignItems="center" width="100%">
        <Container sx={{ py: 13 }}>
          <Box sx={{ textAlign: "center", p: 0 }}>
            {/* 섹션 제목 */}
            <Typography variant="h6" color="error" fontWeight="bold">
              🔴 모임톡
            </Typography>
            <Typography variant="h4" fontWeight="bold" mt={1}>
              모임톡은 이렇게 이용하세요!
            </Typography>
  
            <List sx={{ textAlign: "left", display: "inline-block", mt: 2 }}>
              <ListItem>1️⃣ 원하는 체육시설 선택</ListItem>
              <ListItem>2️⃣ 생성된 모임을 확인하거나 직접 생성</ListItem>
              <ListItem>3️⃣ 모임에 가입하면 해당 모임에 대한 채팅방이 생성</ListItem>
            </List>

            <Box
              sx={{
                display: "flex",
                flexWrap: "wrap",
                justifyContent: "center",
                gap: 2,
                mt: 3,
              }}
            ><img 
            src="src/images/chatting.jpg" // 채팅팅 이미지 
            alt="채팅방 사진"
            style={{ width: "100%", maxWidth: "600px", borderRadius: "8px", marginBottom: "20px" }}
          />
            </Box>
  
            {/* 로그인인 버튼 */}
            <Button
              variant="outlined"
              sx={{ mt: 3, borderRadius: 3 }}
              onClick={() => navigate("/login")}
            >
              로그인 하고 모임톡에 참여하기 &gt;
            </Button>
          </Box>
        </Container>
      </Box>
    );}

  return (
    <div className="chatRoomContainer">
      <h2 className="chatRoomHeader">참여한 채팅방</h2>
      {chatrooms.length === 0 ? (
        <div className="noChatRooms">
          <p>가입된 모임이 없습니다.</p>
          <p>모임에 가입해 주세요.</p>
        </div>
      ) : (
        <div className="chatRoomList">
          {chatrooms.map((chat) => (
            <div key={chat.id} className="chatRoomCard">
              <img 
                src={chat.thumbnail}
                alt="모임 대표사진"
                className="chatRoomImage"
              />              
              {/* 안 읽은 메시지가 있을 경우 빨간색 동그라미 표시 */}
              {unreadMessage.get(String(chat.id)) > 0 && (
                <div className="unreadBadge">
                  {unreadMessage.get(String(chat.id))}
                </div>
              )}  
              <div className="chatRoomContent">
                <div className="chatRoomTitle">{chat.meetingTitle}</div>
                <div className="chatRoomMeta">
                  <span>매주 {chat.meetingWeek} {chat.meetingTime}</span>
                  <span>{chat.numberPeopleCur} / {chat.numberPeopleMax} 명</span>
                </div>
                <div className="chatRoomMeta">
                  <span>
                    <span className="locationIcon">📍</span>{chat.facilityName}
                  </span>
                </div>
              </div>
              
              <button className="chatEnterBtn" onClick={() => clickBtn(chat.id)}>입장</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
  
};

export default Chatrooms;
