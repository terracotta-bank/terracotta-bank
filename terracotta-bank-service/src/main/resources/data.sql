DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (id VARCHAR(64) PRIMARY KEY, amount NUMERIC(12,4), number INTEGER, owner_id VARCHAR(64));

INSERT INTO accounts (id, amount, number, owner_id) VALUES (0, 25, 0, 0);