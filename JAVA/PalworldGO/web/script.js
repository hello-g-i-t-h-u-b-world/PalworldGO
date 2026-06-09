let map;
let userMarker;

let monsterMarkers = [];

let userLat = 37.5665;
let userLng = 126.9780;

let currentMonster = null;

// =================================================================
// 💡 [추가] 보안 및 페이지 진입 차단 가드 (페이지 로드 즉시 실행)
// =================================================================
(function windowAuthGuard() {
    const userId = sessionStorage.getItem("userId");
    // 로그인이 안 되어 있다면 카카오 지도를 그리기 전에 즉시 로그인 페이지로 이동시킵니다.
    if (!userId) {
        alert("로그인이 필요한 서비스입니다. 로그인 페이지로 이동합니다.");
        location.href = "login.html";
    }
})();

function initMap() {
    kakao.maps.load(() => {
        const container = document.getElementById('map');
        const options = { center: new kakao.maps.LatLng(userLat, userLng), level: 3 };
        map = new kakao.maps.Map(container, options);

        userMarker = new kakao.maps.Marker({ map: map, position: new kakao.maps.LatLng(userLat, userLng) });

        checkLoginStatus(); // 이미 위에서 걸렀으므로 여기서는 안심하고 UI만 세팅합니다.
        updateUserLocation();
        requestSpawn();
        setInterval(requestSpawn, 5000); // 5초마다 자동 갱신
    });
}

// 프로필 클릭 핸들러
function handleProfileClick() {
    const userId = sessionStorage.getItem("userId");
    if (!userId) {
        alert("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
        location.href = "login.html";
    } else {
        // 이미 로그인 된 경우 정보창 토글
        document.getElementById("user-info").classList.toggle("hidden");
    }
}

// 로그인 상태에 따른 UI 변경
function checkLoginStatus() {
    const userId = sessionStorage.getItem("userId");
    const username = sessionStorage.getItem("username");
    const userInfo = document.getElementById("user-info");

    // 위쪽 가드 조건문 덕분에 여기 들어온 userId는 무조건 존재함이 보장됩니다.
    userInfo.classList.remove("hidden");
    document.getElementById("display-id").innerText = username;
}

function updateUserLocation() {

    if(navigator.geolocation) {

        navigator.geolocation
            .getCurrentPosition(

                pos => {

                    userLat =
                        pos.coords.latitude;

                    userLng =
                        pos.coords.longitude;

                    const moveLatLon =
                        new kakao.maps.LatLng(
                            userLat,
                            userLng
                        );

                    map.setCenter(
                        moveLatLon
                    );

                    userMarker.setPosition(
                        moveLatLon
                    );

                    console.log(
                        "위치 갱신 성공:",
                        userLat,
                        userLng
                    );
                },

                err => {

                    console.error(
                        "위치 권한 거부:",
                        err
                    );
                },

                {
                    enableHighAccuracy: true,
                    timeout: 5000,
                    maximumAge: 0
                }
            );

    } else {

        alert(
            "위치 정보를 지원하지 않습니다."
        );
    }
}

// 몬스터 요청
async function requestSpawn() {

    try {

        const response =
            await fetch(
                `http://localhost:8080/spawn?lat=${userLat}&lng=${userLng}`
            );

        const data =
            await response.json();

        const monsters =
            data.monsters;

        const weather =
            data.weather;

        // 기존 마커 제거
        monsterMarkers.forEach(
            marker => {

                marker.setMap(null);
            }
        );

        monsterMarkers = [];

        // 다시 그림
        monsters.forEach(
            monster => {

                displayMonster(monster);
            }
        );

        updateWeatherUI(weather);

    } catch (e) {

        console.log(
            "서버 연결 실패"
        );

        console.error(e);
    }
}

function closeCaptureModal() {
    document
        .getElementById(
            "capture-modal"
        )
        .classList
        .add(
            "hidden"
        );
}

// 몬스터 표시
function displayMonster(data) {

    const exists =
        monsterMarkers.find(

            marker =>
                marker.spawnId === data.spawnId
        );

    if(exists) {

        return;
    }

    const imageUrl =
        `images/${data.name}.png`;

    const imageSize =
        new kakao.maps.Size(
            44,
            44
        );

    const imageOption = {

        offset:
            new kakao.maps.Point(
                22,
                44
            )
    };

    const markerImage =
        new kakao.maps.MarkerImage(
            imageUrl,
            imageSize,
            imageOption
        );

    const mMarker =
        new kakao.maps.Marker({

            position:
                new kakao.maps.LatLng(
                    data.lat,
                    data.lng
                ),

            map: map,

            image: markerImage
        });

    mMarker.spawnId =
        data.spawnId;

    data.marker =
        mMarker;

    kakao.maps.event.addListener(

        mMarker,

        'click',

        () => {

            currentMonster =
                data;

            document
                .getElementById(
                    "capture-modal"
                )
                .classList
                .remove(
                    "hidden"
                );

            document
                .getElementById(
                    "m-name"
                )
                .innerText =
                data.name;

            document
                .getElementById(
                    "m-type"
                )
                .innerText =
                "타입: "
                + data.type;
        }
    );

    monsterMarkers.push(
        mMarker
    );
}

// 날씨 UI
function updateWeatherUI(weather) {

    document
        .getElementById(
            "weather-text"
        )
        .innerText =
        weather;

    const boost =
        document.getElementById(
            "weather-boost"
        );

    const icon =
        document.getElementById(
            "weather-icon"
        );

    if(weather === "Clear") {

        icon.className =
            "fas fa-sun";

        boost.innerText =
            "불 타입 확률 UP";

    } else if(weather === "Rain") {

        icon.className =
            "fas fa-cloud-showers-heavy";

        boost.innerText =
            "물 타입 확률 UP";

    } else if(weather === "Clouds") {

        icon.className =
            "fas fa-cloud";

        boost.innerText =
            "풀 타입 확률 UP";

    } else if(weather === "Thunderstorm") {

        icon.className =
            "fas fa-bolt";

        boost.innerText =
            "전기 타입 확률 UP";
    }
}

// 버튼 누르면 현재 유저 위치로 지도 중심을 부드럽게 이동시키는 함수
function moveToUserLocation() {
    if (!map) return;

    // 1. 우선 기존에 저장된 유저의 좌표를 기반으로 지도를 부드럽게 이동시킵니다.
    const currentPos = new kakao.maps.LatLng(userLat, userLng);
    map.panTo(currentPos);

    // 2. 이동함과 동시에 실시간 최신 GPS 좌표를 한 번 더 갱신하여 오차를 줄입니다.
    updateUserLocation();
}

window.onload = initMap;