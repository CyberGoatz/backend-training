# CyberRangeCZ Platform Training
This project represents back-end for managing trainings in CyberRangeCZ platform.

## Content

1.  Project Modules
2.  Build and Start the Project Using Docker

### 1. Project Modules
This project is divided into several modules:
* `training-rest`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `training-api`
  * Contains API (DTO classes)
    * These are annotated with proprietary converters for DateTime processing.
    * Localized Bean validations are set (messages are localized).
    * Annotations for Swagger documentation are included.
  * Map Entities to DTO classes and vice versa with MapStruct framework.
  * Contains @Transactional annotations.
* `training-service`
    * Provides business logic of the application:
      * Calls persistence layer for database queries and combining the results as necessary.
      * Calls another microservices.
* `training-persistence`
  * Provides data layer of the application (Database queries).
  * Uses Spring Data JPA (Spring wrapper layer over JPA implemented with Hibernate framework).
  * Communicates with PostgreSQL database.
  * Uses QueryDSL for filtering the data.
* `training-opensearch`
  * Used for auditing and retrieving data from OpenSearch storage.
  * Contains Event classes describing particular events.

And the main project (parent maven project with packaging pom):
* `training`
  * Contains configurations for all modules as dependency versions, dependency for spring boot parent project etc.


### Build and Start the Project Using Docker

#### Prerequisites
Install the following technology:

Technology       | URL to Download
---------------- | ------------
Docker           | https://docs.docker.com/install/

#### 1. Preparation of Configuration Files
To build and run the project in docker it is necessary to prepare several configurations.

* Set the [OpenID Connect configuration](https://docs.platform.cyberrange.cz/installation-guide/setting-up-oidc-provider/) which is available on the provided hyperlink.

* Fill OIDC credentials gained from the previous step and set additional settings in the [training.properties](https://github.com/cyberrangecz/backend-training/blob/master/etc/training.properties) file and save it.

#### 2. Build Docker Image
In the project root folder (folder with Dockerfile), run the following command:
```shell
$ sudo docker build \
  --build-arg PROPRIETARY_REPO_URL=https://gitlab.ics.muni.cz/api/v4/projects/2358/packages/maven \
  -t training-image \
  .
```

Dockefile contains several default arguments:
* USERNAME=postgres - the name of the user to connect to the database. 
* PASSWORD=postgres - user password.
* POSRGRES_DB=training - the name of the created database.
* PROJECT_ARTIFACT_ID=training - the name of the project artifact.
* PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO.

Those arguments can be overwritten during the build of the image, by adding the following option for each argument: 
```bash
--build-arg {name of argument}={value of argument} 
``` 

#### 3. Start the Project
Start the project by running docker container, but at first make sure that your ***OIDC Provider*** and [user-and-group](https://github.com/cyberrangecz/backend-user-and-group) service is running. Instead of usage of the PostgreSQL database, you can use the in-memory database H2. It just depends on the provided configuration. To run a docker container, run the following command: 
```shell
$  sudo docker run \
   --name training-container -it \
   --network host \
   -p 8083:8083 \
   training-image
```

Add the following option to use the custom property file:
```shell
-v {path to your config file}:/app/etc/training.properties
```

To create a backup for your database add the following docker option:
```shell
-v db_data_training:/var/lib/postgresql/11/main/
```

Add the following environment variable to wait for other services until they are up and running:
```shell
-e SERVICE_PRECONDITION="localhost:8084, localhost:8082"
```  
