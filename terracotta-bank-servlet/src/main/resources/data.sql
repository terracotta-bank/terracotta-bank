DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS checks;
DROP TABLE IF EXISTS messages;

CREATE TABLE users (id VARCHAR(64) PRIMARY KEY, name VARCHAR(256), email VARCHAR(256), username VARCHAR(64), password VARCHAR(64), is_admin BOOLEAN, UNIQUE (email), UNIQUE (username));
CREATE TABLE accounts (id VARCHAR(64) PRIMARY KEY, amount NUMERIC(12,4), number INTEGER, owner_id VARCHAR(64));
CREATE TABLE checks (id VARCHAR(64) PRIMARY KEY, amount NUMERIC(12,4), number VARCHAR(16), account_id VARCHAR(64));
CREATE TABLE messages (id VARCHAR(62) PRIMARY KEY, name VARCHAR(256), email VARCHAR(256), subject VARCHAR(128), message VARCHAR(2048));

CREATE UNIQUE INDEX users_password_idx ON users (username, password);

INSERT INTO users (id, name, email, username, password, is_admin) VALUES (1, 'John Coltrane', 'john@coltrane.com', 'john.coltrane', '$2a$10$0n82eYrU5JLEOJvdJLm/F.yOtcAi7AtH1Ip038uyLZ7chjwskyKVG', 'false');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (2, 'Upton Sinclair', 'upton@sinclair.com', 'upton.sinclair', '$2a$10$1O4F6aH5ppDT2Xsmx0YvkuBneBc0AEvMJ11p3GC7SSTsKarWyup1.', 'false');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (3, 'Admin Admin', 'admin@terracottabank.com', 'admin', '$2a$10$FtmcY0r9DnCPplqTwiXvweejzpNohblOfqB9zR.xkgx///bZwnzOS', 'true');
INSERT INTO users (id, name, email, username, password, is_admin) VALUES (4, 'Josh Cummings', 'josh@cummings.com', 'josh.cummings', '$2a$10$peQccycZm7uwmWYci4rrpuBnHdAY1a5SYhjlhudGM8j7i2KeGiyo2', 'false');

INSERT INTO accounts (id, amount, number, owner_id) VALUES (1, 2500, 987654321, 1);
INSERT INTO accounts (id, amount, number, owner_id) VALUES (2, 25, 987654322, 2);
INSERT INTO accounts (id, amount, number, owner_id) VALUES (4, 12, 987654323, 4);

INSERT INTO messages (id, name, email, subject, message) VALUES (1, '<script>alert("contact name is vulnerable to reflected xss")</script>', '<script>alert("contact email is vulnerable to reflected xss")</script>', '<script>alert("message subject is vulnerable to reflected xss")</script>', '<script>alert("message content is vulnerable to reflected xss")</script>');
	