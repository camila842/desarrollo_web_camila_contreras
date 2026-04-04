// const addActivityButton = document.getElementById("add-activity");
// addActivityButton.addEventListener("click",() => {console.log("hola")});
// const templateActivity = document.getElementById("activity");
// const div = document.getElementById("activity-container");
// // activateButton(addActivityButton,templateActivity,div);

// const templateTimeActivity = document.getElementById("time-selector")

// let cloneTemplate = (template,div) => {
//     const clon = template.content.cloneNode(true);
//     div.appendChild(clon);
// }

// let activateButton = (button,template,div) => {
//     button.addEventListener("click",() => cloneTemplate(template,div));
// }

// let weekButtons = (template) => {
//     const weekDays = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"];

//     for (let i=0; i<7; i++){
//         let dayId = "day-";
//         dayId += weekDays[i];
//         var dayButton = document.getElementById(dayId);
//         let divId = weekDays[i] + "-div";
//         let div = getElementById(divId);
//         activateButton(dayButton,template,div);
//     }
// }

// weekButtons(templateTimeActivity);
let activitiesRegisterCounter = 0;

const addActivity = (event) => {
    event.preventDefault();
    let activity = document.getElementsByClassName("activity-container")[0];
    if (activitiesRegisterCounter == 0){
        let activityForm = document.getElementsByClassName("activities-register")[0];
        activityForm.className = "activities-register visible";
        activitiesRegisterCounter += 1;
    }
    else{
        if(activitiesRegisterCounter <= 2){
            let activityForm = document.getElementsByClassName("activities-register visible")[0];
            let newActivity = activity.cloneNode(true);
            activityForm.appendChild(newActivity);
            activitiesRegisterCounter += 1;
        }
    }
};

const addActBtn = document.getElementById("add-activity");
addActBtn.addEventListener("click",addActivity);