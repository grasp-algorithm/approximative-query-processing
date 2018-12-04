package approximative.query.processing.graph;

import approximative.query.processing.exceptions.InputLabelSetEmptyException;
import approximative.query.processing.util.Constants;
import approximative.query.processing.util.PrefixProperty;
import oracle.pgx.api.Pgx;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxSession;
import oracle.pgx.api.filter.EdgeFilter;
import oracle.pgx.api.filter.GraphFilter;
import oracle.pgx.api.filter.VertexFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/13/18.
 */
public class GraphSession implements Session {
    private static final Logger LOG = LogManager.getLogger("GLOBAL");
    private static final String SESSION_NAME = "aqp-session";

    private PgxSession session;
    private PgxGraph originalGraph;
    private PgxGraph summaryGraph;
    private Set<LabelUse> labelUseSet;
    private long numVertices;

    public GraphSession() {
        labelUseSet = new HashSet<>();
    }

    /**
     * Initialize the PGX session.
     */
    public void initialize() {
        try {
            session = Pgx.createSession(SESSION_NAME);
        } catch (ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }


    /**
     * This method allow the data load on the summaryGraph
     *
     * @param edgeFile
     * @param vertexFile TODO
     *
     * @see Loader#loadGraph(PgxSession, String)
     */
    public void loadOriginalGraph(String edgeFile, String vertexFile) throws IOException, InterruptedException, ExecutionException {
        Loader loader = new GraphLoader();
        originalGraph = loader.loadGraph(session, edgeFile, vertexFile);
        numVertices = originalGraph.getNumVertices();
    }

    @Override
    public PgxGraph filterSummaryGraph(Set<String> labels) throws ExecutionException, InterruptedException {

        Set<String> listEdgeFilter = new HashSet<>();
        Set<String> listVertexFilter = new HashSet<>();

        for (String label : labels) {
            listEdgeFilter.add(String.format("edge.label() ='%s'", label));

            if (labelUseSet.stream().anyMatch(lu -> lu.getLabel().equals(label) && lu.isPresentOnHNProperties()))
                listVertexFilter.add(String.format("vertex.%s > 0", PrefixProperty.PERCENTAGE.toString()
                        .concat(Constants.SEPARATOR)
                        .concat(label)));
        }

        GraphFilter filter;
        if (!listVertexFilter.isEmpty() && !listEdgeFilter.isEmpty())
            filter = new EdgeFilter(String.join(" || ", listEdgeFilter))
                    .union(new VertexFilter(String.join(" || ", listVertexFilter)));
        else if (!listEdgeFilter.isEmpty())
            filter = new EdgeFilter(String.join(" || ", listEdgeFilter));
        else if (!listVertexFilter.isEmpty())
            filter = new VertexFilter(String.join(" || ", listVertexFilter));
        else
            throw new InputLabelSetEmptyException("Filter fail");


        return summaryGraph.filter(filter);
    }

    @Override
    public PgxGraph filterSummaryGraph(List<String> labels) throws ExecutionException, InterruptedException {
        return filterSummaryGraph(new HashSet<>(labels));
    }


    @Override
    public void loadOriginalGraph(String configPath) throws IOException {
        Loader loader = new GraphLoader();
        originalGraph = loader.loadGraph(session, configPath);
        numVertices = originalGraph.getNumVertices();
    }


    /**
     * This method allow the data load on the summaryGraph
     *
     * @param configPath path of the config file
     *
     * @throws FileNotFoundException if the config file does not exists.
     * @see Loader#loadGraph(PgxSession, String)
     */
    public void loadSummaryGraph(String configPath, String labelUsePath) throws FileNotFoundException {
        Loader loader = new GraphLoader();
        summaryGraph = loader.loadGraph(session, configPath);
        labelUseSet = loader.loadLabelsUse(labelUsePath);
    }

    public PgxGraph getSummaryGraph() {
        return summaryGraph;
    }

    public PgxGraph getOriginalGraph() {
        return originalGraph;
    }

    public Set<LabelUse> getLabelUseSet() {
        return labelUseSet;
    }

    public void setLabelUseSet(Set<LabelUse> labelUseSet) {
        this.labelUseSet = labelUseSet;
    }

    public PgxSession getSession() {
        return session;
    }


    public void setSummaryGraph(PgxGraph summaryGraph) throws ExecutionException, InterruptedException {

        this.summaryGraph.destroy();
        this.summaryGraph = summaryGraph;
    }


    public long getNumVertices() {
        return numVertices;
    }

    /**
     * Close the PGX Session.
     */
    public void close() {
        session.close();
    }

}
