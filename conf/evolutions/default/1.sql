# schema

# --- !Ups

CREATE TABLE `user` (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(200) NOT NULL,
    activated BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP NULL DEFAULT NULL,
    modified TIMESTAMP NULL DEFAULT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE auth_token (
    token_id BINARY(16) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expiry TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE login_info (
    id SERIAL PRIMARY KEY,
    provider_id BINARY(16) NOT NULL,
    provider_key BINARY(16) NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    KEY idx_provider_id (provider_id),
    KEY idx_provider_key (provider_key)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_login_info (
    user_id BIGINT NOT NULL,
    login_info_id BIGINT NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (login_info_id) REFERENCES login_info(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, login_info_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE security_role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_security_role (
    user_id BIGINT NOT NULL,
    security_role_id BIGINT NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (security_role_id) REFERENCES security_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, security_role_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO security_role (name) values ('user');
INSERT INTO security_role (name) values ('administrator');

CREATE TRIGGER user_after_insert AFTER INSERT ON `user` FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER user_trigger_after_update AFTER UPDATE ON `user` FOR EACH ROW SET @modified := CURRENT_TIME;

CREATE TRIGGER auth_token_before_insert BEFORE INSERT ON auth_token FOR EACH ROW
BEGIN
    IF new.token_id IS NULL THEN
        SET new.token_id = UUID();
    END IF;
END;

CREATE TRIGGER login_info_before_insert BEFORE INSERT ON login_info FOR EACH ROW
BEGIN
    IF new.provider_id IS NULL THEN
        SET new.provider_id = UUID();
    END IF;
    IF new.provider_key IS NULL THEN
        SET new.provider_key = UUID();
    END IF;
END;

CREATE TRIGGER login_info_trigger_after_insert AFTER INSERT ON login_info FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER login_info_trigger_after_update AFTER UPDATE ON login_info FOR EACH ROW SET @modified := CURRENT_TIME;

CREATE TRIGGER user_login_info_after_insert AFTER INSERT ON user_login_info FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER user_login_info_after_update AFTER UPDATE ON user_login_info FOR EACH ROW SET @modified := CURRENT_TIME;

CREATE TRIGGER user_security_role_after_insert AFTER INSERT ON user_security_role FOR EACH ROW SET @modified := CURRENT_TIME;
CREATE TRIGGER user_security_role_after_update AFTER UPDATE ON user_security_role FOR EACH ROW SET @modified := CURRENT_TIME;

# --- !Downs

DROP TABLE user_login_info CASCADE;

DROP TABLE login_info CASCADE;

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE auth_token CASCADE;

DROP TABLE `user` CASCADE;