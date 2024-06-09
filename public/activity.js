document.addEventListener('DOMContentLoaded', function() {
    const createActivityBtn = document.getElementById('createActivityBtn');
    const createActivityModal = document.getElementById('createActivityModal');
    const closeModalBtn = document.querySelector('.close');
    const activityList = document.getElementById('activityList');

    createActivityBtn.addEventListener('click', function() {
        createActivityModal.style.display = 'block';
        createActivityModal.innerHTML = createActivityForm();
    });

    closeModalBtn.addEventListener('click', function() {
        createActivityModal.style.display = 'none';
    });

    window.addEventListener('click', function(event) {
        if (event.target === createActivityModal) {
            createActivityModal.style.display = 'none';
        }
    });

    // Function to create activity form dynamically
    function createActivityForm() {
        return `
            <form id="createActivityForm" style="background-color: #4CAF50; padding: 20px; border-radius: 10px;">
                <label for="activityname" style="color: white;">Activity Name:</label>
                <input type="text" id="activityname" name="activityname" required style="margin-bottom: 10px;">
    
                <label for="activitylocation" style="color: white;">Location:</label>
                <input type="text" id="activitylocation" name="activitylocation" required style="margin-bottom: 10px;">
    
                <label for="activityduration" style="color: white;">Duration:</label>
                <input type="text" id="activityduration" name="activityduration" required style="margin-bottom: 10px;">
    
                <label for="activityprice" style="color: white;">Price:</label>
                <input type="number" id="activityprice" name="activityprice" required style="margin-bottom: 10px;">
    
                <label for="activityimage" style="color: white;">Image:</label>
                <input type="file" id="activityimage" name="activityimage" accept="image/*" required style="margin-bottom: 20px;">
    
                <button type="submit" style="background-color: white; color: #4CAF50; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer;">Create</button>
            </form>
        `;
    }

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

    // Fetch activities when the page is loaded
    fetchActivities();
});
