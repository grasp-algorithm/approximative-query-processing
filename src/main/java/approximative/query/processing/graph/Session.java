package approximative.query.processing.graph;

import oracle.pgx.api.PgxGraph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/27/18.
 */
public interface Session extends AutoCloseable {

    /**
     *
     */
    void initialize();

    /**
     *
     * @param edgeFile
     * @param vertexFile
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    void loadOriginalGraph(String edgeFile, String vertexFile) throws IOException, InterruptedException, ExecutionException;


    void loadOriginalGraph(String configPath) throws IOException, InterruptedException, ExecutionException;


    /**
     *
     * @param configPath
     * @param labelUsePath
     * @throws FileNotFoundException
     */
    void loadSummaryGraph(String configPath, String labelUsePath) throws FileNotFoundException;


    /**
     *
     * @param labels
     * @throws ExecutionException
     * @throws InterruptedException
     */
    PgxGraph filterSummaryGraph(Set<String> labels) throws ExecutionException, InterruptedException;

    /**
     *
     * @param labels
     * @throws ExecutionException
     * @throws InterruptedException
     */
    PgxGraph filterSummaryGraph(List<String> labels) throws ExecutionException, InterruptedException;


    /**
     *
     * @return
     */
    PgxGraph getSummaryGraph();


    /**
     *
     * @param summaryGraph
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void setSummaryGraph(PgxGraph summaryGraph) throws ExecutionException, InterruptedException;

    /**
     *
     * @return
     */
    PgxGraph getOriginalGraph();

    /**
     *
     * @return
     */
    Set<LabelUse> getLabelUseSet();

    /**
     *
     * @return
     */
    long getNumVertices();

    /**
     *
     */
    void close();
}
