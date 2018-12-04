package approximative.query.processing.graph;

import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxSession;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/27/18.
 */
public interface Loader {

    /**
     *
     * @param session
     * @param configPath
     * @return
     * @throws FileNotFoundException
     */
    PgxGraph loadGraph(PgxSession session, String configPath) throws FileNotFoundException;


    /**
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    Set<LabelUse> loadLabelsUse(String path) throws FileNotFoundException;


    /**
     *
     * @param session
     * @param edgeFile
     * @param vertexFile
     * @return
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    PgxGraph loadGraph(PgxSession session, String edgeFile, String vertexFile) throws IOException, ExecutionException,
            InterruptedException;



}
