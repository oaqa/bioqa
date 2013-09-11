Biomedical Question Answering Framework
=======================================

The Biomedical Question Answering Framework provides an effective open-source solution to automatically finding the optimal combination of components and their configurations (configuration space exploration problem, or CSE problem) in building a biomedical question answer system (e.g. to respond to a question in [TREC Genomics Track][], *What is the role of PrnP in mad cow disease?*).

> **The BioQA framework is not just one particular QA system, but represents infinite number of possible QA solutions by intergrating various related toolkits, algorithms, knowledge bases or other resources defined in a BioQA configuration space.**

The framework employs the topic set and benchmarks from the question answering task of [TREC Genomics Track][], as well as commonlyused tools, resources, and algorithms cited by participants. A set of basic components has been selected and adapted to the [CSE Framework][] implementation by writing wrapper code where necessary, and users can also easily extend to wrap other existing tools or newly developped algorithms. This configuration space represented by the extended configuration descriptors (defined for the resulting set of configured components, e.g. [default-sqlite-test.yaml](../blob/master/src/main/resources/bioqa/default-sqlite-test.yaml), [default-mysql-test.yaml](../blob/master/src/main/resources/bioqa/default-mysql-test.yaml), [bioqa-test.yaml](../blob/master/src/main/resources/bioqa/bioqa-test.yaml)) can be explored with the [CSE Framework][] automatically, yielding an optimal and generalizable configuration which can outperform published results of the given components for the same task.


**GitHub home**: https://github.com/oaqa/bioqa

**Maven dependency** (Artifact is publicly available in the [OAQA Repository][]  or [Central Repository][].)
```xml
<dependency>
  <groupId>edu.cmu.lti.oaqa.bio.core</groupId>
  <artifactId>bioqa</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Citation**
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

Overview
--------


[OAQA Repository]: http://mu.lti.cs.cmu.edu:8081/nexus/content/groups/public/
[Central Repository]: http://search.maven.org/
[TREC Genomics Track]: http://ir.ohsu.edu/genomics/
[CSE Framework]: https://github.com/oaqa/cse-framework/
