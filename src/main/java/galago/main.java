/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package galago;

import baselines.PerformancePredictor;
import baselines.StdevQPP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.utility.Parameters;

/**
 *
 * @author Parastoo
 */
public class main {
    public static String index = "C:\\Users\\Parastoo\\Documents\\NetBeansProjects\\explicit-semantic-analysis-master(1)\\explicit-semantic-analysis-master\\indexTest\\indexClueweb2009.idx";
    public static String queryFile ;
    public static String resultFile ;
    public static String dataset = "2009";
    public static String datasetName = "clueweb";
    public static int start = 1;
    public static int end = 200;
    
    public static void main(String[] args)  {
        Parameters globalParams;
        index = args[0];
        queryFile = args[1];
        resultFile = args[2];
        try{
            globalParams = Parameters.create();
            globalParams.set("index", index); // an alternative way to select the index
            globalParams.set("defaultSmoothingMu",2500.0); // all queries use Dirichlet smoothing by default - this is the mu parameter
            // alternatively you can set "mu" in query params

            BufferedReader br = new BufferedReader(new FileReader(new File(queryFile)));
            BufferedWriter wr = new BufferedWriter(new FileWriter(new File(resultFile)));

            String line = "";
            while ((line =br.readLine())!= null) {

                Retrieval retrieval = RetrievalFactory.create(globalParams);
                if (line.split(":").length ==2){
                    String queryId = line.split(":")[0];
                    String queryContent = line.split(":")[1];

                    String qlQuery = queryContent;   // this is a query likelihood query
                    String sdmQuery = "#sdm( "+queryContent+" )";   // this is a sequential dependence model query
                    String sdmRmQuery = "#rm( #sdm( "+queryContent+" ) )";   //  this is an rm3 expanded SDM model
                    String weigthedQLquery = "#combine:0=0.4:1=0.1:2=0.1:3=1.0( "+queryContent+" )";   // this is a query likelihood query with manual weights


                    queryContent = queryContent;

                    Parameters p = Parameters.instance();

                    p.set("fbTerm", 30);
                    p.set("fbOrigWeight", 0.001);
                    p.set("fbDocs", 20);
                    p.set("numDocs", 300);
                    p.get("sampleSize", 100);
                    p.set("StdevApproach", "fixed");
                    p.set("NormalizeStdev", false);
                    PerformancePredictor qpp = new StdevQPP(retrieval);
                    double pred = qpp.predict(queryContent, p);
                    System.out.println("query"+queryId+": "+pred);
                    wr.write("query"+queryId+": "+pred+"\n");
                    wr.flush();

                }

            }
            br.close();
            wr.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }


    }

    

}
