SELECT 1;

CREATE TABLE account(
   user_id serial PRIMARY KEY,
   username VARCHAR (50) UNIQUE NOT NULL
);

INSERT INTO account values ('1', 'ariel');