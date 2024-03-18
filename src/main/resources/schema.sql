DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) UNIQUE PRIMARY KEY,
    partner_name VARCHAR(255)
);

INSERT into users (user_name, user_email, partner_name)
VALUES ('Evgenii', 'ekuznecov@ecln.ru', 'ECOLAND');

CREATE TABLE IF NOT EXISTS chats (
    chat_id_telegram BIGINT UNIQUE PRIMARY KEY,
    user_email VARCHAR(255) REFERENCES users(user_email) ON DELETE CASCADE NOT NULL,
    verified BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS products (
    erp_id BIGINT UNIQUE PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    industrial_site VARCHAR(255) NOT NULL,
    url VARCHAR(255) UNIQUE NOT NULL,
    end_of_term date NOT NULL
);