const { Pool } = require('pg');
const pool = new Pool({
  connectionString: "postgres://ua60mfps4cfmib:pf3b00b75fdf5338b311909dafbb5e86b1bf77fad7e2c5f7a5c231312887e38ce@c9uss87s9bdb8n.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com:5432/daobh2hbimne40",
  ssl: {
    rejectUnauthorized: false
  }
});

module.exports = {
  query: (text, params) => pool.query(text, params),
};