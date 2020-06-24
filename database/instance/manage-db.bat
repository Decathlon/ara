@echo off

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: manage-db.bat
::
:: This script handles the ARA database container.
:: Feel free to change the CONFIGURATION (below) to meet your installation requirement.
::
:: You can use this script with the following commands : 
:: - create : Pull the mysql db, run the image and create the db's structure
:: - destroy : Destroy the container built by the image but leave the data pristine.
:: - start : Start the database container.
:: - stop : Stop the database container.
:: - mysqladmin : Run the `mysql` command in the container in interactive mode.
:: - shell : Run an interactive shell in the container.
:: - purge : Destroy the container, and remove the data persisted in the host.
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: CONFIGURATION
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Flag to set to 1 to enable the logging of the actual commands used.
set logs=1
:: The name of the container which will holds the db to manipulate
set container_name=ara-db
:: The database password (it is highly recommended to change it).
set password=dev_password_to_change
:: The container port exposed to the host.
set port=3306


if %1 == create (
	call :create_database %2
	exit /B
)

if %1 == start (
	call :start_container
	exit /B
)

if %1 == stop (
	call :stop_container
	exit /B
)

if %1 == mysqladmin (
	call :open_mysql
	exit /B
)

if %1 == shell (
	call :open_shell
	exit /B
)

if %1 == destroy (
	call :destroy_container
	exit /B
)

if %1 == purge (
	call :purge
	exit /B
)

if %1 == import (
	call :import %2 %3
	exit /B
)

if %1 == help (
	call :show_help
	exit /B
)

call :show_usage
exit /B

:show_usage
echo usage : manage-db.bat ^<cmd^>
echo Use the 'help' command to get a full list of all available commands.
exit /B

:create_database
if [%~1] == [] (
	echo [ARA] ERROR : a folder path is expected to persists the data.
	call :show_usage
	exit /B
)
call :create_container %~1
call :start_container
exit /B

:create_container
call :create_directory_if_not_found %~1
echo [ARA] Creating the image...
docker build -t ara-db-image .
echo [ARA] Create the %container_name% container (data in %~1)
docker run --name %container_name% -e MYSQL_ROOT_PASSWORD=%password% -p %port%:3306 -v %~1:/var/lib/mysql -d ara-db-image
exit /B

:create_directory_if_not_found
if not exist %~1 (
	echo [ARA] The directory %~1 does not exist...
	echo [ARA] Creating the directory %~1...
	setlocal enableextensions
	md %~1
	endlocal
)
exit /B

:start_container
echo [ARA] Starting %container_name% container...
docker container start %container_name%
exit /B

:stop_container
echo [ARA] Stopping %container_name% container...
docker container stop %container_name%
exit /B

:destroy_container
call :stop_container
echo [ARA] Removing %container_name% container...
docker container rm %container_name%
exit /B

:open_mysql
docker exec -it %container_name% mysql -uroot -p%password% ara-dev
exit /B

:open_shell
docker exec -it %container_name% /bin/sh
exit /B

:purge
set /p confirmation=Are you sure to delete your ARA's data (can't be undone) [y/N] ?
set ok=0 
if %confirmation% == y (
	set ok=1
)
if %confirmation% == Y (
	set ok=1
)
if %confirmation% == yes (
	set ok=1
)
if %ok% == 1 (
	docker inspect %container_name% | findstr /C:"\"Source\":" > temp.txt
	set /p directory_to_remove=<temp.txt
	FOR /f tokens^=1^,2^,3^,4^ delims^=^" %%a IN ("%directory_to_remove%") do set directory_to_remove=%%d
	call :destroy_container
	rmdir /s /q %directory_to_remove%
	del /f /q temp.txt
)
exit /B

:import
if [%~1] == [] (
	echo [ARA] ERROR : a folder path is expected to persists the data.
	call :show_usage
	exit /B
)
if [%~2] == [] (
	echo [ARA] ERROR : a dump path is expected to import it in the container.
	call :show_usage
	exit /B
)
call :create_container %~1
call :start_container
call :add_dump %~2
exit /B

:add_dump
echo Waiting to the database to be UP...
timeout 5 /NOBREAK
echo Import the dump located at %~1 into the database.
docker exec -i %container_name% sh -c "exec mysql -uroot -p%password% ara-dev " < %~1
exit /B

:show_help
echo manage-db.bat ^<cmd^> : Manage ARA database container.
echo Where ^<cmd^> can be :
echo - create ^<data_dir^> : Pull and run the mysql image and create the database
echo                       structure. Use ^<data_dir^> as the folder to persists
echo                       the data on the host system.
echo - destroy : Destroy the container built by the image but leave the data pristine.
echo - start : Start the database container.
echo - stop : Stop the database container.
echo - mysqladmin : Run the 'mysql' command in the container in interactive mode.
echo - shell : Run an interactive shell in the container.
echo - purge : Destroy the container, and remove the data persisted in the host.
echo - import ^<data_dir^> ^<dump_dir^> : Import the given dump file in a newly created database container.
exit /B