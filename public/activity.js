document.addEventListener('DOMContentLoaded', function() {
    const createActivityBtn = document.getElementById('createActivityBtn');
    const createActivityModal = document.getElementById('createActivityModal');
    const closeModalBtn = document.querySelector('.close');
    const activityList = document.getElementById('activityList');

    createActivityBtn.addEventListener('click', function() {
        createActivityModal.style.display = 'block';
    });

    closeModalBtn.addEventListener('click', function() {
        createActivityModal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
        if (event.target === createActivityModal) {
            createActivityModal.style.display = 'none';
        }
    });

    // Function to fetch activities from the server and display them
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
        image.src = `data:image/jpeg;base64,${activity.activityImage.toString('base64')}`;
        card.appendChild(image);

        const name = document.createElement('h3');
        name.textContent = activity.activityName;
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

    // Fetch activities when the page is loaded
    fetchActivities();
});
