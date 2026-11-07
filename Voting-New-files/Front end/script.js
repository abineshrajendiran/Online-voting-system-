// Load parties from Java backend

fetch("/api/parties")
.then(response => response.json())
.then(data => {

    let dropdown = document.getElementById("party");

    data.forEach(party => {

        let option = document.createElement("option");

        option.value = party.id;
        option.textContent = party.name;

        dropdown.appendChild(option);
    });
});


// Submit vote

function submitVote() {

    let voterId =
        document.getElementById("voterId").value;

    let partyId =
        document.getElementById("party").value;

    if (!voterId || !partyId) {
        alert("Enter Voter ID and select a party");
        return;
    }

    fetch("/api/vote", {
        method: "POST",
        headers: {
            "Content-Type":
                "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
            voterId: voterId,
            partyId: partyId
        })
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById("message")
        .textContent = data;
    });
}


// Display results

function showResults() {

    fetch("/api/results")
    .then(response => response.json())
    .then(data => {

        let output = "";

        data.forEach(party => {
            output += party.name +
                " : " + party.votes + " votes<br>";
        });

        document.getElementById("results")
        .innerHTML = output;
    });
}