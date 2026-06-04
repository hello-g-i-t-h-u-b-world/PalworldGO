async function register() {

    const username =
        document.getElementById(
            "username"
        ).value;

    const password =
        document.getElementById(
            "password"
        ).value;

    const response = await fetch(
        "http://localhost:8080/register",
        {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                username,
                password
            })
        }
    );

    const result =
        await response.json();

    if(result.success) {

        alert("회원가입 성공");

    } else {

        alert("회원가입 실패");
    }
}

async function login() {

    const username =
        document.getElementById(
            "username"
        ).value;

    const password =
        document.getElementById(
            "password"
        ).value;

    const response =
        await fetch(
            "http://localhost:8080/login",
            {

                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify({

                    username: username,

                    password: password
                })
            }
        );

    const result =
        await response.json();

    console.log(result);

    if(result.success) {

        sessionStorage.setItem("userId", result.userId);

        sessionStorage.setItem("username", result.username);

        alert("로그인 성공");

        location.href =
            "index.html";

    } else {

        alert("아이디 또는 비밀번호 오류");
    }
}