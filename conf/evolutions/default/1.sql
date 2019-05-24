# schema

# --- !Ups

CREATE TABLE security_role (
    id INT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE IF EXISTS security_role CASCADE;