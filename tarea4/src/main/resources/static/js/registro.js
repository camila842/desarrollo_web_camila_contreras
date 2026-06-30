const checkNombre = () => {
    const nombre = document.getElementById("name").value.trim();
    const valido = nombre.length >= 3;
    document.getElementById("error-name").className = valido ? "error" : "error visible";
    return valido;
};
const checkApellido = () => {
    const apellido = document.getElementById("surname").value.trim();
    const valido = apellido.length >= 3;
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
const checkRegion = () => {
    const region = document.getElementById("region-select").value;
    const valido = region !== "";
    document.getElementById("error-region").className = valido ? "error" : "error visible";
    return valido;
};
const checkComuna = () => {
    const comuna = document.getElementById("comuna-select").value;
    const valido = comuna !== "";
    document.getElementById("error-comuna").className = valido ? "error" : "error visible";
    return valido;
};
const populateComunas = () => {
    const regionId = parseInt(document.getElementById("region-select").value);
    const comunaSelect = document.getElementById("comuna-select");

    comunaSelect.innerHTML = "";

    if (!regionId) {
        comunaSelect.disabled = true;
        comunaSelect.innerHTML = '<option value="">--Seleccione región primero--</option>';
        return;
    }

    const regionData = COMUNAS_POR_REGION.find(r => r.id === regionId);
    comunaSelect.disabled = false;
    comunaSelect.innerHTML = '<option value="">--Seleccione--</option>';

    if (regionData) {
        regionData.comunas.forEach(c => {
            const opt = document.createElement("option");
            opt.value = c.id;
            opt.textContent = c.nombre;
            comunaSelect.appendChild(opt);
        });
    }

    checkRegion();
    checkComuna();
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
    const valido = checkNombre() && checkApellido() && checkEmail() && checkRole() && checkRegion() && checkComuna() && checkPassword();
    if (!valido) {
        event.preventDefault();
    }
};

document.getElementById("name").addEventListener("input", checkNombre);
document.getElementById("surname").addEventListener("input", checkApellido);
document.getElementById("role-input").addEventListener("change", checkRole);
document.getElementById("region-select").addEventListener("change", populateComunas);
document.getElementById("comuna-select").addEventListener("change", checkComuna);
document.getElementById("email").addEventListener("input", checkEmail);
document.getElementById("password").addEventListener("input", checkPassword);
document.getElementById("sign-up-form").addEventListener("submit", formValidator);
