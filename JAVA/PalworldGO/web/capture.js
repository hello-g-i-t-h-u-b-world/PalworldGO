let clickCount = 0;
let gameTime = 10;
let gameInterval;
let pokemonInterval;

function tryCapture() {

    console.log(
        "현재 몬스터:",
        currentMonster
    );

    if(!currentMonster) {

        alert("몬스터 정보가 없습니다.");

        return;
    }

    document
        .getElementById(
            "capture-modal"
        )
        .classList
        .add(
            "hidden"
        );

    startCaptureGame(
        currentMonster
    );
}

function startCaptureGame(monster) {

    clickCount = 0;

    gameTime = 10;

    const game =
        document.getElementById(
            "capture-game"
        );

    const pokemon =
        document.getElementById(
            "moving-pokemon"
        );

    const timer =
        document.getElementById(
            "timer"
        );

    game.classList.remove(
        "hidden"
    );

    pokemon.src =
        `images/${monster.name}.png`;

    pokemon.onerror = () => {

        pokemon.src =
            "images/default.png";
    };

    timer.innerText =
        gameTime;

    clearInterval(gameInterval);

    clearInterval(pokemonInterval);

    // 타이머
    gameInterval = setInterval(() => {

        gameTime--;

        timer.innerText =
            gameTime;

        if(gameTime <= 0) {

            clearInterval(
                gameInterval
            );

            clearInterval(
                pokemonInterval
            );

            finishCapture(
                monster
            );
        }

    }, 1000);

    // 랜덤 이동
    pokemonInterval = setInterval(() => {

        const x =
            Math.random() * 80;

        const y =
            Math.random() * 80;

        pokemon.style.left =
            x + "%";

        pokemon.style.top =
            y + "%";
        pokemon.style.transition = "all 1s linear";

    }, 1000);

    // 클릭 성공
    pokemon.onclick = () => {

        clickCount++;

        console.log(
            "클릭 성공:",
            clickCount
        );
    };
}

// capture.js - finishCapture 함수 내부 수정 부분
async function finishCapture(monster) {
    clearInterval(gameInterval);
    clearInterval(pokemonInterval);

    try {
        // 백엔드 CaptureGameHandler API 호출
        const response = await fetch(
            "http://localhost:8080/capture-game", // 프로젝트 호스트 주소에 맞게 확인
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    userId: sessionStorage.getItem("userId"),
                    spawnId: monster.spawnId,
                    monsterName: monster.name,
                    type: monster.type,
                    // 💡 [추가] 사용자가 10초 동안 미니게임에서 클릭 성공한 횟수 전송!
                    clickCount: clickCount
                })
            }
        );

        const result = await response.json();

        // 서버에서 최종 연산되어 온 포획 여부(success) 판정 결과 확인
        if (result.success) {
            closeCaptureGame();
            alert(`포획 성공! (클릭 횟수: ${clickCount}번, 확률: ${result.rate}%)`);
        } else {
            closeCaptureGame();
            alert(`포획 실패! (클릭 횟수: ${clickCount}번, 확률: ${result.rate}%)`);
        }

        // =====================
        // 마커 제거
        // =====================
        if(monster.marker) {
            monster.marker.setMap(null);
            monsterMarkers = monsterMarkers.filter(
                marker => marker !== monster.marker
            );
        }

    } catch (e) {
        console.error("finishCapture 오류:", e);
        alert("포획 처리 실패");
    }
}

function closeCaptureGame() {

    clearInterval(gameInterval);

    clearInterval(pokemonInterval);

    const game =
        document.getElementById(
            "capture-game"
        );

    const pokemon =
        document.getElementById(
            "moving-pokemon"
        );

    game.classList.add(
        "hidden"
    );

    // 다시 보이게 설정
    pokemon.style.display =
        "block";

    // 위치 초기화
    pokemon.style.left = "40%";
    pokemon.style.top = "40%";
}