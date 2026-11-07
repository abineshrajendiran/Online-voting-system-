async function loadParties() {
  const res = await fetch("/api/parties");
  const parties = await res.json();
  const select = document.getElementById("partyId");
  parties.forEach(p => {
    const opt = document.createElement("option");
    opt.value = p.id;
    opt.textContent = p.id + " - " + p.name + " (" + p.code + ")";
    select.appendChild(opt);
  });
}

async function submitVote() {
  const body =
    "voterId=" + encodeURIComponent(document.getElementById("voterId").value) +
    "&name=" + encodeURIComponent(document.getElementById("name").value) +
    "&password=" + encodeURIComponent(document.getElementById("password").value) +
    "&partyId=" + encodeURIComponent(document.getElementById("partyId").value);

  const res = await fetch("/api/vote", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: body
  });
  const data = await res.json();
  const msg = document.getElementById("msg");
  msg.textContent = data.message;
  msg.className = data.ok ? "ok" : "err";
}

loadParties();