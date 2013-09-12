Biomedical Question Answering Framework
=======================================

The Biomedical Question Answering Framework provides an effective open-source solution to automatically finding the optimal combination of components and their configurations (configuration space exploration problem, or CSE problem) in building a biomedical question answer system (e.g. to respond to a question in [TREC Genomics Track][], *What is the role of PrnP in mad cow disease?*).

> **The BioQA framework is not just one particular QA system, but represents infinite number of possible QA solutions by intergrating various related toolkits, algorithms, knowledge bases or other resources defined in a BioQA configuration space.**

The framework employs the topic set and benchmarks from the question answering task of [TREC Genomics Track][], as well as commonlyused tools, resources, and algorithms cited by participants. A set of basic components has been selected and adapted to the [CSE Framework][] implementation by writing wrapper code where necessary, and users can also easily extend to wrap other existing tools or newly developped algorithms. This configuration space represented by the extended configuration descriptors (defined for the resulting set of configured components, e.g. [default-sqlite-test.yaml](src/main/resources/bioqa/default-sqlite-test.yaml), [default-mysql-test.yaml](src/main/resources/bioqa/default-mysql-test.yaml), [bioqa-test.yaml](src/main/resources/bioqa/bioqa-test.yaml)) can be explored with the [CSE Framework][] automatically, yielding an optimal and generalizable configuration which can outperform published results of the given components for the same task.

Source code, Maven dependency, and BibTex citation
--------------------------------------------------

**GitHub home**: https://github.com/oaqa/bioqa

**Use it in your project**: Artifact is publicly available in the [OAQA Repository][]  or [Central Repository][].
```xml
<dependency>
  <groupId>edu.cmu.lti.oaqa.bio.core</groupId>
  <artifactId>bioqa</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Cite it in your paper**
```tex
@inproceedings{Yang:2013,
 author = {Yang, Zi and Garduno, Elmer and Fang, Yan and Maiberg, Avner and McCormack, Collin and Nyberg, Eric},
 title = {Building Optimal Information Systems Automatically: Configuration Space Exploration for Biomedical Information Systems},
 booktitle = {Proceedings of the 22st ACM international conference on Information and knowledge management},
 series = {CIKM '13},
 year = {2013},
 location = {San Fransisco, CA, USA},
 numpages = {10},
 url = {http://dx.doi.org/10.1145/2505515.2505692},
 doi = {10.1145/2505515.2505692}
 publisher = {ACM},
 address = {New York, NY, USA},
}
```

How to test it?
---------------

### Prerequisite

1. Be sure [Maven](http://maven.apache.org/) is installed and properly configured to fetch the dependency artifacts.
2. Offline corpus annotation and indexing
    1. Annotate the [TREC Genomics corpus](http://ir.ohsu.edu/genomics/2006data.html#docs) with the `legalspan` and `sentence` annotations with the `legalspans.txt` file from the organizer and any sentence segmenter respectively using [UIMA](https://uima.apache.org/). Serialized the annotated CAS corresponding to each document to an XMI file.
    2. Optionally you may gzip each xmi file.
    3. Index the annotated corpus with [Indri](http://www.lemurproject.org/indri/) search engine. (You should be able to search for extents `legalspan` and `sentence`.)

### Test a simple configuration space with SQLite as the persistence database

1. A schema with no content can be downloaded from the [emptydb project](https://github.com/oaqa/emptydb/raw/master/oaqa-eval.db3) and save to `BIOQA_HOME/data/`. If you save it in a difference location or you change the username/password, you need to update `src/main/resources/bioqa/persistence/local-sqlite-persistence-provider.yaml`.
2. Update the YAML descriptors by providing the information how to access and Indri. Replace `INDRI_URL` and `INDRI_PORT` with your actual indri url and indri port in `src/main/resources/bioqa/retrieval/default-sqlite.yaml` and `src/main/resources/bioqa/ie/default-sqlite/yaml`.
3. Specify the main yaml as `src/main/resources/bioqa/default-sqlite-test.yaml` and execute: `mvn exec:exec -Dconfig=bioqa.default-sqlite-test`.

### Test a simple configuration space with MySQL as the persistence database

1. Create your own MySQL schema, and update `src/main/resources/bioqa/persistence/local-mysql-persistence-provider.yaml` with your own `url`, `username` and `password`.
2. Update the YAML descriptors by providing the information how to access the Indri service in the same way as on a single machine.
3. Specify the main yaml as `src/main/resources/bioqa/default-mysql-test.yaml` and execute: `mvn exec:exec -Dconfig=bioqa.default-mysql-test`.

### Test the entire configuration space provided by the BioQA framework

1. Update the YAML descriptors by providing the information how to access Indri.
2. Update the YAML descriptors by providing the information how to access the annotated corpus. Replace `XMI_DIR_PATH` with the directory or URL prefix that contains the annotated XMI files (or gzipped XMI files). For example, `file:/PATH/TO/YOUR/XMIGZ/DIRECTORY` or `http://URL:PORT/HTTP/SERVICE/URL/TO/PROVIDE/ACCESS/TO/REMOTE/FILES`.
3. Use MySQL as persistence database or update the main `src/main/resources/bioqa/bioqa-test.yaml` similar to `src/main/resources/bioqa/retrieval/default-sqlite.yaml` if SQLite or other persistence media is being used.
4. Specify the main yaml and execute: `mvn exec:exec -Dconfig=bioqa.bioqa-test`.

 (See Section 6 of the [CSE paper][] for more detailed for component description.)

### Test it on a cluster with CSE Asynchrous Driver based on [UIMA-AS][]

1. Be sure [UIMA-AS] is installed on the cluster.
2. Update the broker `URL` and `PORT` in `src/main/resources/bioqa/async/cse-broker.yaml`, `src/main/resources/bioqa/collection/db-collection-reader-consumer.yaml` and `src/main/resources/bioqa/collection/db-collection-reader-provider.yaml`.
3. The inputs and gold-standard outputs need to stored a prior in the `inputelements` table of the databas, which will be retrieved directly from database while the program is being executed.
4. Update the database access information `JDBC_CONNECTION_URL`, `USERNAME`, and `PASSWORD` in both `src/main/resources/bioqa/collection/db-collection-reader-consumer.yaml` and ``src/main/resources/bioqa/collection/db-collection-reader-provider.yaml`.
5. Execute the producer on the cluster's master node and start the consumer on each slave node.


How to extend it?
-----------------

Please refer to [OAQA Tutorial] to learn how to create your own framework.


[OAQA Repository]: http://mu.lti.cs.cmu.edu:8081/nexus/content/groups/public/
[Central Repository]: http://search.maven.org/
[TREC Genomics Track]: http://ir.ohsu.edu/genomics/
[CSE Framework]: https://github.com/oaqa/cse-framework/
[UIMA-AS]: https://uima.apache.org/doc-uimaas-what.html
[OAQA Tutorial]: https://github.com/oaqa/oaqa-tutorial/wiki/Tutorial

License
-------

Copyright 2013 Carnegie Mellon University

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Questions?
----------

If you have any questions or suggestions, please feel free to create an issue, or contact [me](http://www.cs.cmu.edu/~ziy).
