import React, { useEffect, useState, useRef } from "react";
import { Map, MapMarker, MarkerClusterer } from "react-kakao-maps-sdk";
import axios from "axios";
import { Box, Card, CardActionArea, CardContent, CardMedia, Typography, Grid, Button } from "@mui/material";
import '../../styles/place/FacilityMap.css';
import { useNavigate } from "react-router-dom";

function FacilityMap() {
  const mapRef = useRef(null);
  const [positions, setPositions] = useState([]);
  const [selectedPlace, setSelectedPlace] = useState(null);
  const [facilities, setFacilities] = useState([]); // 같은 시설명을 가진 데이터 저장
  const [showDetails, setShowDetails] = useState(false);

  const [currentIndex, setCurrentIndex] = useState(0); // currentIndex를 상태로 정의
  const itemsPerPage = 1;

  const navigate = useNavigate();


  // 시설 데이터 가져오기
  useEffect(() => {
    axios
      .get(`http://localhost:8080/api/facility/allFacility`, {
        withCredentials: true,
      }) 
      .then((response) => {
        const facilities = response.data;

        const mappedData = facilities.filter(facility => {
          return facility.xpos && facility.ypos;
        })
        .map(facility => ({
          lat: parseFloat(facility.ypos?.trim()),
          lng: parseFloat(facility.xpos?.trim()),
          placename: facility.PLACENM.trim(),
        }));

        setPositions(mappedData); // 좌표 업데이트

      })
      .catch((error) => {
        console.error("Error fetching facility list:", error);
      });
  }, []);

  // 같은 시설명을 가진 시설 정보 가져오기
  const facilityDetails = (placename) => {
    axios
      .get(`http://localhost:8080/api/facility/byPlaceName`, {
        params: { placeName: placename },  // URL 인코딩 적용
        withCredentials: true,
      })
      .then((response) => {
        setFacilities(response.data); // 같은 시설명을 가진 데이터 저장
        setShowDetails(true);
        setCurrentIndex(0); // 새로운 장소 클릭 시 currentIndex 초기화
      })
      .catch((error) => {
        console.error("Error fetching facility details:", error);
      });
  };

  const onClusterclick = (_target, cluster) => {
    const map = mapRef.current;
    const level = map.getLevel() - 1;
    map.setLevel(level, { anchor: cluster.getCenter() });
  };

  
  // 시설 페이지지로 이동
  function detailPageBtn(facilityId) {
    setCurrentIndex(0);
    navigate(`/facility/${facilityId}`);
}

  return (
    <div className="container" style={{ fontFamily: 'Pretendard' }}>
      <h2 className="title">운동 시설 찾기</h2>

      <div className="content-wrapper">
        <div className="map-container">
          <Map
            center={{ lat: 37.5665, lng: 126.978 }}
            style={{ width: "100%", height: "700px" }}
            level={10}
            ref={mapRef}
          >
            <MarkerClusterer averageCenter minLevel={10}>
              {positions.map((pos) => (
                <MapMarker
                  key={`${pos.lat}-${pos.lng}-${pos.placename}`}
                  position={{ lat: pos.lat, lng: pos.lng }}
                  onClick={() => {
                    if (selectedPlace && selectedPlace.lat === pos.lat && selectedPlace.lng === pos.lng) {
                      setSelectedPlace(null);
                    } else {
                      setSelectedPlace(pos);
                      facilityDetails(pos.placename);
                    }
                  }}
                >
                  {selectedPlace && selectedPlace.lat === pos.lat && selectedPlace.lng === pos.lng && (
                    <div className="marker-label">{selectedPlace.placename}</div>
                  )}
                </MapMarker>
              ))}
            </MarkerClusterer>
          </Map>
        </div>

        <div className="facility-container">
          {showDetails && facilities.length > 0 && (
            <Box className="facility-box">
              <div className="facility-list">
                <Button 
                  onClick={() => setCurrentIndex(Math.max(currentIndex - itemsPerPage, 0))} 
                  disabled={currentIndex === 0} 
                  className="nav-button"
                >
                  ◀
                </Button>
                
                <Grid container spacing={3} className="facility-grid">
                  {facilities.slice(currentIndex, currentIndex + itemsPerPage).map((facility) => (
                    <Card key={facility.id} className="facility-card">
                      <CardActionArea>
                        <CardMedia component="img" height="200" image={facility.IMGURL} alt={facility.AREANM} />
                        <CardContent>
                          <Typography gutterBottom variant="h5" className="facility-text">{facility.SVCNM}</Typography>
                          <Typography variant="body2" className="facility-text">
                            <div>장소명: {facility.PLACENM} ({facility.AREANM})</div>
                            <div>이용기간: {new Date(facility.SVCOPNBGNDT).toISOString().split("T")[0]} ~ {new Date(facility.SVCOPNENDDT).toISOString().split("T")[0]}</div>
                          </Typography>
                            <Button
                            onClick={() => detailPageBtn(facility.id)}
                            sx={{
                              textTransform: 'none',
                              fontSize: '30px',
                              color: '#2A8182',
                              border: 'none',
                              background: 'none',
                              cursor: 'pointer',
                              padding: '25px',
                              '&:hover': {
                                textDecoration: 'underline',
                              },
                            }}
                          >
                            Go →
                          </Button>
                        </CardContent>
                      </CardActionArea>
                    </Card>
                  ))}
                </Grid>

                <Button 
                  onClick={() => setCurrentIndex(Math.min(currentIndex + itemsPerPage, facilities.length - itemsPerPage))} 
                  disabled={currentIndex + itemsPerPage >= facilities.length} 
                  className="nav-button"
                >
                  ▶
                </Button>
              </div>
            </Box>
          )}
        </div>
      </div>
    </div>
  );
}

export default FacilityMap;