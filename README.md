## Approximative Query Processing

This module is responsible for the query translation from queries in the original graph to queries in the
 summary graph and approximate the evaluation of queries

# Getting Started

1. Clone the project or download the project.
2. It requires the latest version of [PGX 3.1](https://www.oracle.com/technetwork/oracle-labs/parallel-graph-analytix/downloads/index.html) (courtesy of Oracle Labs).
3. Execute the file [`dependencies-3.1.0.sh`](label-driven-summarization/scripts/dependencies-3.1.0.sh). But first modify your PGX_HOME and the three variables in the file to point to PGX libraries.
4. Execute `mvn clean compile package`.
5. Execute the following command:

```bash
$ OPTS="-Xms<MIN_MEMORY_USE>g -Xmx<MAX_MEMORY_USE>g \
-Dpgx.max_off_heap_size=<MAX_MEMORY_USE>000000 \
-XX:-UseGCOverheadLimit \
-Dlog4j.debug \
-Dlog4j.configurationFile=<LOG4J_PROPERTIES>"

$ java OPTS -jar ~/graphcon/approximative-query-processing/target/approximative-query-processing-1.0-SNAPSHOT.jar <FOLDER_SUMMARY_AND_ORIGINAL_GRAPH> [WORKLOADS_FOLDERS]
```
Where:
* `<MIN_MEMORY_USE>` : The minimal use of memory allowed.
* `<MAX_MEMORY_USE>` : The maximal use of memory allowed.
* `<LOG4J_PROPERTIES>` : a log4j file properties to specify the level of log and the path to the directory.
* `<FOLDER_SUMMARY_AND_ORIGINAL_GRAPH>` is the path of a directory with the files of the summary + the original graph:
    - edge_file.txt: A required file with triplets or edges of the graph (gMark output format).
    - vertex_file.txt: An optional file with the id of all the vertices and potentially also with labels.
    - schema.json: A required file with the description of what algorithms will be applied to the input graph.
    - summary-pgx.json: The config file of the summary graph.
    - static-label-use.txt: A file with a list of labels and its use on hyper-edges or hyper-nodes.
* `[WORKLOADS_FOLDERS]`: in optional list of folders where it's possible to send workloads as json files with the follow format:

```json
{
  "queries": [
    {
      "select": "SELECT COUNT(*)",
      "match": "MATCH () -[:l5]-> () -[:l0]-> ()"
    },
    {
      "select": "SELECT COUNT(*)",
      "match": "MATCH () <-[:l3]- () -[:l4]-> ()"
    },
    ...

  ]
}
```

_* If non workload is sent, the algorithm will generate queries of type: single-label, Kleene-Plus/ Kleene-Star queries, disjunction & concantenation._

# Example
An example is provided in the [resources folder](https://github.com/grasp-algorithm/label-driven-summarization/tree/master/src/main/resources/summaries/running-example).
There you can find the required files (same example as the paper) and the command.

```bash
$ OPTS="-Xms1g -Xmx1g \
-Dpgx.max_off_heap_size=1000000 \
-XX:-UseGCOverheadLimit \
-Dlog4j.debug"

$ java OPTS -jar Label-driven-1.0.0-jar-with-dependencies.jar /resources/summaries/running-example/
```

Once the command finish its execution, the last line will be:

|Number| Query                                                | Type        | Length  | Error | NumberOfQueries | RuntimeOG | RuntimeSG  |ResultOG    | ResultSG  | Gain      | NoPresentOnCrossEdges  |
| ---- | ---------------------------------------------------- | ------------| ------- |------ | --------------- | --------- | ---------- | ---------- | --------- | --------- | ---------------------- |
|Q1    | **SELECT COUNT(*) MATCH (x0)<-/:expires?/-(x1)*      | OPTIONAL    | 1       |  0.0% | 1               | 248       | 7.4        | 3162       | 3162      | 97.01613  | false                  |
|Q2    | **SELECT COUNT(*) MATCH () -[:price&#124;type]-> ()* | DISJUNCTION | 2       |  0.0% | 2               | 90        | 33.2       | 677        | 677       | 63.111107 | true                   |

Where:
* **Query**: is the original query over the original graph.
* **Type**: It could be SINGLE LABEL, OPTIONAL, DISJUNCTION, KLEENE STAR, KLEENE PLUS, CONCATENATION.
* **Length**: The number of labels involved.
* **Error**: The percentage of error between the original result and the approximate result.
* **NumberOfQueries**: the number of queries executed over the summary graph to estimate the answer.
* **RuntimeOG**: the runtime that takes the query to be executed in the original graph (microseconds).
* **RuntimeSG**: the runtime that takes the query to be executed in the summary graph (microseconds).
* **ResultOG**: the real result in the original graph.
* **ResultSG**: the approximate result in the summary graph.
* **Gain**: The ratio of gain in the runtime between the original graph and the summary graph runtimes.

_* The queries are executed six times, and the first one is removed for the stats average._