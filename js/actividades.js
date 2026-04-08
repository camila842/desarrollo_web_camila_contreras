
let activitiesRegisterCounter = 0;

const activateTimeInput = () => {
    //const weekDays = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"];
    for (let day of document.getElementsByName("week-day")){
        day.addEventListener("change",(event) => {
            addTime(event.target.value,event.target.checked);
        });
    }
};

const addTime = (inputValue,checked) => {

    let dayDiv = document.getElementById(`${inputValue}-div`);
    if (checked){
        let timeInput = dayDiv.getElementsByClassName("time-selector")[0];
        if (timeInput){
            timeInput.className = "time-selector visible";
        }
        else{
            timeInput = document.getElementsByClassName("time-selector")[0];
            let newTime = timeInput.cloneNode(true);
            newTime.className = "time-selector visible";
            dayDiv.appendChild(newTime);
        }
    }
    else{
        let timeInput = dayDiv.getElementsByClassName("time-selector visible")[0];
        if (timeInput) {
            timeInput.className = "time-selector";
        }
    }
};

const addActivity = (event) => {
    event.preventDefault();
    if (activitiesRegisterCounter == 0){
        let activityForm = document.getElementsByClassName("activities-register")[0];
        activityForm.className = "activities-register visible";
        activitiesRegisterCounter += 1;
        
        let addActivityButton = document.getElementById("add-activity");
        addActivityButton.className = "button";

        let submitActivityButton = document.getElementById("submit-activity");
        submitActivityButton.className = "button visible";
    }
};

const validateActivity = (event) =>{
    event.preventDefault(); 

    const validadorNombre = (nombre) => {
        return nombre && nombre.length >= 3;
    };

    const validadorTipo = (tipo) => {
        return tipo !== "";
    };

    const validateOneDay = () => {
        const validadorHoras = (hora) => {
            return hora == "";
        };
        const activity = document.getElementsByClassName("activity-container")[0];
        const weekDays = activity.getElementsByClassName("day");
        const weekDaysInputs = activity.getElementsByName("week-day");
        let dayChecker = false;
        let timeChecker = true;
        for (let i=0; i<7; i++){
            let dayInput = weekDaysInputs[i];
            if (dayInput.checked){
                dayChecker = true;
                
                let begTime = weekDays[i].getElementsByClassName("from-time")[0];
                let endTime = weekDays[i].getElementsByClassName("from-time")[0];

                if (!validadorHoras(begTime.value) || !validadorHoras(endTime.value)){
                    timeChecker = false;
                }
            }
        }
        return dayChecker && timeChecker;
    };

    const addActivityToList = (nombre,tipo,dias,horaInicios,horaTerminos) => {
        const newNode = document.createElement("div");
        newNode.className = "actividad-item";
        const newName = document.createElement("span");
        newName.className = "nombre";
        newName.textContent = nombre ;

        const newType = document.createElement("span");
        newType.className = "tipo";
        newType.textContent = tipo;
        
        newNode.appendChild(newName);
        newNode.appendChild(newType);
        for (let i=0; i<7; i++){
            const inputDia = dias[i].getElementsByName("week-day")[0];
            if (inputDia.checked){
                const newDay = document.createElement("span");
                newDay.className = "day-of-activity";
                newDay.textContent = `${inputDia.value}: ` ;

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
    let tipoActividad = document.getElementById("tipo-de-actividad");
    let inicioActividad = document.getElementsByClassName("from-time");
    let terminoActividad = document.getElementsByClassName("until-time");

    let errorNombre = document.getElementById("error-name");
    let errorTipo = document.getElementById("error-tipo");
    let errorHoras = document.getElementById("error-horas");
    let errorDias = document.getElementById("error-dias");

    let valNom = validadorNombre(nombreActividad.value);
    let valTip = validadorTipo(tipoActividad.value);
    let valHorIni = validadorHoras(inicioActividad);
    let valHorTer = validadorHoras(terminoActividad);
    let valDias = validateOneDay();

    if (!valNom) {
        errorNombre.className = "error visible";
    } else {
        errorNombre.className = "error";
    }

    if (!valTip) {
        errorTipo.className = "error visible";
    } else {
        errorTipo.className = "error";
    }

    if (!valHorIni || !valHorTer) {
        errorHoras.className = "error visible";
    } else {
        errorHoras.className = "error";
    }

    if (!valDias) {
        errorDias.className = "error visible";
    } else {
        errorDias.className = "error";
    }

    if(valHorIni && valHorTer && valNom && valTip && valDias){
        const weekDays = document.getElementsByClassName("day");
        addActivityToList(nombreActividad.value,tipoActividad.value,weekDays,inicioActividad,terminoActividad);
    }

};

const addActBtn = document.getElementById("add-activity");
addActBtn.addEventListener("click",addActivity);


activateTimeInput();

const filledForm = document.getElementById("activities-register");
filledForm.addEventListener("submit",validateActivity);