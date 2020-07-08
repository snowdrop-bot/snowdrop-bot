CREATE TABLE IF NOT EXISTS bot_user (
  id INT,
  username VARCHAR(255) PRIMARY KEY,
  password VARCHAR(255),
  role VARCHAR(255)
);

INSERT INTO bot_user (id, username, password, role) VALUES (1, 'admin', 'admin', 'admin');