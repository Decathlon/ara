
   ## ARA Documentation

 ###### Requirements
 To install ARA you need :

       - at least Java 8
       - Intellij Idea (back development)
       - Visual Studio Code ( front development )
       - Docker
       - git

Verify docker is installed on your IDE `$ docker --version`
Verify java is installed on your IDE `$ java --version`

 ###### Get ARA source code

`$ mkdir 'yourFolder'`
`$ cd 'yourFolder'`
`$ git clone https://github.com/Decathlon/ara.git`

 ###### BDD installation

Start your docker and run this command line : 

`$ docker run -e MYSQL_ROOT_PASSWORD=yourDatabasePassword -p 3306:3306 decathlon/ara-db`

__NB : Note your DataBasePassword, it will serve for later__

  ###### Set up front code

After Downloaded Visual Studio Code, verify that you have plugins below :
- npm plugin
- vetur plugin

Import this folder in Visual Studio Code : 
\ara\client

In Visual Studio Code Terminal : enter command below :
`$ npm install`
`$ npm install dependancies`
`$ npm run-script dev`

  ###### Set up back code

Go to Intellij and import ara project previously recovered under Github
- Make `$ clean install `on each pom.xml folder except client folder

- In Edit Configuration part :

**Main Application** : com.decathlon.ara.AraApplication
**VM Options **: -Dspring.profiles.active=dev -- DspringMake .configclean install  onaeachource.password folder=**yourDatabasePassword**
*classpath of module **: ara-server
**JRE **: 1.8

Make a 

  ###### ARA is alive ... : ready to develop

Your docker image bdd  is up ( *docker ps -a* )
So you can try to launch your Back application (run server application)

<center><span style="color:blue">Enjoy with ARA  ;-)</span></center>

@author : ARA_TEAM
at your disposal to help you with any question or need you might have.

