import { useState, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import useAuthStore from '../../stores/auth/useAuthStore';
import { BsCircleFill } from "react-icons/bs"; // 접속중 아이콘
import { FaCrown } from "react-icons/fa"; // 방장 왕관 아이콘
import CloseIcon from '@mui/icons-material/Close'; // Close 버튼 아이콘 추가
import SendIcon from '@mui/icons-material/Send'; // 종이비행기 아이콘 추가
import LogoutIcon from '@mui/icons-material/Logout';  // 나가기 아이콘 

import {
  Container,
  TextField,
  Button,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Typography,
  Box,
  Paper,
  Divider,
  Menu,
  MenuItem,
  IconButton,
} from '@mui/material';
import '../../styles/chat/ChatPage.css';
import MoreVertIcon from "@mui/icons-material/MoreVert";


const ChatPage = () => {
  const clientRef = useRef(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState(""); // 메시지 내용
  const [chatroomInfo, setChatroomInfo] = useState(null);  //채팅방 정보

  const [activeUsers, setActiveUsers] = useState(new Set());   // 실시간 접속 유저

  const { roomId } = useParams();  // url 에서 roomId 가지고 오기
  const navigate = useNavigate();
  const { user, checkAuthStatus } = useAuthStore();
  
  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const messagesEndRef = useRef(null);


  // 시간 변환
  const formatTimestamp = (isoString) => {
    const date = new Date(isoString);
    return date.toLocaleString("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
      hour12: false
    });
  };


  useEffect(() => {
    checkAuthStatus();
  }, []);

  //채팅방 정보 조회
  useEffect(() => {
    if(!roomId) return;

    const getChatroomInfo = async () => {
      try {
          const response = await axios.get(`http://localhost:8080/api/chat/${roomId}`, {
            withCredentials: true
          });
          
          setChatroomInfo({
            ...response.data,
            chatroomTitle: response.data.meetingTitle, // 채팅방 이름 추가
            chatParticipants: response.data.chatParticipants,  // 채팅방 참여자 목록 추가
          });

          // 안읽은 메세지 초기화
          try {
            await axios.post(`http://localhost:8080/api/chat/${roomId}/unread-reset`, {}, {
              withCredentials: true 
              });
          } catch (error) {
            console.error("unreadMessage Reset 중 오류 발생:", error);
          }

          // 현재 접속 중인 유저 추가
          await axios.post(`http://localhost:8080/api/chat/${roomId}/enter`, {}, {
            withCredentials: true
          });

          const activeUsersResponse = await axios.get(`http://localhost:8080/api/chat/${roomId}/activeUsers`, {
            withCredentials: true
          });

          setActiveUsers(new Set(activeUsersResponse.data));

          const preMessages = await axios.get(`http://localhost:8080/api/chat/${roomId}/messages`, {
            withCredentials: true
          });
          setMessages(preMessages.data);   // 기존 메세지 불러오기
      } catch (error) {
          console.error('채팅방 정보 불러오는 중 오류 발생:', error);
          navigate('/chatrooms');
      }
    };  
      if(roomId) {
        getChatroomInfo();
      }
  }, [roomId, navigate]);


  // 웹소켓 연결
  useEffect(() => {
    if (roomId) { 
      if (!clientRef.current) { 
        clientRef.current = new Client({
          brokerURL: "ws://localhost:8080/ws-stomp/websocket", // WebSocket 엔드포인트
  
          onConnect: async () => {
            console.log("Connected to WebSocket");
  
            // 채팅 메시지 구독
            clientRef.current.subscribe(`/sub/chatRoom/${roomId}`, async (message) => {
              const receivedData = JSON.parse(message.body);
              setMessages((prev) => [...prev, receivedData]);
            });
  
            // 실시간 접속 유저 목록 갱신
            clientRef.current.subscribe(`/sub/${roomId}/activeUsers`, (message) => {
              const activeUsersList = JSON.parse(message.body);
              setActiveUsers(new Set(activeUsersList));
            });
  
            // 채팅방 참여 유저 목록 실시간 반영
            clientRef.current.subscribe(`/sub/${roomId}/updateChatUsers`, (message) => {
              const response = JSON.parse(message.body);
              const updateUsersList = response.updateUsers;

              setChatroomInfo(prevChatroomInfo => ({
                ...prevChatroomInfo,
                chatParticipants: updateUsersList.chatParticipants,
              }));
            });

            // 새로운 유저 감지
            clientRef.current.subscribe(`/sub/${roomId}/newUser`, (message) => {
              const response = JSON.parse(message.body);
              const newUser = response.nickname;

              if(clientRef.current && clientRef.current.connected) {
                clientRef.current.publish({
                  destination: `/pub/chatRoom/${roomId}`,
                  body: JSON.stringify({
                    roomId: Number(roomId),
                    systemMessage: true,
                    content: `------  ${newUser}님이 입장했습니다.  ------`,
                    timestamp: new Date().toISOString(),
                  })
                });
              }
            });

  
            // 강퇴된 유저 감지
            clientRef.current.subscribe(`/sub/chatRoom/${roomId}/kicked`, (message) => {
              const receivedData = JSON.parse(message.body);
            
              if (receivedData.userId === String(user.id)) {
                alert(receivedData.message);

                clientRef.current.deactivate();
                clientRef.current = null;

              navigate('/chatrooms');
              }
            });
  
            // 방장이 채팅방을 삭제했을 때 모든 유저가 이동
            clientRef.current.subscribe(`/sub/chatRoom/${roomId}/deleted`, (message) => {
              alert(message.body); // 방장이 채팅방을 삭제했다는 알림
              navigate('/chatrooms');
  
              if (clientRef.current) {
                clientRef.current.deactivate();
                clientRef.current = null;
              }
            });
  
            // WebSocket이 연결될 때 activeUsers 갱신
            try {
              await axios.post(`http://localhost:8080/api/chat/${roomId}/enter`, {}, { withCredentials: true });
            } catch (error) {
              console.error("Active users 불러오는 중 오류 발생:", error);
            }
          },
  
          onDisconnect: async () => {
            console.log("Disconnected from WebSocket");
            try {
              await axios.post(`http://localhost:8080/api/chat/${roomId}/exit`, {}, { withCredentials: true });
            } catch (error) {
              console.error('웹소켓 종료 시 activeUsers 업데이트 중 오류 발생:', error);
            }
            clientRef.current = null; // 연결 해제 시 WebSocket 초기화
          }
        });
  
        clientRef.current.activate();
      }
    }
  
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        console.log("WebSocket deactivated");
        clientRef.current = null;
      }
    };
  }, [roomId]); 
  



  // 채팅방 나가기
  async function leaveChatroom() {
    const isHost = chatroomInfo.chatParticipants.some(p => p.user.id === user.id && p.isHost);

    if (isHost) {
      const confirmLeave = window.prompt(
        "방장이 채팅방을 나가면 채팅방은 삭제됩니다.\n\n그래도 나가시겠습니까?\n(입력: 확인)"
      );
  
      if (confirmLeave !== "확인") {
        alert("채팅방 삭제가 취소되었습니다.");
        return;
      }
    } else {
      const confirmMsg = confirm("정말 채팅방을 나가시겠습니까?");
      if (!confirmMsg) return;
    }    
    
      try { 
        await axios.delete(`http://localhost:8080/api/chat/delete/${roomId}?isHost=${isHost}`, {
          withCredentials: true
        });
        
        alert("채팅방을 성공적으로 나갔습니다.");

        // WebSocket이 끊어진 경우 다시 연결 후 메시지 전송
        if (!clientRef.current || !clientRef.current.connected) {
          let retries = 0;
          while (!clientRef.current.connected && retries < 5) {  // 최대 5번(5초) 대기
            console.log(`WebSocket 연결 대기 중... (${retries + 1})`);
            await new Promise(resolve => setTimeout(resolve, 1000));
            retries++;
          }
        }

        // 유저가 채팅방 나가면 메세지 출력
        if (!isHost) {
          if(clientRef.current && clientRef.current.connected) {
            clientRef.current.publish({
              destination: `/pub/chatRoom/${roomId}`,
              body: JSON.stringify({
                roomId: Number(roomId),
                systemMessage: true,
                content: `------  ${user.nickname}님이 채팅방을 나갔습니다  ------`,
                timestamp: new Date().toISOString(),
              })
            });
          }
        }

          // WebSocket 연결 해제
          if (clientRef.current) {
            console.log("WebSocket connection closed");
            clientRef.current.deactivate();
          }

        navigate(`/chatrooms`);        
      } catch (error) {
        console.error('채팅방 탈퇴하는 중 오류 발생:', error);
      }
  }

  // 유저 강퇴 시키기
  async function kickUser(kickUserId) {
    const confirmMsg = confirm("정말 이 유저를 강제 퇴장시키겠습니까?");
    if (!confirmMsg) return;
    
    const nickname = chatroomInfo?.chatParticipants?.find(p => p.user.id === kickUserId)?.user?.nickname || "unknown";

    try {
      await axios.post(`http://localhost:8080/api/chat/kickUser/${roomId}?userId=${kickUserId}`, {}, {
        withCredentials: true,
      });
  
      alert("유저를 강제 퇴장시켰습니다.");

      // 강퇴 메시지 전송
      if (clientRef.current && clientRef.current.connected) {
        clientRef.current.publish({
          destination: `/pub/chatRoom/${roomId}`,
          body: JSON.stringify({
            roomId: Number(roomId),
            systemMessage: true,
            content: `------ ${nickname}님이 강제 퇴장되었습니다 ------`,
            timestamp: new Date().toISOString(),
          }),
        });
      }

      alert("유저를 강제 퇴장시켰습니다.");

    } catch (error) {
      console.error("유저 강제 퇴장 중 오류 발생:", error);
      alert("유저를 강제 퇴장하는 데 실패했습니다.");
    }
  }
  

  // 메세지 전송
  const sendMessage = async () => {
    if (clientRef.current && roomId && user.nickname && content) {
      const chatMessage = {
        
        roomId: Number(roomId), // 숫자로 변환
        sender: user.nickname,
        content: content,
        timestamp: new Date().toISOString(),  // 현재 시간
      };

      clientRef.current.publish({
        destination: `/pub/chatRoom/${roomId}`, // pub/chatRoom/{id}로 메시지 전송
        body: JSON.stringify(chatMessage),
      });
      setContent(""); // 메시지 입력 필드 초기화

      
      // 접속 안한 유저의 안읽은 메세지 count++
      try {
        await axios.post(`http://localhost:8080/api/chat/${roomId}/unread-increase`, {}, {
          withCredentials: true 
          });
      } catch (error) {
        console.error("unreadMessage count 중 오류 발생:", error);
      }
    }
  };

  const handleMenuOpen = (event, userId) => {
    setAnchorEl(event.currentTarget);
    setSelectedUserId(userId);
  };
  
  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedUserId(null);
  };

  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth", block: "end" });
    }
  }, [messages]); // 메시지가 변경될 때 실행


  // 현재 로그인한 사용자가 방장인지 확인하는 함수
  const isUserHost = () => {
    return chatroomInfo?.chatParticipants.some(p => p.user.id === user.id && p.isHost);
  };

  // 유저 정보가 없는 경우 화면 표시 X
  if (!user || !user.id) {
    navigate('/chatrooms');
    return null;
  }

  return (
    <Container sx={{ margin: "20px auto", maxWidth: "70vw", height: "100vh", overflow: "hidden" }}>
      {chatroomInfo ? (
        <Paper elevation={3} sx={{ padding: 2, height: "90vh", display: "flex", flexDirection: "column", position: "relative" }}>
          <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <Typography variant="h4" sx={{ marginTop: 2 }}>{chatroomInfo.chatroomTitle}</Typography>
            <IconButton onClick={() => navigate("/chatrooms")} sx={{ fontSize: "20px", padding: "4px 8px" }}>
              <CloseIcon />
            </IconButton>
          </Box>
          <Divider sx={{ marginBottom: 2 }} />
  
          <Box sx={{ display: "flex", flex: 1, overflow: "hidden" }}>
            <Paper className="chatContainer" elevation={2} sx={{
              flex: 3.5, 
              display: "flex", 
              flexDirection: "column", 
              overflowY: "auto", 
              wordWrap: "break-word",
              whiteSpace: "normal",
              padding: 2
            }}>
              <List>
                {messages.map((msg, index) => (
                  <ListItem key={index} className={msg.sender === user.nickname ? "myChatContainer" : "otherChatContainer"}>
                    {msg.systemMessage ? (
                      <ListItemText primary={<Typography className="systemMessage">{msg.content}</Typography>} />
                    ) : msg.sender === user.nickname ? (
                      <Box className="myChatBox" sx={{ display: "flex", justifyContent: "flex-end", width: "100%" }}>
                        <Typography variant="body1" className="myChat">{msg.content}</Typography>
                        <Typography variant="caption">{formatTimestamp(msg.timestamp)}</Typography>
                      </Box>
                    ) : (
                      <Box className="otherChatBox" sx={{ display: "flex", justifyContent: "flex-start", width: "100%" }}>
                        <Typography variant="subtitle1" className="chatNickName">{msg.sender}</Typography>
                        <Typography variant="body1" className="otherChat">{msg.content}</Typography>
                        <Typography variant="caption">{formatTimestamp(msg.timestamp)}</Typography>
                      </Box>
                    )}
                  </ListItem>
                ))}
                {/* 스크롤을 자동으로 아래로 유지하는 ref */}
                <div ref={messagesEndRef} />
              </List>
            </Paper>
  
            <Paper className="userList" sx={{
              flex: 1, 
              display: "flex", 
              flexDirection: "column", 
              overflowY: "auto", 
              padding: 2, 
              position: "relative"
            }}>
              <Typography variant="h6" mt={1}>채팅방 참여 유저</Typography>
              <List>
              {chatroomInfo.chatParticipants.map((p, id) => (
              <ListItem key={id} sx={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
              <ListItemAvatar sx={{ position: "relative" }}>
                <Avatar src={p.user.userImg || undefined}>
                  {!p.user.userImg && p.user.nickname[0]}
                </Avatar>
                {p.isHost && <FaCrown size={23} color="gold" style={{ position: "absolute", top: -15, right: 0 }} />}
              </ListItemAvatar>

              <ListItemText
                primary={
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    {p.user.id === user.id && (
                      <Box sx={{ width: 20, height: 20, borderRadius: "50%", backgroundColor: "gray", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "12px", color: "white" }}>
                        나
                      </Box>
                    )}
                    {p.user.nickname}
                    {activeUsers.has(String(p.user.id)) && <BsCircleFill size={10} color="green" />}
                  </Box>
                }
              />

              {/* 호스트일 경우에만 더보기 버튼 표시 */}
              {isUserHost() && p.user.id !== user.id && (  <>
              <IconButton onClick={(event) => handleMenuOpen(event, p.user.id)}>
                <MoreVertIcon />
              </IconButton>
              <Menu anchorEl={anchorEl} open={Boolean(anchorEl) && selectedUserId === p.user.id} onClose={handleMenuClose}>
                <MenuItem onClick={() => kickUser(p.user.id)}>퇴장</MenuItem>
              </Menu>
                </>
              )}
            </ListItem>
              ))}
            </List>

            {/* 채팅방 나가기 버튼을 오른쪽 하단으로 이동 */}
            <Box sx={{ position: "absolute", bottom: 60, right: 10 }}>
              <Button
                variant="outlined"
                color="error"
                onClick={leaveChatroom}
                sx={{ fontSize: "12px", padding: "6px 6px", display: "flex", alignItems: "center", gap: 1 }}
              >
                <LogoutIcon fontSize="small" />
                
              </Button>
            </Box>
            </Paper>
          </Box>
  
          <Box sx={{ display: "flex", alignItems: "center", padding: 2, width: "100%" }}>
            <TextField
              className="chatInput"
              label="메세지를 입력하세요"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && sendMessage()} // 엔터 키 이벤트 추가
              placeholder="메세지를 입력하세요"
              variant="outlined"
              sx={{ flex: 0.81, width: "auto" }} // 입력창 크기 조정
            />
            {/* 전송 버튼을 아이콘 버튼으로 변경 */}
            <IconButton className="chatButton" color="primary" onClick={sendMessage} sx={{ marginLeft: 1 }}>
              <SendIcon />
            </IconButton>
          </Box>
        </Paper>
      ) : (
        <Typography>채팅방 정보를 불러오는 중...</Typography>
      )}
    </Container>
  );
};


export default ChatPage;
