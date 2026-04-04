CREATE TABLE roles(
                      id BIGINT GENERATED ALWAYS AS IDENTITY,
                      name VARCHAR(50) UNIQUE NOT NULL,
                      CONSTRAINT pk_roles PRIMARY KEY (id)
);
INSERT INTO roles(name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles(name) VALUES ('CLIENT') ON CONFLICT (name) DO NOTHING;

CREATE TABLE users(
                      id BIGINT GENERATED ALWAYS AS IDENTITY,
                      email VARCHAR(128) UNIQUE NOT NULL,
                      password VARCHAR(256) NOT NULL,
                      disabled BOOLEAN NOT NULL,
                      CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE user_roles(
                           id_user BIGINT NOT NULL,
                           id_role BIGINT NOT NULL,
                           CONSTRAINT pk_user_roles PRIMARY KEY (id_user, id_role),
                           CONSTRAINT fk_user_roles_role FOREIGN KEY (id_role) REFERENCES roles(id) ON DELETE CASCADE,
                           CONSTRAINT fk_user_roles_user FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE
);