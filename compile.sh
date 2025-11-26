#!/bin/bash

# Simple compile script for the tag-note project

# Create output directory
mkdir -p target/classes

# Compile Java files
javac -cp "src/main/java:~/.m2/repository/org/springframework/boot/spring-boot-starter-data-jpa/3.1.0/spring-boot-starter-data-jpa-3.1.0.jar:~/.m2/repository/org/hibernate/orm/hibernate-core/6.2.2.Final/hibernate-core-6.2.2.Final.jar:~/.m2/repository/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar" \
      -d target/classes \
      src/main/java/com/example/tagnote/*.java \
      src/main/java/com/example/tagnote/config/*.java \
      src/main/java/com/example/tagnote/controller/*.java \
      src/main/java/com/example/tagnote/entity/*.java \
      src/main/java/com/example/tagnote/repository/*.java \
      src/main/java/com/example/tagnote/service/*.java \
      src/main/java/com/example/tagnote/dto/*.java

echo "Compilation completed!"