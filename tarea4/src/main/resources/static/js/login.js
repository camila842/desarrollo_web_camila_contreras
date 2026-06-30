const checkEmailLogin = () => {
    const email = document.getElementById("email").value.trim();
    let valido = false;

    if (email.length >= 3) {
        const partes = email.split("@");
        if (partes.length === 2 && partes[0].length >= 1 && partes[1].includes(".")) {
            const dominioPartes = partes[1].split(".");
            valido = dominioPartes.every(p => p.length > 0);
        }
    }

    document.getElementById("error-email").className = valido ? "error" : "error visible";
    return valido;
};

const checkPasswordLogin = () => {
    const password = document.getElementById("contrasenna").value;
    const valido = password.length > 0;
    document.getElementById("error-password").className = valido ? "error" : "error visible";
    return valido;
};

const loginValidator = (event) => {
    const valido = checkEmailLogin() && checkPasswordLogin();
    if (!valido) {
        event.preventDefault();
    }
};

document.getElementById("email").addEventListener("input", checkEmailLogin);
document.getElementById("contrasenna").addEventListener("input", checkPasswordLogin);
document.getElementById("login-form").addEventListener("submit", loginValidator);
