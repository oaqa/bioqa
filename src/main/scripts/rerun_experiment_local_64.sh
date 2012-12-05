#!/bin/bash

export CLASSPATH=lib/${project.artifactId}-${project.version}.jar:${classpath}

java -Xmx2500m -Djava.library.path=lib/ edu.cmu.lti.oaqa.ecd.driver.ECDDriver $1 $2
