FROM mysql:8.0

MAINTAINER imast.io
COPY cnf/port.cnf /etc/mysql/conf.d/ 

COPY init/* /docker-entrypoint-initdb.d/

EXPOSE 8810

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["mysqld"]
