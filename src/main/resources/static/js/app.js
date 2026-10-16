(() => {
  const tokenMeta = document.querySelector('meta[name="csrf-token"]');
  const token = tokenMeta ? tokenMeta.content : "";
  document
    .querySelectorAll('.tenant-row[draggable="true"]')
    .forEach((tenant) => {
      tenant.addEventListener("dragstart", (event) => {
        event.dataTransfer.setData("text/plain", tenant.dataset.tenantId);
        event.dataTransfer.effectAllowed = "move";
        tenant.classList.add("dragging");
      });
      tenant.addEventListener("dragend", () =>
        tenant.classList.remove("dragging"),
      );
    });
  document.querySelectorAll(".room-card").forEach((room) => {
    room.addEventListener("dragover", (event) => {
      event.preventDefault();
      if (!room.classList.contains("full")) room.classList.add("drag-target");
      event.dataTransfer.dropEffect = room.classList.contains("full")
        ? "none"
        : "move";
    });
    room.addEventListener("dragleave", (event) => {
      if (!room.contains(event.relatedTarget))
        room.classList.remove("drag-target");
    });
    room.addEventListener("drop", (event) => {
      event.preventDefault();
      room.classList.remove("drag-target");
      if (room.classList.contains("full")) {
        window.alert(
          "This room is full. Choose a room with an available space.",
        );
        return;
      }
      const tenantId = event.dataTransfer.getData("text/plain");
      if (!tenantId) return;
      const form = room.querySelector(".drop-assignment-form");
      if (!form) return;
      form.querySelector('input[name="tenantId"]').value = tenantId;
      if (token) {
        let csrf = form.querySelector('input[name="_csrf"]');
        if (!csrf) {
          csrf = document.createElement("input");
          csrf.type = "hidden";
          csrf.name = "_csrf";
          form.appendChild(csrf);
        }
        csrf.value = token;
      }
      form.requestSubmit();
    });
  });
})();
