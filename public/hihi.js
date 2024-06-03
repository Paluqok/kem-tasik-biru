document.addEventListener('DOMContentLoaded', () => {
    const createActivityButton = document.getElementById('createActivityButton');
    const createActivityContainer = document.getElementById('createActivityContainer');
    const activitiesContainer = document.getElementById('activitiesContainer');
  
    // Function to fetch and display all activities
    async function fetchActivities() {
      try {
        const response = await fetch('http://localhost:4000/activities');
        if (!response.ok) {
          throw new Error('Failed to fetch activities');
        }
        const activities = await response.json();
        activitiesContainer.innerHTML = '';
        activities.forEach(activity => {
          const activityCard = document.createElement('div');
          activityCard.classList.add('activity-card');
          activityCard.innerHTML = `
            <img src="${activity.activityImage}" alt="${activity.activityName}">
            <h3>${activity.activityName}</h3>
            <button onclick="viewActivity(${activity.activityID})">More</button>
          `;
          activitiesContainer.appendChild(activityCard);
        });
      } catch (error) {
        console.error('Error fetching activities:', error);
      }
    }
  
    // Function to create the activity form
    function createActivityForm() {
      return `
        <form id="createActivityForm">
          <label for="activityName">Activity Name:</label>
          <input type="text" id="activityName" name="activityName" required>
          
          <label for="activityLocation">Activity Location:</label>
          <input type="text" id="activityLocation" name="activityLocation" required>
  
          <label for="activityDuration">Activity Duration:</label>
          <input type="text" id="activityDuration" name="activityDuration" required>
  
          <label for="activityPrice">Activity Price:</label>
          <input type="number" id="activityPrice" name="activityPrice" required>
  
          <label for="activityImage">Activity Image (URL):</label>
          <input type="text" id="activityImage" name="activityImage" required>
          
          <button type="submit">Create</button>
        </form>
      `;
    }
  
    createActivityButton.addEventListener('click', () => {
      createActivityContainer.innerHTML = createActivityForm();
      const createActivityFormElement = document.getElementById('createActivityForm');
      createActivityFormElement.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData(createActivityFormElement);
        const activityData = {
          activityName: formData.get('activityName'),
          activityLocation: formData.get('activityLocation'),
          activityDuration: formData.get('activityDuration'),
          activityPrice: formData.get('activityPrice'),
          activityImage: formData.get('activityImage'),
        };
        try {
          const response = await fetch('http://localhost:4000/activities', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(activityData),
          });
          if (!response.ok) {
            throw new Error('Failed to create activity');
          }
          // Clear the form
          createActivityContainer.innerHTML = '';
          // Refresh the activities list
          fetchActivities();
        } catch (error) {
          console.error('Error creating activity:', error);
        }
      });
    });
  
    fetchActivities();
  });
  