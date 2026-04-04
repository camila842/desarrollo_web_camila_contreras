
let activitiesRegisterCounter = 0;
conts activityTemplate = document.getElementsByClassName("activity-container");

const cloneTimes = () => {
    let timeInput = document.getElementsByClassName("time-selector")[0];
    const weekDays = ["tuesday","wednesday","thursday","friday","saturday","sunday"];
    for (let day of weekDays){
        let cloneInput = timeInput.cloneNode(true);
        let dayDiv = document.getElementById(`${day}-div`);
        dayDiv.appendChild(cloneInput);
    }
};
//cloneTimes();
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
            let daysSelector = newActivity.getElementsByClassName("day-selector");
            const timesDays = daysSelector.getElementsByName("week-day");
            for (day in daysSelector.getElementsByClassName("day")){
                let input = timesDays[day];
                let dayInput = daysSelector.getElementsByClassName("day")[day];
                let time = dayInput.getElementsByClassName("time-selector")[0];
                let timeVisible = input.getElementsByClassName("time-selector visible")[0];
                if (timeVisible){
                    timeVisible.className = "time-selector"
                }
                input.addEventListener("change",(event) => {
                    addTime(event.target.value,event.target.checked);
                });
            }
            activityForm.appendChild(newActivity);
            activitiesRegisterCounter += 1;
            
        }
    }
};

const addActBtn = document.getElementById("add-activity");
addActBtn.addEventListener("click",addActivity);

const weekDays = ["monday","tuesday","wednesday","thursday","friday","saturday","sunday"];
for (let day of document.getElementsByName("week-day")){
    //const dayNode = document.getElementById(`day-${day}`);
    day.addEventListener("change",(event) => {
        addTime(event.target.value,event.target.checked);
    });
}

// const monday = document.getElementById("day-monday");
// monday.addEventListener("click", (event) => {
//     addTime(event.target.value);
// });