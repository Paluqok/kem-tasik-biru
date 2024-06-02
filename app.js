const express = require('express');
const path = require('path');
const db = require('./db'); // Import the database configuration

const app = express();

// Middleware to parse JSON bodies
app.use(express.json());

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

// Define a route to test the database connection
app.get('/time', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM ACTIVITY');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to get all activities
app.get('/activities', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM Activity');
    res.json(result.rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to create a new activity
app.post('/activities', async (req, res) => {
  const client = await db.pool.connect();
  try {
    const { activityName, activityLocation, activityDuration, activityPrice, activityImage } = req.body;

    // Begin transaction
    await client.query('BEGIN');

    // Insert the new activity
    const insertActivityText = `
      INSERT INTO Activity (activityName, activityLocation, activityDuration, activityPrice, activityImage)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING activityID;
    `;
    const insertActivityValues = [activityName, activityLocation, activityDuration, activityPrice, activityImage];
    const result = await client.query(insertActivityText, insertActivityValues);

    // Commit transaction
    await client.query('COMMIT');

    res.status(201).json({ activityID: result.rows[0].activityID });
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error creating activity:', err);
    res.status(500).send('Something went wrong!');
  } finally {
    // Release the client back to the pool
    client.release();
  }
});

// Route to get activity details
app.get('/activities/:id', async (req, res) => {
  const { id } = req.params;
  try {
    const result = await db.query('SELECT * FROM Activity WHERE activityID = $1', [id]);
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to update an activity
app.put('/activities/:id', async (req, res) => {
  const client = await db.pool.connect();
  const { id } = req.params;
  const { activityName, activityLocation, activityDuration, activityPrice, activityImage } = req.body;
  try {
    // Begin transaction
    await client.query('BEGIN');

    // Update the activity
    const updateActivityText = `
      UPDATE Activity
      SET activityName = $1, activityLocation = $2, activityDuration = $3, activityPrice = $4, activityImage = $5
      WHERE activityID = $6;
    `;
    const updateActivityValues = [activityName, activityLocation, activityDuration, activityPrice, activityImage, id];
    await client.query(updateActivityText, updateActivityValues);

    // Commit transaction
    await client.query('COMMIT');

    res.status(200).send('Activity updated successfully');
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error updating activity:', err);
    res.status(500).send('Something went wrong!');
  } finally {
    // Release the client back to the pool
    client.release();
  }
});

// Route to delete an activity
app.delete('/activities/:id', async (req, res) => {
  const client = await db.pool.connect();
  const { id } = req.params;
  try {
    // Begin transaction
    await client.query('BEGIN');

    // Delete the activity
    await client.query('DELETE FROM Activity WHERE activityID = $1', [id]);

    // Commit transaction
    await client.query('COMMIT');

    res.status(200).send('Activity deleted successfully');
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error deleting activity:', err);
    res.status(500).send('Something went wrong!');
  } finally {
    // Release the client back to the pool
    client.release();
  }
});

const PORT = process.env.PORT || 4000; // Use the PORT environment variable or default to 3000

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
