// All delete buttons should ask the user to confirm
document.querySelectorAll("input[name='delete']").forEach(button => {
	button.onclick = () => {
		return confirm("Are you sure? This cannot be undone.");
	}
});

// Clear any invalid state if a user begins typing
document.querySelectorAll("input").forEach(input => {
	input.addEventListener("input", () => {
		input.removeAttribute("aria-invalid");
	})
});

// Disable the image submit button until an image is selected
const fileInput = document.getElementById("file");
const fileSubmit = document.getElementById("image");
if (fileInput != null && fileSubmit != null) {
	fileSubmit.disabled = true;
	fileInput.addEventListener("change", () => {
		fileSubmit.disabled = fileInput.files?.length < 1;
	});
}
