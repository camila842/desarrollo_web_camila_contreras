const templateTimeActivity = document.getElementById("time-selector")

let cloneTemplate = (template,div) => {
    const clon = template.content.cloneNode(true);
    div.appendChild(clon);
}

let activateButton = (button,template,div) => {
    button.addEventListener("click",() => cloneTemplate(template,div));
}

let weekButtons = (template) => {
    const weekDays = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"];

    for (let i=0; i<7; i++){
        let dayId = "day-";
        dayId += weekDays[i];
        let dayButton = document.getElementById(dayId);
        let divId = weekDays[i] + "-div";
        let div = getElementById(divId);
        activateButton(dayButton,template,div);
    }
}

weekButtons(templateTimeActivity)

const addActivityButton = document.getElementById("add-activity");
const templateActivity = document.getElementById("activity");
const div = document.getElementById("activity-container");
activateButton(addActivityButton,templateActivity,div);
