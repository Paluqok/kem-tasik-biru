const { Pool } = require('pg');

const pool = new Pool({
  max: 10, // connection limit
  host: 'c9uss87s9bdb8n.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com',
  user: 'ua60mfps4cfmib',
  password: 'ktb1234',
  database: 'daobh2hbimne40',
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
