FROM java:openjdk-7-jdk

COPY . /usr/sparqlmap
WORKDIR /usr/sparqlmap

RUN ./gradlew clean installDist

CMD /usr/sparqlmap/build/install/sparqlmap/bin/sparqlmap -dburl $DB_URL -dbuser $DB_USER -dbpassword $DB_PASS