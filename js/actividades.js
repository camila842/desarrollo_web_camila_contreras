
let activitiesRegisterCounter = 0;

const activateTimeInput = () => {
    for (let day of document.getElementsByName("week-day")) {
        day.addEventListener("change", (event) => {
            addTime(event.target.value, event.target.checked);
            checkDias();
        });
    }
};

const addTime = (inputValue, checked) => {
    let dayDiv = document.getElementById(`${inputValue}-div`);
    if (checked) {
        let timeInput = dayDiv.getElementsByClassName("time-selector")[0];
        if (timeInput) {
            timeInput.className = "time-selector visible";
            attachTimeListeners(timeInput);
        } else {
            timeInput = document.getElementsByClassName("time-selector")[0];
            let newTime = timeInput.cloneNode(true);
            newTime.className = "time-selector visible";
            for (let input of newTime.querySelectorAll("input[type='time']")) {
                input.value = "";
            }
            dayDiv.appendChild(newTime);
            attachTimeListeners(newTime);
        }
    } else {
        let timeInput = dayDiv.getElementsByClassName("time-selector visible")[0];
        if (timeInput) {
            timeInput.className = "time-selector";
        }
    }
};

const attachTimeListeners = (timeSelector) => {
    for (let input of timeSelector.querySelectorAll("input[type='time']")) {
        input.addEventListener("change", checkHoras);
    }
};

const checkNombre = () => {
    const nombre = document.getElementById("activity-name").value;
    const valido = nombre && nombre.length >= 3;
    document.getElementById("error-name").className = valido ? "error" : "error visible";
};

const checkTipo = () => {
    const tipo = document.getElementById("tipo-de-actividad").value;
    const valido = tipo !== "";
    document.getElementById("error-tipo").className = valido ? "error" : "error visible";
};

const checkDias = () => {
    const weekDaysInputs = document.getElementsByName("week-day");
    let anyChecked = false;
    for (let input of weekDaysInputs) {
        if (input.checked) { anyChecked = true; break; }
    }
    document.getElementById("error-dias").className = anyChecked ? "error" : "error visible";
    checkHoras();
};

const checkHoras = () => {
    const validadorHoras = (hora) => hora !== "";
    const weekDays   = document.getElementsByClassName("day");
    const weekDaysInputs = document.getElementsByName("week-day");
    let timeChecker = true;
    for (let i = 0; i < 7; i++) {
        if (weekDaysInputs[i].checked) {
            const begTime = weekDays[i].getElementsByClassName("from-time")[0];
            const endTime = weekDays[i].getElementsByClassName("until-time")[0];
            if (!validadorHoras(begTime.value) || !validadorHoras(endTime.value)) {
                timeChecker = false;
                break;
            }
        }
    }
    document.getElementById("error-horas").className = timeChecker ? "error" : "error visible";
};

const checkFile = () => {
    const files = document.getElementById("file-input").files;
    let valido = true;
    if (files.length != 0){
        
        const validTypes = ["png","jpg","jpeg","mp4"];
        for (let file of files){
            const splittedName = file.name.split(".");
            const type = splittedName[splittedName.length - 1];
            valido = valido && validTypes.includes(type);
        }
        document.getElementById("error-file-type").className = valido ? "error" : "error visible";
        document.getElementById("error-file").className = "error";
    }
    else{
        document.getElementById("error-file").className = "error visible";
        valido = false;
    }
    return valido;
};

const addActivity = (event) => {
    event.preventDefault();
    if (activitiesRegisterCounter == 0) {
        let activityForm = document.getElementsByClassName("activities-register")[0];
        activityForm.className = "activities-register visible";
        activitiesRegisterCounter += 1;
        
        let addActivityButton = document.getElementById("add-activity");
        addActivityButton.className = "button";

        let submitActivityButton = document.getElementById("submit-activity");
        submitActivityButton.className = "button visible";
    }
};

const validateActivity = (event) => {
    event.preventDefault();

    const validadorNombre = (nombre) => {
        return nombre && nombre.length >= 3;
    };

    const validadorTipo = (tipo) => {
        return tipo !== "";
    };

    const validateOneDay = () => {
        const validadorHoras = (hora) => {
            return hora !== "";
        };
        const activity = document.getElementsByClassName("activity-container")[0];
        const weekDays = activity.getElementsByClassName("day");
        const weekDaysInputs = document.getElementsByName("week-day");
        let dayChecker = false;
        let timeChecker = true;
        for (let i = 0; i < 7; i++) {
            let dayInput = weekDaysInputs[i];
            if (dayInput.checked) {
                dayChecker = true;

                let begTime = weekDays[i].getElementsByClassName("from-time")[0];
                let endTime = weekDays[i].getElementsByClassName("until-time")[0];

                if (!validadorHoras(begTime.value) || !validadorHoras(endTime.value)) {
                    timeChecker = false;
                }
            }
        }
        return dayChecker && timeChecker;
    };

    const addActivityToList = (nombre, tipo, dias) => {
        const newNode = document.createElement("div");
        newNode.className = "actividad-item";
        const newName = document.createElement("span");
        newName.className = "nombre";
        newName.textContent = nombre;

        const newType = document.createElement("span");
        newType.className = "tipo";
        newType.textContent = tipo;

        newNode.appendChild(newName);
        newNode.appendChild(newType);
        for (let i = 0; i < 7; i++) {
            const inputDia = dias[i].querySelector("[name='week-day']");
            if (inputDia.checked) {
                const newDay = document.createElement("span");
                newDay.className = "day-of-activity";
                newDay.textContent = `${inputDia.value}: `;

                const begTime = dias[i].getElementsByClassName("from-time")[0];
                const newBegHour = document.createElement("span");
                newBegHour.textContent = `Hora inicio: ${begTime.value}`;

                const endTime = dias[i].getElementsByClassName("until-time")[0];
                const newEndHour = document.createElement("span");
                newEndHour.textContent = `Hora término: ${endTime.value}`;

                newNode.appendChild(newDay);
                newNode.appendChild(newBegHour);
                newNode.appendChild(newEndHour);
            }
        }
        let activityDiv = document.getElementById("form-results");
        activityDiv.appendChild(newNode);
    };

    let nombreActividad = document.getElementById("activity-name");
    let tipoActividad   = document.getElementById("tipo-de-actividad");
    let errorNombre = document.getElementById("error-name");
    let errorTipo   = document.getElementById("error-tipo");
    let errorHoras  = document.getElementById("error-horas");
    let errorDias   = document.getElementById("error-dias");

    let valNom  = validadorNombre(nombreActividad.value);
    let valTip  = validadorTipo(tipoActividad.value);
    let valDias = validateOneDay();
    let valFile = checkFile();

    errorNombre.className = valNom  ? "error" : "error visible";
    errorTipo.className   = valTip  ? "error" : "error visible";
    errorDias.className   = valDias ? "error" : "error visible";
    errorHoras.className  = valDias ? "error" : "error visible";

    if (valNom && valTip && valDias && valFile) {
        const weekDays = document.getElementsByClassName("day");
        addActivityToList(nombreActividad.value, tipoActividad.value, weekDays);
        resetForm();
    }
};

const resetForm = () => {
    document.getElementById("activities-register").reset();

    // ocultar todos los time-selectors
    for (let timeSelector of document.getElementsByClassName("time-selector visible")) {
        timeSelector.className = "time-selector";
    }

    // limpiar errores
    for (let error of document.getElementsByClassName("error visible")) {
        error.className = "error";
    }
};

const addActBtn = document.getElementById("add-activity");
addActBtn.addEventListener("click", addActivity);

document.getElementById("activity-name").addEventListener("input", checkNombre);
document.getElementById("tipo-de-actividad").addEventListener("change", checkTipo);
document.getElementById("file-input").addEventListener("change",checkFile);

const filledForm = document.getElementById("activities-register");
filledForm.addEventListener("submit", validateActivity);

activateTimeInput();
