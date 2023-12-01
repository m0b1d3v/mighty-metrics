// Any time a submit button is clicked, show a loading indicator
document.querySelectorAll("input[type='submit']").forEach(button => {
	button.onclick = () => button.ariaBusy = "true";
});

// All delete buttons should ask the user to confirm
document.querySelectorAll("button[name='delete']").forEach(button => {
	button.onclick = () => {
		return confirm("Are you sure? This cannot be undone.");
	}
});

// Clear any invalid state if a user begins typing
document.querySelectorAll("input").forEach(input => {
	input.addEventListener("input", () => input.removeAttribute("aria-invalid"));
});

// If Javascript is enabled, hide the form submit button and upload automatically
// Don't forget that the first form on the page is a logout button
const fileLabel = document.getElementById("fileLabel");
const fileInput = document.getElementById("file");
const fileSubmit = document.getElementById("image");
if (fileLabel != null && fileInput != null && fileSubmit != null) {
	fileSubmit.hidden = true;
	fileInput.addEventListener("change", () => {
		fileLabel.ariaBusy = "true";
		fileSubmit.click();
	});
}
