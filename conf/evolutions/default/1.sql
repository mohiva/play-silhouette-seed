# schema

# --- !Ups

CREATE TABLE `user` (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NULL DEFAULT NULL,
    last_name VARCHAR(50) NULL DEFAULT NULL,
    date_of_birth DATE NULL DEFAULT NULL,
    email VARCHAR(100) NULL DEFAULT NULL,
    avatar_url VARCHAR(200) NULL DEFAULT NULL,
    activated BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP NULL DEFAULT NULL,
    modified TIMESTAMP NULL DEFAULT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE login_info (
    user_id BIGINT UNSIGNED NOT NULL,
    provider_id CHAR(36) NOT NULL,
    provider_key CHAR(36) NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    KEY idx_provider_id_key (provider_id, provider_key),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE auth_token (
    user_id BIGINT UNSIGNED NOT NULL,
    token_id CHAR(36) NOT NULL,
    expiry TIMESTAMP NOT NULL,
    KEY idx_token_id (token_id),
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE security_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_security_role (
    user_id BIGINT UNSIGNED NOT NULL,
    security_role_id BIGINT UNSIGNED NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (security_role_id) REFERENCES security_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, security_role_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO security_role (name) values ('user');
INSERT INTO security_role (name) values ('administrator');

CREATE TRIGGER user_after_insert AFTER INSERT ON `user` FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER user_trigger_after_update AFTER UPDATE ON `user` FOR EACH ROW SET @modified := CURRENT_TIME;

CREATE TRIGGER auth_token_before_insert BEFORE INSERT ON auth_token FOR EACH ROW SET new.token_id = UUID();

CREATE TRIGGER login_info_trigger_after_insert AFTER INSERT ON login_info FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER login_info_trigger_after_update AFTER UPDATE ON login_info FOR EACH ROW SET @modified := CURRENT_TIME;

CREATE TRIGGER user_security_role_after_insert AFTER INSERT ON user_security_role FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER user_security_role_after_update AFTER UPDATE ON user_security_role FOR EACH ROW SET @modified := CURRENT_TIME;

# --- !Downs

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE auth_token CASCADE;

DROP TABLE login_info CASCADE;

DROP TABLE `user` CASCADE;