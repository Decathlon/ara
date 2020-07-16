FROM mysql:5.7

ENV MYSQL_DATABASE=ara-dev
ENV MYSQL_USER=dev
ENV MYSQL_PASSWORD=dev
ENV MYSQL_ROOT_PASSWORD=to_change

CMD [ "--autocommit=OFF", \
      "--character-set-server=utf8", \
      "--sql_mode=STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION", \
      "--default_storage_engine=innodb" ]