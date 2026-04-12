const checkNombre = () => {
    const nombre = document.getElementById("name").value;
    const valido = nombre && nombre.length >= 3;
    document.getElementById("error-name").className = valido ? "error" : "error visible";
    return valido;
};
const checkApellido = () => {
    const apellido = document.getElementById("surname").value;
    const valido = apellido && apellido.length >= 3;
    document.getElementById("error-surname").className = valido ? "error" : "error visible";
    return valido;
};
const checkRole = () => {
    const role = document.getElementById("role-input").value;
    const valido = role != "";
    document.getElementById("error-role").className = valido ? "error" : "error visible";
    return valido;
};
const checkEmail = () => {
    const checkAddressName = (addressName) => {
        return addressName && addressName.length>=3;
    };
    const checkAddressDomain = (addressDomain) => {
        if (addressDomain.includes(".")){
            const domainParts = addressDomain.split(".");
            let result = true;
            for (let txt of domainParts){
                result = result && txt;
            }
            return result;
        }
        else{
            return false;
        }
    };
    const email = document.getElementById("email").value;
    let valido = false;
    if(email){    
        const emailParts = email.split("@");
        if (emailParts.length == 2){
            valido = checkAddressName(emailParts[0]) && checkAddressDomain(emailParts[1]);
        }
        else{
            valido = false;
        }
    }
    document.getElementById("error-email").className = valido ? "error" : "error visible";
    return valido;
};
const checkPassword = () => { 
    const checkSpecial = (password) => {
        let contains = false;
        const specialCaracters = ["@","¿","?","{","}", "."];
        for (let carac of specialCaracters){
            contains = contains || password.includes(carac);
        }
        return contains;
    };
    const password = document.getElementById("password").value;
    const valido = password && password.length >= 8;
    const special = password && checkSpecial(password);
    document.getElementById("error-password-length").className = valido ? "error" : "error visible";
    document.getElementById("error-password-caracter").className = special ? "error" : "error visible";
    return valido && special;
};

const formValidator = (event) => {
    event.preventDefault();
    const valido = checkNombre() && checkApellido() && checkEmail() && checkRole() && checkPassword();
    document.getElementById("not-error-mesage").className = valido ? "error visible" : "error";
    if(valido){
        document.getElementById("sign-up-form").reset();
    }
};

const nameInput = document.getElementById("name");
nameInput.addEventListener("input",checkNombre);

const surnameInput = document.getElementById("surname");
surnameInput.addEventListener("input",checkApellido);

const roleInput = document.getElementById("role-input");
roleInput.addEventListener("change",checkRole);

const emailInput = document.getElementById("email");
emailInput.addEventListener("input",checkEmail);

const passwordInput = document.getElementById("password");
passwordInput.addEventListener("input",checkPassword);

const registration = document.getElementById("sign-up-form");
registration.addEventListener("submit",formValidator);

