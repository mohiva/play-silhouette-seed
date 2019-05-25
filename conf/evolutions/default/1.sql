# schema

# --- !Ups

CREATE TABLE `user` (
    id INT AUTO_INCREMENT NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(200) NOT NULL,
    activated BOOLEAN NOT NULL DEFAULT FALSE,
    last_login TIMESTAMP NULL DEFAULT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE login_info (
    id INT AUTO_INCREMENT NOT NULL AUTO_INCREMENT,
    provider_id VARCHAR(100) NOT NULL,
    provider_key VARCHAR(50) NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    KEY idx_provider_id (provider_id),
    KEY idx_provider_key (provider_key),
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_login_info (
    user_id INT NOT NULL,
    login_info_id INT NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (login_info_id) REFERENCES login_info(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, login_info_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE security_role (
    id INT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_security_role (
    user_id INT NOT NULL,
    security_role_id INT NOT NULL,
    modified TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (security_role_id) REFERENCES security_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, security_role_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO security_role (name) values ('user');
INSERT INTO security_role (name) values ('administrator');

# --- !Downs

DROP TABLE user_login_info CASCADE;

DROP TABLE login_info CASCADE;

DROP TABLE user_security_role CASCADE;

DROP TABLE security_role CASCADE;

DROP TABLE `user` CASCADE;