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
        const activityData = {
            activityname: formData.get('activityname'),
            activitylocation: formData.get('activitylocation'),
            activityduration: formData.get('activityduration'),
            activityprice: formData.get('activityprice'),
            activityimage: formData.get('activityimage')
        };

        try {
            const response = await fetch('/activities', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(activityData)
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

    // Fetch activities when the page is loaded
    async function fetchActivities() {
        try {
            const response = await fetch('/activities');
            const activities = await response.json();
            activities.forEach(activity => {
                const card = createActivityCard(activity);
                activityList.appendChild(card);
            });
        } catch (error) {
            console.error('Error fetching activities:', error);
        }
    }

    // Function to create activity card
    function createActivityCard(activity) {
        const card = document.createElement('div');
        card.classList.add('activity-card');
        
        const image = document.createElement('img');
        image.src = `data:image/jpeg;base64,${activity.activityimage.toString('base64')}`;
        card.appendChild(image);

        const name = document.createElement('h3');
        name.textContent = activity.activityname;
        card.appendChild(name);

        const moreBtn = document.createElement('button');
        moreBtn.textContent = 'More';
        moreBtn.addEventListener('click', function() {
            // Redirect to activity detail page or show more details
            // You can implement this functionality as per your requirement
        });
        card.appendChild(moreBtn);

        return card;
    }

    fetchActivities(); // Fetch activities when the page is loaded
});
