package approximative.query.processing.graph;

import approximative.query.processing.exceptions.LoadGraphDataException;
import approximative.query.processing.util.Constants;
import oracle.pgx.api.GraphBuilder;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxSession;
import oracle.pgx.config.IdGenerationStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/13/18.
 */
class GraphLoader implements Loader {
    private static final Logger LOG = LogManager.getLogger("GLOBAL");
    private static volatile AtomicInteger idEdge = new AtomicInteger(-1);

    private Set<LabelUse> labelUseSet;

    private static Integer getIdEdge() {
        return idEdge.incrementAndGet();
    }

    GraphLoader() {
        this.labelUseSet = new HashSet<>();
    }

    @Override
    public PgxGraph loadGraph(PgxSession session, String configPath) throws FileNotFoundException {
        try {
            File config = new File(configPath);
            if (!config.exists())
                throw new FileNotFoundException(String.format("File '%s' not found.", configPath));

            return session.readGraphWithProperties(config.getPath());

        } catch (ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
            throw new LoadGraphDataException("Graph could not be loaded.");
        }
    }

    @Override
    public Set<LabelUse> loadLabelsUse(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists())
            throw new FileNotFoundException(String.format("File '%s' not found", path));

        boolean skipHead = true;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                if (!skipHead) {
                    String[] array = scanner.nextLine().split(" ");

                    labelUseSet.add(new LabelUse()
                            .setLabel(array[0])
                            .setPresentOnHNProperties(array[1].equals("1"))
                            .setPresentOnCrossEdges(array[2].equals("1"))
                            .setPresentOnReachCount(array[3].equals("1"))
                            .setPresentOnPathInCount(array[4].equals("1"))
                            .setPresentOnPathOutCount(array[5].equals("1"))
                            .setPresentOnSumInDegree(array[6].equals("1"))
                            .setPresentOnSumOutDegree(array[7].equals("1")));
                }

                skipHead = false;
            }
        }

        return labelUseSet;
    }


    @Override
    public PgxGraph loadGraph(PgxSession session, String edgeFile, String vertexFile) throws IOException, ExecutionException,
            InterruptedException {

        GraphBuilder<Integer> builder = session.createGraphBuilder(IdGenerationStrategy.USER_IDS, IdGenerationStrategy.USER_IDS);
        if (new File(vertexFile).exists()) {
            try (FileInputStream inputStream = new FileInputStream(vertexFile)) {

                try (Scanner sc = new Scanner(inputStream, "UTF-8")) {
                    while (sc.hasNextLine()) {
                        String[] line = sc.nextLine().split(" ");
                        builder.addVertex(Integer.parseInt(line[0]))
                                .addLabel("Person");
                    }

                    // note that Scanner suppresses exceptions
                    if (sc.ioException() != null) {
                        throw sc.ioException();
                    }
                }
            }
        }

        if (!new File(edgeFile).exists())
            throw new FileNotFoundException(String.format("File '%s' not found.", edgeFile));

        try (FileInputStream inputStream = new FileInputStream(edgeFile)) {
            try (Scanner sc = new Scanner(inputStream, "UTF-8")) {
                while (sc.hasNextLine()) {
                    String[] line = sc.nextLine().split(Constants.SEPARATOR_CONFIG);

                    String labelPrefix = "";
                    if (line[1].chars().allMatch(Character::isDigit))
                        labelPrefix = Constants.LABEL_PREFIX;

                    builder.addEdge(getIdEdge(),
                            Integer.parseInt(line[0]), Integer.parseInt(line[2]))
                            .setLabel(labelPrefix.concat(line[1]));

                }
                // note that Scanner suppresses exceptions
                if (sc.ioException() != null) {
                    throw sc.ioException();
                }
            }
        }

        return builder.build();
    }

}
