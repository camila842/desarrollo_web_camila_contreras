const search = (event) => {
    const addInfo = (info, node) => {
        let infoNode = document.createElement("th");
        infoNode.textContent = info;
        node.appendChild(infoNode);
    };

    let table = document.getElementById("members-query");
    let members = document.getElementsByClassName("member-info")[0];

    if (!members) {
        let member = document.createElement("tr");
        member.className = "member-info";

        let memberName = "No Name Data";
        let memberEmail = "No Email Data";
        let memberActivity = "No Activity Data";
        let memberTipo = "No Type Data";

        addInfo(memberName, member);
        addInfo(memberEmail, member);
        addInfo(memberActivity, member);
        addInfo(memberTipo, member);

        table.appendChild(member);
    }
};

const searchForm = document.getElementById("members-query-form");

if (searchForm) {
    searchForm.addEventListener("submit", search);
}