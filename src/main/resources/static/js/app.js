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
  const editDialog = document.getElementById("edit-tenant-dialog");
  const deleteDialog = document.getElementById("delete-tenant-dialog");
  const propertyId = document.querySelector(".tenant-panel")?.dataset.propertyId;

  document.querySelectorAll(".tenant-edit-trigger").forEach((button) => {
    button.addEventListener("click", () => {
      const id = button.dataset.tenantId;
      document.getElementById("edit-tenant-form").action = `/properties/${propertyId}/tenants/${id}/edit`;
      document.getElementById("edit-tenant-name").value = button.dataset.tenantName || "";
      document.getElementById("edit-tenant-phone").value = button.dataset.tenantPhone || "";
      document.getElementById("edit-tenant-email").value = button.dataset.tenantEmail || "";
      document.getElementById("edit-tenant-rent").value = button.dataset.tenantRent || "0.00";
      editDialog.showModal();
    });
  });

  document.querySelectorAll(".tenant-delete-trigger").forEach((button) => {
    button.addEventListener("click", () => {
      const id = button.dataset.tenantId;
      document.getElementById("delete-tenant-form").action = `/properties/${propertyId}/tenants/${id}/delete`;
      document.getElementById("delete-tenant-name").textContent = button.dataset.tenantName || "this tenant";
      deleteDialog.showModal();
    });
  });

  document.querySelectorAll("[data-close-dialog]").forEach((button) => {
    button.addEventListener("click", () => button.closest("dialog")?.close());
  });

  [editDialog, deleteDialog].forEach((dialog) => {
    dialog?.addEventListener("click", (event) => {
      if (event.target === dialog) dialog.close();
    });
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
