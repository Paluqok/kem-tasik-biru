// detail-activity.js

document.addEventListener('DOMContentLoaded', function() {
    const activityDetail = document.getElementById('activityDetail');
    const updateActivityForm = document.getElementById('updateActivityForm');
    const deleteActivityForm = document.getElementById('deleteActivityForm');

    // Function to display activity details
    function displayActivityDetails(activity) {
        // Clear previous details
        activityDetail.innerHTML = '';

        // Create elements to display activity details
        const name = document.createElement('h2');
        name.textContent = activity.activityname;
        activityDetail.appendChild(name);

        const location = document.createElement('p');
        location.textContent = 'Location: ' + activity.activitylocation;
        activityDetail.appendChild(location);

        const duration = document.createElement('p');
        duration.textContent = 'Duration: ' + activity.activityduration;
        activityDetail.appendChild(duration);

        const price = document.createElement('p');
        price.textContent = 'Price: RM' + activity.activityprice;
        activityDetail.appendChild(price);

        // Create image element
        const image = document.createElement('img');
        image.src = activity.activityimage; // Assuming activityimage is the URL of the image
        image.alt = 'Activity Image';
        activityDetail.appendChild(image);

        // Create update button
        const updateBtn = document.createElement('button');
        updateBtn.textContent = 'Update';
        updateBtn.addEventListener('click', function() {
            // Show update form
            updateActivityForm.style.display = 'block';
            // Populate update form with current activity data
            populateUpdateForm(activity);
        });
        activityDetail.appendChild(updateBtn);

        // Create delete button
        const deleteBtn = document.createElement('button');
        deleteBtn.textContent = 'Delete';
        deleteBtn.addEventListener('click', function() {
            // Show delete confirmation form
            deleteActivityForm.style.display = 'block';
        });
        activityDetail.appendChild(deleteBtn);
    }

    // Function to populate update form with current activity data
    function populateUpdateForm(activity) {
        // Populate form fields with current activity data
        document.getElementById('updateActivityName').value = activity.activityname;
        document.getElementById('updateActivityLocation').value = activity.activitylocation;
        document.getElementById('updateActivityDuration').value = activity.activityduration;
        document.getElementById('updateActivityPrice').value = activity.activityprice;
    }

    // Fetch activity details when the page is loaded
    async function fetchActivityDetails(activityid) {
        try {
            const response = await fetch(`/activities/${activityid}`);
            const activity = await response.json();
            displayActivityDetails(activity);
        } catch (error) {
            console.error('Error fetching activity details:', error);
        }
    }

    // Fetch activity details based on activity ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    const activityid = urlParams.get('id');
    if (activityid) {
        fetchActivityDetails(activityid);
    } else {
        console.error('Activity ID not found in URL');
    }

    // Close update form when close button is clicked
    const closeUpdateBtn = document.querySelector('#updateActivityForm .close');
    closeUpdateBtn.addEventListener('click', function() {
        updateActivityForm.style.display = 'none';
    });

    // Close delete confirmation form when close button is clicked
    const closeDeleteBtn = document.querySelector('#deleteActivityForm .close');
    closeDeleteBtn.addEventListener('click', function() {
        deleteActivityForm.style.display = 'none';
    });

    // Handle update form submission
    const updateActivityFormElement = document.querySelector('#updateActivityForm form');
    updateActivityFormElement.addEventListener('submit', async function(event) {
        event.preventDefault();
        const formData = new FormData(updateActivityFormElement);
        const updatedActivityData = {
            activityname: formData.get('activityname'),
            activitylocation: formData.get('activitylocation'),
            activityduration: formData.get('activityduration'),
            activityprice: formData.get('activityprice'),
            activityimage: formData.get('updatedActivityImage')
        };
        try {
            const response = await fetch(`/activities/${activityid}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updatedActivityData)
            });
            if (response.ok) {
                console.log('Activity updated successfully');
                // Update displayed activity details
                fetchActivityDetails(activityid);
            } else {
                console.error('Failed to update activity:', response.statusText);
            }
        } catch (error) {
            console.error('Error updating activity:', error);
        }
        // Close update form after submission
        updateActivityForm.style.display = 'none';
    });

    // Handle delete confirmation form submission
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    confirmDeleteBtn.addEventListener('click', async function() {
        const confirmation = confirm('Are you sure you want to delete this activity?');
        if (confirmation) {
            try {
                const response = await fetch(`/activities/${activityId}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    console.log('Activity deleted successfully');
                    // Redirect to activity list page
                    window.location.href = '/activity.html';
                } else {
                    console.error('Failed to delete activity:', response.statusText);
                }
            } catch (error) {
                console.error('Error deleting activity:', error);
            }
        }
        deleteActivityForm.style.display = 'none';
    });
});
