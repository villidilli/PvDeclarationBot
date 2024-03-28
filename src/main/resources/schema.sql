DROP TABLE IF EXISTS chats;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) UNIQUE PRIMARY KEY,
    partner_name VARCHAR(255)
);

INSERT into users (user_name, user_email, partner_name)
VALUES ('Evgenii', 'ekuznecov@ecln.ru', 'ECOLAND'),
       ('Alexandr', 'akovalev@vkirzhanov.ru', 'ECOLAND');

CREATE TABLE IF NOT EXISTS chats (
    chat_id_telegram BIGINT UNIQUE PRIMARY KEY,
    user_email VARCHAR(255) REFERENCES users(user_email) ON DELETE CASCADE NOT NULL,
    verified BOOLEAN DEFAULT false
);

CREATE TABLE IF NOT EXISTS declarations (
    number_declaration VARCHAR(255) UNIQUE PRIMARY KEY,
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    file_name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS label_mockups (
    mockup_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    file_name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    erp_id BIGINT UNIQUE PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    industrial_site VARCHAR(255) NOT NULL,
    product_group1 VARCHAR(255) NOT NULL,
    product_group2 VARCHAR(255),
    product_group3 VARCHAR(255),
    barcode VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS product_label_mockup (
    product_erp_id BIGINT REFERENCES products(erp_id) ON DELETE CASCADE,
    mockup_id BIGINT REFERENCES label_mockups(mockup_id) ON DELETE CASCADE,
    PRIMARY KEY (product_erp_id, mockup_id)
);

CREATE TABLE IF NOT EXISTS product_declaration (
    product_erp_id BIGINT REFERENCES products(erp_id) ON DELETE CASCADE,
    num_declaration VARCHAR(255) REFERENCES declarations(number_declaration) ON DELETE CASCADE,
    PRIMARY KEY (product_erp_id, num_declaration)
);