#!/bin/bash

export CLASSPATH=lib/${project.artifactId}-${project.version}.jar:${classpath}

mkdir -p log

java -Xmx4000m -verbose:gc -Djava.library.path=lib/ edu.cmu.lti.oaqa.cse.driver.AsyncDriver $1 CONSUMER $2 >> log/${HOSTNAME}-remoteqa.log 2>&1
