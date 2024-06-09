const express = require('express');
const path = require('path');
const cors = require('cors'); // Import the CORS middleware
const db = require('./db'); // Import the database configuration

const app = express();

// Middleware to parse JSON bodies
app.use(express.json());

// Use CORS
app.use(cors());

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

// Define a route to test the database connection
app.get('/time', async (req, res) => {
  try {
    const result = await db.query('SELECT NOW()');
    res.json(result.rows[0]);
  } catch (err) {
    console.error(err);
    res.status(500).send('Something went wrong!');
  }
});

app.get('/create-customer', (req, res) => {
  res.sendFile(path.join(__dirname, 'create-customer.html'));
});

app.post('/create-customer', async (req, res) => {
  const { custname, custaddress, custemail, custphone, custpassword } = req.body;
  try {
      const result = await pool.query(
          'INSERT INTO public.customer (custname, custaddress, custemail, custphone, custpassword) VALUES ($1, $2, $3, $4, $5) RETURNING *',
          [custname, custaddress, custemail, custphone, custpassword]
      );
      res.status(201).send('Customer created successfully');
  } catch (err) {
      console.error(err.message);
      res.status(500).send('Server error');
  }
});

// Route to get all activities
app.get('/activities', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM activity');
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
    const { activityname, activitylocation, activityduration, activityprice, activityimage } = req.body;

    // Begin transaction
    await client.query('BEGIN');

    // Insert the new activity
    const insertActivityText = `
      INSERT INTO activity (activityname, activitylocation, activityduration, activityprice, activityimage)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING activityid;
    `;
    const insertActivityValues = [activityname, activitylocation, activityduration, activityprice, activityimage];
    const result = await client.query(insertActivityText, insertActivityValues);

    // Commit transaction
    await client.query('COMMIT');

    res.status(201).json({ activityid: result.rows[0].activityid });
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
    const result = await db.query('SELECT * FROM activity WHERE activityid = $1', [id]);
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
  const { activityname, activitylocation, activityduration, activityprice, activityimage } = req.body;
  try {
    // Begin transaction
    await client.query('BEGIN');

    // Update the activity
    const updateActivityText = `
      UPDATE activity
      SET activityname = $1, activitylocation = $2, activityduration = $3, activityprice = $4, activityimage = $5
      WHERE activityid = $6;
    `;
    const updateActivityValues = [activityname, activitylocation, activityduration, activityprice, activityimage, id];
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
    await client.query('DELETE FROM activity WHERE activityid = $1', [id]);

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

const PORT = process.env.PORT || 4000; // Use the PORT environment variable or default to 4000

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
