const express = require('express');
const path = require('path');
const cors = require('cors');
const multer = require('multer');
const db = require('./db');

const app = express();

// Middleware to parse JSON bodies
app.use(express.json());

// Use CORS
app.use(cors());

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, 'public')));

// Configure Multer
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    cb(null, `${Date.now()}-${file.originalname}`);
  }
});
const upload = multer({ storage });

// Define a route to test the database connection
app.get('/time', async (req, res) => {
  try {
    const result = await db.query('SELECT NOW()');
    res.json(result.rows[0]);
  } catch (err) {
    console.error('Error in /time route:', err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to get all activities
app.get('/activities', async (req, res) => {
  try {
    const result = await db.query('SELECT * FROM public.activity');
    res.json(result.rows);
  } catch (err) {
    console.error('Error in /activities route:', err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to create a new activity
app.post('/activities', async (req, res) => {
  const client = await db.pool.connect();
  try {
    const { activityname, activitylocation, activityduration, activityprice , activityimage } = req.body;
    console.log('Received data:', req.body); // Log received data for debugging

    // Begin transaction
    await client.query('BEGIN');

    // Insert the new activity
    const insertActivityText = `
      INSERT INTO public.activity (activityname, activitylocation, activityduration, activityprice, activityimage)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING activityid;
    `;
    const insertActivityValues = [activityname, activitylocation, activityduration, activityprice, activityimage];
    const result = await client.query(insertActivityText, insertActivityValues);
    console.log('Insert result:', result.rows); // Log result for debugging

    // Commit transaction
    await client.query('COMMIT');

    res.status(201).json({ activityid: result.rows[0].activityid });
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error creating activity:', err); // Detailed error logging
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
    const result = await db.query('SELECT * FROM public.activity WHERE activityid = $1', [id]);
    res.json(result.rows[0]);
  } catch (err) {
    console.error('Error in /activities/:id route:', err);
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
      UPDATE public.activity
      SET activityname = $1,
          activitylocation = $2,
          activityduration = $3,
          activityprice = $4,
          activityimage = $5
      WHERE activityid = $6
    `;
    const updateActivityValues = [activityname, activitylocation, activityduration, activityprice, activityimage, id];
    await client.query(updateActivityText, updateActivityValues);

    // Commit transaction
    await client.query('COMMIT');

    res.status(200).send('Activity updated successfully');
  } catch (err) {
    // Rollback transaction in case of error
    await client.query('ROLLBACK');
    console.error('Error updating activity:', err); // Detailed error logging
    res.status(500).send('Something went wrong!');
  } finally {
    // Release the client back to the pool
    client.release();
  }
});

// Route to delete an activity
app.delete('/activities/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await db.query('DELETE FROM public.activity WHERE activityid = $1', [id]);
    res.sendStatus(204);
  } catch (err) {
    console.error('Error in /activities/:id route:', err);
    res.status(500).send('Something went wrong!');
  }
});

// Route to handle image uploads
app.post('/upload', upload.single('file'), (req, res) => {
  res.json({ filename: req.file.filename });
});

const PORT = process.env.PORT || 4000;

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
