DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS checks;
DROP TABLE IF EXISTS messages;

CREATE TABLE users (id VARCHAR(64) PRIMARY KEY, name VARCHAR(256), email VARCHAR(256), username VARCHAR(64), password VARCHAR(64), is_admin BOOLEAN, UNIQUE (email), UNIQUE (username));
CREATE TABLE accounts (id VARCHAR(64) PRIMARY KEY, amount NUMERIC(12,4), number INTEGER, owner_id VARCHAR(64));
CREATE TABLE checks (id VARCHAR(64) PRIMARY KEY, amount NUMERIC(12,4), number VARCHAR(16), account_id VARCHAR(64));
CREATE TABLE messages (id VARCHAR(62) PRIMARY KEY, name VARCHAR(256), email VARCHAR(256), subject VARCHAR(128), message VARCHAR(2048));

CREATE UNIQUE INDEX users_password_idx ON users (username, password);

INSERT INTO users (id, name, email, username, password, is_admin) VALUES (1, 'John Coltraine', 'john@coltraine.com', 'john.coltraine', 'j0hn', 'false');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (2, 'Upton Sinclair', 'upton@sinclair.com', 'upton.sinclair', 'upt0n', 'false');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (3, 'Admin Admin', 'admin@terracottabank.com', 'admin', 'admin', 'true');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (4, 'Josh Cummings', 'josh@cummings.com', 'josh.cummings', 'j0sh', 'false');

INSERT INTO accounts (id, amount, number, owner_id) VALUES (1, 2500, 987654321, 1);
INSERT INTO accounts (id, amount, number, owner_id) VALUES (2, 25, 987654322, 2);
INSERT INTO accounts (id, amount, number, owner_id) VALUES (4, 12, 987654323, 4);

INSERT INTO messages (id, name, email, subject, message) VALUES (1, '<script>alert("contact name is vulnerable to reflected xss")</script>', '<script>alert("contact email is vulnerable to reflected xss")</script>', '<script>alert("message subject is vulnerable to reflected xss")</script>', '<script>alert("message content is vulnerable to reflected xss")</script>');
	