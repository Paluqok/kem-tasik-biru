document.addEventListener('DOMContentLoaded', function() {
    const createActivityBtn = document.getElementById('createActivityBtn');
    const createActivityModal = document.getElementById('createActivityModal');
    const activityList = document.getElementById('activityList');

    createActivityBtn.addEventListener('click', function() {
        console.log('Create Activity button clicked');
        createActivityModal.style.display = 'block'; // Display the modal
    });

    // Close modal when close button or outside modal is clicked
    const closeModalBtn = document.querySelector('.close');
    closeModalBtn.addEventListener('click', function() {
        console.log('Close button clicked');
        createActivityModal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
        if (event.target === createActivityModal) {
            console.log('Outside modal clicked');
            createActivityModal.style.display = 'none';
        }
    });

    // Form submission
    const createActivityForm = document.getElementById('createActivityForm');
    createActivityForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        console.log('Form submitted');

        const formData = new FormData(createActivityForm);

        try {
            const response = await fetch('/createActivities', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                console.log('Activity created successfully');
                createActivityModal.style.display = 'none';
                location.reload(); // Reload to fetch the new activities
            } else {
                console.error('Failed to create activity', response.statusText);
            }
        } catch (error) {
            console.error('Error creating activity:', error);
        }

        createActivityModal.style.display = 'none'; // Close the modal after submission
        createActivityForm.reset(); // Reset form fields if needed
    });

});
