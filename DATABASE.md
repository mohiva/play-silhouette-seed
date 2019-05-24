## Create/drop MySQL App database in Linux with:

`sudo mysql -u root -e "create database myappdb"`

`sudo mysql -u root -e "drop database myappdb"`

## Create/drop MySQL Test database in Linux with:

`sudo mysql -u root -e "create database mytestdb"`

`sudo mysql -u root -e "drop database mytestdb"`

## Create MySQL user

GRANT ALL PRIVILEGES ON *.* TO 'dev'@'localhost' IDENTIFIED BY '12345';