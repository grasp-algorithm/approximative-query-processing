package approximative.query.processing.translator.analyzer;

import approximative.query.processing.converter.Converter;
import approximative.query.processing.converter.ConverterImpl;
import approximative.query.processing.converter.Generator;
import approximative.query.processing.converter.GeneratorImpl;
import approximative.query.processing.translator.schema.Workload;
import approximative.query.processing.util.Util;
import oracle.pgx.api.PgxGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/14/18.
 */
public class WorkloadLoader {
    private Workload workload;
    private Set<String> labels;

    public WorkloadLoader() {
        this.workload = new Workload();
        labels = new HashSet<>();
    }

    public void loadWorkload(String[] workloadDirectories) throws IOException{
        loadWorkload(workloadDirectories, false);
    }

    public void loadWorkload(String[] workloadDirectories, boolean generateDisjunction) throws IOException {

        Converter converter = new ConverterImpl();

        for (String workloadDirectory : workloadDirectories) {
            File folder = new File(workloadDirectory);
            if (!folder.isDirectory())
                throw new FileNotFoundException(String.format("Folder '%s' not found", workloadDirectory));

            workload.getQueries().addAll(converter.convert(workloadDirectory, generateDisjunction));
            // fixme remove this query
            workload.getQueries().removeIf(q ->
                Util.getLabels(q.getMatch()).size() == 2 && Util.getLabels(q.getMatch()).get(0).equals(Util.getLabels(q.getMatch()).get(1))
                                          );
        }
    }


    public void generateWorkloads(PgxGraph graph) {

        Generator generator = new GeneratorImpl();
        workload.getQueries().addAll(generator.generate(graph));
    }


    public Set<String> getLabelsOnWorkload() {

        workload.getQueries().forEach(q -> {
            labels.addAll(Util.getLabels(q.getMatch()));
        });

        return labels;
    }

    public Workload getWorkload() {
        return workload;
    }
}
