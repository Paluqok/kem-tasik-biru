document.getElementById('showFormButton').addEventListener('click', function() {
    document.getElementById('showFormButton').style.display = 'none';
    document.getElementById('formContainer').classList.remove('hidden');
});

document.getElementById('closeForm').addEventListener('click', function() {
    document.getElementById('formContainer').classList.add('hidden');
    document.getElementById('showFormButton').style.display = 'inline-block';
});

document.getElementById('activityForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent form submission for demonstration

    // Form handling logic goes here (e.g., AJAX request to server)

    // After form is handled, go back to the previous state
    document.getElementById('formContainer').classList.add('hidden');
    document.getElementById('showFormButton').style.display = 'inline-block';
});
