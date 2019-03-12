FROM mariadb:5.5.56

ENV MYSQL_DATABASE=ara
ENV MYSQL_USER=root
ENV MYSQL_ROOT_PASSWORD=V*n6MxBq7mr?4P?M

ENTRYPOINT [ "docker-entrypoint.sh", \
             "--autocommit=OFF", \
             "--character-set-server=utf8", \
             "--sql_mode=STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION", \
             "--default_storage_engine=innodb", \
             "--innodb_file_format=BARRACUDA", \
             "--innodb_file_format_max=BARRACUDA", \
             "--innodb_log_file_size=50M", \
             "--innodb_buffer_pool_size=2033M", \
             "--innodb_large_prefix", \
             "--innodb_file_per_table", \
             # Asynchronous I/O not supported by mounted Windows directories
             "--innodb-use-native-aio=0", \
             "--innodb-flush-method=fsync" ]
