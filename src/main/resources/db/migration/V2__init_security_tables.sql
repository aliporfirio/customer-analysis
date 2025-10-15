CREATE TABLE roles (
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   username VARCHAR(50) UNIQUE NOT NULL,
   password VARCHAR(255) NOT NULL
);

CREATE TABLE users_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY(user_id, role_id)
);

INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_OPERATOR');

INSERT INTO users (username, password) VALUES ('admin', '$2a$10$DuvM3te/EJc8so7tp819BuWohvxRo8hfnGfodPgqJOm0TAd6Vm3O.');
INSERT INTO users (username, password) VALUES ('operator', '$2a$10$L59xxCU/n.iuA2/f49L7luPQXzpMP.TvkeC1iAhTUnkv8rL4KmQOy');

INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);
