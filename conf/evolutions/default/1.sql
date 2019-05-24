# schema

# --- !Ups

CREATE TABLE security_role (
    myid INT AUTO_INCREMENT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (myid)
);

# --- !Downs

DROP TABLE security_role CASCADE;