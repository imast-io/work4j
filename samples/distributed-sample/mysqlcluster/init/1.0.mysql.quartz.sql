CREATE DATABASE quartz_scheduler;

USE quartz_scheduler;

CREATE USER 'username'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON quartz_scheduler.* TO 'username'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


