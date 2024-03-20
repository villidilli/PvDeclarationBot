DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS declarations;

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

CREATE TABLE IF NOT EXISTS declarations (
    number_declaration BIGINT UNIQUE PRIMARY KEY,
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    path VARCHAR(255) UNIQUE NOT NULL
);

INSERT into declarations (number_declaration, start_date, due_date, path)
VALUES ('111', '1990-10-10', '1991-11-11', 'C:\Users\Usserss\YandexDisk\Документы\Прочее\!!! Декл.соответствия\ПВ\Koliate.Ананасы(02.2023).pdf');

CREATE TABLE IF NOT EXISTS products (
    erp_id BIGINT UNIQUE PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    industrial_site VARCHAR(255) NOT NULL,
    product_group1 VARCHAR(255) NOT NULL,
    product_group2 VARCHAR(255),
    product_group3 VARCHAR(255),
    declaration BIGINT REFERENCES declarations(number_declaration) ON DELETE CASCADE
);

INSERT INTO products (erp_id, product_name, industrial_site, product_group1, product_group2, product_group3, declaration)
VALUES ('2769', 'Ананасы кольца 580мл ж/б', 'KOLIATE CO., LTD', 'Ананасы', '','','111');