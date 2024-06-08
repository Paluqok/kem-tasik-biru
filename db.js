const { Pool } = require('pg');

const pool = new Pool({
  max: 10, // connection limit
  host: 'localhost',
  user: 'postgres',
  password: 'ktb1234',
  database: 'booking_system',
  port: 5432, // default PostgreSQL port
  ssl: {
    rejectUnauthorized: false
  }
});

pool.connect((err, client, release) => {
  if (err) {
    console.error('Error connecting to the database:', err);
  } else {
    console.log('Connected to the database');
    release(); // Release the client back to the pool
  }
});

module.exports = {
  query: (text, params) => pool.query(text, params),
};
