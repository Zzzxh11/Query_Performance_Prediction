package galago;

import baselines.PerformancePredictor;
import baselines.QPPEvaluator;
import baselines.StdevQPP;
import com.github.andrewoma.dexx.collection.Pair;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.utility.Parameters;
import static org.lemurproject.galago.core.tools.apps.BatchSearch.logger;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class testBaselines {
    public static void main(String[] args) throws Exception {
        try {
            String[] indexPaths = new String[1];
//            indexPaths[0] = args[0];
//            String queryFileName = args[1];
//            String mapFile = args[2];
//            String resultFile = args[3];
//            String predictor = args[4];
            indexPaths[0] = "C:\\Users\\Parastoo\\Documents\\NetBeansProjects\\explicit-semantic-analysis-master(1)\\explicit-semantic-analysis-master\\indexTest\\indexClueweb2009.idx";
            String queryFileName = "C:\\Users\\Parastoo\\Desktop\\test\\query.txt";
            String mapFile = "C:\\Users\\Parastoo\\Documents\\NetBeansProjects\\explicit-semantic-analysis-master(1)\\explicit-semantic-analysis-master\\data\\map-ql_2009-result.txt";
            String resultFile = "C:\\Users\\Parastoo\\Desktop\\test\\result.txt";
            String predictor = "WIG";

            BufferedWriter bw= new BufferedWriter(new FileWriter(resultFile));
            Retrieval retrieval = RetrievalFactory.instance(Arrays.asList(indexPaths), Parameters.create());

            List<Parameters> queries = trecToGalagoFormat(queryFileName, retrieval, true);

            List<Pair<String, Double>> predictions = new ArrayList<>();
            for (Parameters query : queries) {
                try {
                    String queryNumber = query.getString("number");
                    String queryText = query.getString("text");
                    queryText = queryText.toLowerCase(); // option to fold query cases -- note that some parameters may require upper case

                    logger.info("Processing query #" + queryNumber + ": " + queryText);
                    Constructor c = Class.forName("baselines."+predictor).getConstructor(Retrieval.class);
                    PerformancePredictor qpp = (PerformancePredictor) c.newInstance(retrieval);
                    query.set("fbTerm", 30);
                    query.set("fbOrigWeight", 0.001);
                    query.set("fbDocs", 20);
                    query.set("numDocs", 300);
                    query.get("sampleSize", 100);
                    query.set("StdevApproach", "fixed");
                    query.set("NormalizeStdev", false);

                    double pred = qpp.predict(queryText, query);
                    predictions.add(new Pair<String, Double>(queryNumber, pred));
                    bw.write(queryNumber+" "+pred+"\n");
                    bw.flush();
                    System.err.println(pred);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    bw.close();
                }
            }
            QPPEvaluator evaluator = new QPPEvaluator();
            List<Pair<String, Double>> avgPrec = QPPEvaluator.readPerfFromFile(mapFile, "map");

            bw.flush();
            String[] eval = evaluator.evalAll(avgPrec, predictions);
            for(String str :eval){
                bw.write(str+"\n");
                bw.flush();
            }
            bw.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static List<Parameters> trecToGalagoFormat(String queryFileName, Retrieval retrieval, boolean b) {
        List<Parameters> result = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(queryFileName)));
            String line="";
            while((line =br.readLine())!=null){
                Parameters p = Parameters.create();
                if(line.split(":").length==2){
                    String number = line.split(":")[0];
                    String text = line.split(":")[1];
                    text = "#stopword("+text+")";
                    p.set("number",number);
                    p.set("text",text);
                    result.add(p);
                }
            }
            br.close();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return result;
    }



}
