#!/bin/bash

export CLASSPATH=lib/${project.artifactId}-${project.version}.jar:${classpath}

java -Xmx2500m -Djava.library.path=lib/ edu.cmu.lti.oaqa.cse.driver.AsyncDriver $1 $2 $3