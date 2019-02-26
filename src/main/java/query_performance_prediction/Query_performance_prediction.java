/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ 
package query_performance_prediction;

import com.github.andrewoma.dexx.collection.Pair;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import core.LanguageModel;
import core.SqlConnection;
import featurs.F01;
import featurs.F01_TermBased;
import featurs.F02;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Parastoo
 */
public class Query_performance_prediction {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
        
        
        SqlConnection.getInstance();
        String dataset = "2009";
        String resultSet= "ql";
        int documentNumber = 5;
        int start = 1;
        int end = 200;
        float rate =1.0f;
        boolean avg=false;
        
        F02 f02 = new F02(dataset, resultSet, documentNumber, start, end, rate, avg);
        f02.run();
//        f02.runCollection();
//        
        
        
        
        
        Query_performance_prediction  q = new Query_performance_prediction();  
        q.test(resultSet, dataset, documentNumber, start, end, rate, avg);
//        q.crossfoldValidation(resultSet, dataset, documentNumber, start, end);
//        q.insertToDataBase(resultSet, dataset);
//        q.insertDocEntities();
//        q.insertDocEntities();
//        q.mergeFiles();
//        q.insertToDataBase(resultSet, dataset);
//        q.createMap(resultSet,dataset);
//        q.test(resultSet, dataset, documentNumber, start, end);
//        q.generatecrossFoldData(resultSet, dataset, documentNumber, start, end, avg);
//        q.crossfoldValidation(resultSet, dataset, documentNumber, start, end, avg);
            
    }

    public static HashMap<String, Double> readPerfFromFils(String input) throws FileNotFoundException {
        HashMap<String, Double> result = new HashMap<>();
        Scanner in = new Scanner(new File(input));
        int i = 1;
        while (in.hasNextLine()){
            try{
            String qid = in.next();
            double val = in.nextDouble();
            result.put(qid,val);
            }
            catch(Exception ex){
                
            }
        }
        in.close();
        return result;
    }
    
    public static HashMap<String, Double> readPerfFromFile (String filename, String targetMetric) throws FileNotFoundException {
        HashMap<String, Double> result = new HashMap<>();
        Scanner in = new Scanner(new File(filename));
        int i = 1;
        while (in.hasNextLine()){
            String metric = in.next();
            String qid = in.next();
            if (qid.equals("all"))
                break;
            double val = in.nextDouble();
            if (metric.equals(targetMetric))
                result.put(qid,val);
        }
        in.close();
        return result;
    }
    
    public static double [] evalAll(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        double[] result = new double[3];
        result[0] = computeSpearmannCorrelation(a, b);
        result[1] = computePearsonCorrelation(a, b);
        result[2] = computeKendallsTauDistance(a, b);
        return  result;
    }
    
    public void mergeFiles(){
        try(PrintWriter pw = new PrintWriter("./data/clueweb2009Merged.txt")) {
            File f = new File("C:\\Users\\Parastoo\\Documents\\course\\arshad\\term2\\queryexpantion\\clue09Fix\\data\\faezeh\\SELM\\Repository\\cluewebPoolFiles\\share\\TestData\\ClueWeb09PoolSourceFile_Fix");
            String[] s = f.list();
            if (s == null) {
                throw new IOException("Directory doesn't exist: " + f);
            }
            for (String s1 : s)
            {
                File f1 = new File(f, s1);
                if (!f1.isFile()) {
                    continue;
                }
                try (Reader reader = new FileReader(f1);
                        BufferedReader br = new BufferedReader(reader)) {
                    pw.println("<DOC>");
                    pw.println("<DOCNO>"+s1+"</DOCNO>");
                    pw.println("<TEXT>");
                    System.out.println(s1);
                    String line = br.readLine();
                    while (line != null)
                    {
                        pw.println(line);
                        line = br.readLine();
                    }
                    pw.println("</TEXT>");
                    pw.println("</DOC>");
                }
            }
        } catch (Throwable e) {
            e.getMessage();
        }
    }
    
    public static double computeSpearmannCorrelation(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Comparator comparator = new Comparator<Pair<String, Double>>(){
            @Override
            public int compare(Pair<String, Double> p1, Pair<String, Double> p2) {
                return p2.component2().compareTo(p1.component2());
            }
        };
        Collections.sort(a, comparator);
        Collections.sort(b, comparator);

        Map <String, Integer> first = getMapRank(a);
        System.out.println("list1:");
        for (int i=0; i<a.size(); i++) {
            String q_i = a.get(i).component1();
            System.out.println(q_i);

        }
        System.out.println("list2:");
        for (int i=0; i<b.size(); i++) {
            String q_i = b.get(i).component1();
            System.out.println(q_i);

        }
        Map <String, Integer> second = getMapRank(b);

        double cov = 0, var1 = 0, var2 = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                cov += ((first.get(q_i) - first.get(q_j)) * (second.get(q_i) - second.get(q_j)));
                var1 += ((first.get(q_i) - first.get(q_j)) * (first.get(q_i) - first.get(q_j)));
                var2 += ((second.get(q_i) - second.get(q_j)) * (second.get(q_i) - second.get(q_j)));
            }
        }
        return cov / Math.sqrt(var1 * var2);
    }
    
    public static double computePearsonCorrelation(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Map <String, Double> first = pairToMap(a);
        Map <String, Double> second = pairToMap(b);

        double cov = 0, var1 = 0, var2 = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                cov += ((first.get(q_i) - first.get(q_j)) * (second.get(q_i) - second.get(q_j)));
                var1 += ((first.get(q_i) - first.get(q_j)) * (first.get(q_i) - first.get(q_j)));
                var2 += ((second.get(q_i) - second.get(q_j)) * (second.get(q_i) - second.get(q_j)));
            }
        }
        return cov / Math.sqrt(var1 * var2);
    }
    
    public static double computeKendallsTauDistance(List <Pair<String, Double>> a, List <Pair<String, Double>> b) {
        Map <String, Double> first = pairToMap(a);
        Map <String, Double> second = pairToMap(b);

        int concordant = 0, discordant = 0;
        for (int i=0; i<a.size(); i++) {
            for (int j=i+1; j<a.size(); j++) {
                String q_i = a.get(i).component1();
                String q_j = a.get(j).component1();
                if ((first.get(q_i) - first.get(q_j))*(second.get(q_i) - second.get(q_j)) < 0)
                    discordant++;
                if ((first.get(q_i) - first.get(q_j))*(second.get(q_i) - second.get(q_j)) > 0)
                    concordant++;
            }
        }
        double z = (double)(a.size()*(a.size()-1))/2.;
        return (double)(concordant-discordant)/z;
    }
    
    public static Map <String, Integer> getMapRank (List <Pair<String, Double>> sortedList) {
        Map <String, Integer> result = new HashMap <> ();
        int rank = 0;
        double previousScore = -1000;
        for (int i=0; i<sortedList.size(); i++) {
            if (sortedList.get(i).component2() != previousScore) {
                rank++;
                previousScore = sortedList.get(i).component2();
            }
            result.put(sortedList.get(i).component1(), rank);
        }
        return result;
    }
    
    public static Map <String, Double> pairToMap (List <Pair<String, Double>> list) {
        Map <String, Double> result = new HashMap <> ();
        for (int i=0; i<list.size(); i++) {
            result.put(list.get(i).component1(), list.get(i).component2());
        }
        return result;
    }
    
    public void  insertToDataBase(String rs,String dataset){
        MysqlDataSource dataSource ;
        Connection connection;
        Statement statement;
        dataSource = new MysqlDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("137428sAm");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("query_performance_prediction");
        dataSource.setUseUnicode(true);
        dataSource.setCharacterEncoding("UTF-8");





        String filePath = "./data/"+rs+"-"+dataset+".txt";
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));


            String line ;

            int lineNumber =1;

            String createTable = " CREATE table IF NOT EXISTS "+rs+"_"+dataset+" (\n" +
                                "     query_id varchar(700) ,\n" +
                                "     document_id varchar(700)  ,\n" +
                                "     rank int(11),\n" +
                                "     score varchar(700),\n" +
                                "     primary key (query_id, document_id)\n" +
                                "     \n" +
                                " )";
            statement.executeUpdate(createTable);
            while((line = br.readLine() )!= null)  {
                if(lineNumber>0){
                    String[] lineSplit = line.split(" ");
                    if(lineSplit.length>1){


                        String query_id = lineSplit[0];
                        String document_id = lineSplit[2];
                        String rank = lineSplit[3];
                        String score = lineSplit[4];

                        try{
                            PreparedStatement updateSales = connection.prepareStatement(
                                "insert into "+rs+"_"+dataset+" (query_id,document_id,rank,score) values(?,?,?,?)");
                            updateSales.setString(1, query_id);
                            updateSales.setString(2, document_id);
                            updateSales.setInt(3, Integer.parseInt(rank));
                            updateSales.setString(4, score);
                            int row = updateSales.executeUpdate();
                            if (row > 0) {
                                System.out.println("A contact was inserted");

                            }

                        System.out.println(lineNumber);
                        }
                        catch(Exception ex){
                            System.out.println("repeated");
                            statement.close();
                            connection.close();
                            connection = dataSource.getConnection();
                            statement = connection.createStatement();
                        }
                    }
                }
                lineNumber ++;
            }


            connection.close();
            br.close();
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

    }
    
    public void createMap(String rs, String dataset){

        String filePath = "./data/map-"+rs+"-"+dataset+".txt";

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            FileWriter writer = new FileWriter(new File("./data/map-"+rs+"-"+dataset+"-result.txt"));


            String line ;

            int lineNumber =1;

            while((line = br.readLine() )!= null)  {
                if(line.contains("map")){
                   writer.write(line+"\n");
                }
                lineNumber ++;

            }

            br.close();
            writer.close();
        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

    }
    
    public void insertDocEntities(String rs,String ds){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/"+ds+"/document/"+ds+"-entities.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            String line ;

            int lineNumber =1;

            String createTable = " CREATE table IF NOT EXISTS document_"+ds+" (\n" +
                                "     id int(11) ,\n" +
                                "     title varchar(10000)  ,\n" +
                                "     entity longtext,\n" +
                                "     primary key (id)\n" +
                                "     \n" +
                                " )";
            statement.executeUpdate(createTable);
            int docCount =0;
            String lastId = "";

            ArrayList<String> entList = new ArrayList<>();;
            line = br.readLine();
            while(line != null)  {
                try{
                    if(lineNumber>773257){

                        System.out.println(lineNumber);

                        String docId = line.split("\t")[0];
                        String entitiesWithConf = line.split("\t")[1];
                        if(docId.equals(lastId)){
                            String[] ent_conf = entitiesWithConf.split(" ");

                            for (String temp : ent_conf) {
                                entList.add(temp.split("\\|")[0]);
                            }
                            line = br.readLine();
                            lineNumber++;
                        }
                        else{
                            String entities = String.join(",",entList);
                            PreparedStatement update =connection.prepareStatement(
                                    "insert into document_robust (id,title,entity) values(?,?,?)");
                            update.setInt(1, docCount);
                            update.setString(2, lastId);
                            update.setString(3, entities);
                            int row2 = update.executeUpdate();
                            if(row2<=0){
                                System.out.println("problem");
                                line = br.readLine();
                                lineNumber++;
                            }
                            else{
                                docCount++;
                            }
                            update.close();
                            entList = new ArrayList<>();
                            lastId = docId;
                        }


                    }
                    else{
                        line = br.readLine();
                        lineNumber++;
                        if(lineNumber == 312517)
                            System.out.println(line);
                    }
                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());
                    line=br.readLine();
                    lineNumber++;
                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getStackTrace());
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();
                    line=br.readLine();
                    lineNumber++;
                }
            }

            connection.close();
            br.close();
        }
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
    
    public void insertQueryEntities(String ds){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/Robust/query/query-entities-robust.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            String line ;

            int lineNumber =1;
            String createTable = " CREATE table IF NOT EXISTS document_"+ds+" (\n" +
                                "     id int(11) ,\n" +
                                "     title varchar(10000)  ,\n" +
                                "     entity longtext,\n" +
                                "     primary key (id)\n" +
                                "     \n" +
                                " )";
            statement.executeUpdate(createTable);
            while((line= br.readLine())!= null)  {
                try{

                    System.out.println(lineNumber);
                    String queryId = line.split(": ")[0];
                    String entities = line.split(": ")[1];


                    PreparedStatement update =connection.prepareStatement(
                            "insert into query_Robust (id,title,entity) values(?,'',?)");
                    update.setString(1, queryId);
                    update.setString(2, entities);
                    int row2 = update.executeUpdate();
                    if(row2<=0){
                        System.out.println("problem");
                    }

                    update.close();
                    lineNumber++;


                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());

                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getMessage());
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();

                }
            }


            connection.close();
            br.close();
        }
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }

    }
    
    public void createEntitySet(String ds){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/"+ds+"/query/"+ds+"-entities.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            String line ;

            int lineNumber =1;

            while((line= br.readLine())!= null)  {
                try{

                    System.out.println(lineNumber);
                    String queryId = line.split(": ")[0];
                    String[] entities = line.split(": ")[1].split(",");
                    for (String str :entities) {
                        System.out.println(str);
                    }
                    lineNumber++;


                }

                catch(Exception ex){
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();



                }
            }


            connection.close();
            br.close();
        }
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
    
    public void insertEntities(String ds){
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File("./data/"+ds+"/entities/"+ds+"-entities.txt")));
            MysqlDataSource dataSource ;
            Connection connection;
            Statement statement;
            dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("137428sAm");
            dataSource.setServerName("localhost");
            dataSource.setDatabaseName("query_performance_prediction");
            dataSource.setUseUnicode(true);
            dataSource.setCharacterEncoding("UTF-8");
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            String line ;

            int lineNumber =1;
            int id =47738;
            while((line= br.readLine())!= null)  {
                try{

                    System.out.println(lineNumber);
                    String entity = line.split(" ")[0];
                    String title = line.split(" ")[1];
                    title = title.replace(" ", "");
                    title = title.replace("!", "");
                    title = title.replace("\"", "");
                    title = title.replace("*", "");
                    title = title.replace("?", "");


                    PreparedStatement update =connection.prepareStatement(
                            "insert into entity_"+ds+" (id,title,entity,owner) values(?,?,?,'query')");
                    update.setInt(1, id);
                    update.setString(2, title);
                    update.setString(3, entity);
                    int row2 = update.executeUpdate();
                    if(row2<=0){
                        System.out.println("problem");

                    }
                    id++;
                    update.close();
                    lineNumber++;


                }
                catch(SQLException sx){
                    System.out.println("point 1: "+sx.getMessage());

                }
                catch(Exception ex){
                    System.out.println("point 2: "+ex.getMessage());
                    statement.close();
                    connection.close();
                    connection = dataSource.getConnection();
                    statement = connection.createStatement();

                }
            }


            connection.close();
            br.close();
        }
        catch(Exception ex){
            System.out.println("point 3: "+ex.getMessage());
        }
    }
    
    public void generatecrossFoldData(String rs, String dataset, int documentNumber, int start,int end, boolean avg) throws IOException, SQLException{
        for(int i =1; i <=10 ; i=i+1){
            F02 f02 = new F02(dataset,rs,documentNumber,start,end,i/(float)10,avg);
            f02.run();
        }
    }
    
    public void crossfoldValidation(String rs, String dataset, int documentNumber, int start,int end,boolean avg){
        try {
            String mapFile = "./crossfold/"+dataset+"/map/map-"+rs+"-"+dataset+".txt";
            String result  ="./crossfold/"+dataset+"/pearson/pearson-"+rs+"-"+dataset+"-"+documentNumber+"docs-"+avg+"avg.txt";
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(result)));
            HashMap<String, Double> map =  Query_performance_prediction.readPerfFromFile(mapFile,"map");
            List<HashMap<String,Double>> allPredictions = new ArrayList<>();
            double[] finalPearson = new double[5];
            int[] startt = {301,351,401,601,651};
            int[] endd = {350,400,450,650,700};


            for(int j=0 ; j<5 ; j++){
                int startTest = 0;
                int endTest = 0;
                double maxPearson = 0;
                double maxRate = 0;
                if(dataset.equals("robust2")){
                    startTest = startt[j];
                    endTest = endd[j];
                }
                else{
                    startTest = start+(((end-start)+1)*j/5);
                    endTest = startTest+(((end-start)+1)/5)-1;
                }
                for(int i =1 ; i <=10 ; i=i+1){
                    allPredictions = new ArrayList<>();
                    for (int ii = 1; ii <= 10; ii=ii+1 ){
                        String input = "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"-5docs-"+ii/(float)10+"percentEntities-"+avg+"avg-F02.txt";
                        allPredictions.add(Query_performance_prediction.readPerfFromFils(input));

                    }
                    List <Pair<String, Double>> trainMap = new ArrayList<>();
                    HashMap<String,Double> predictions = allPredictions.get((int)(i)-1);
                    List <Pair<String, Double>> testSet = new ArrayList<>();
                    List <Pair<String, Double>> trainSet = new ArrayList<>();

                    for(int k = startTest; k <= endTest ; k++){
                        if (predictions.containsKey(Integer.toString(k))){
                            testSet.add(new Pair<String,Double>(Integer.toString(k),predictions.get(k)));
                            predictions.remove(Integer.toString(k));
                        }
                    }
                    for(String k:predictions.keySet()){
                        trainSet.add(new Pair<String,Double>(k,predictions.get(k)));
                    }
                    for(Pair p : trainSet){
                        if(map.containsKey(p.component1().toString())){
                            double value = map.get(p.component1().toString());
                            String key = p.component1().toString();
                            trainMap.add(new Pair<String,Double>(key,value));
                        }
                        else{
                            trainMap.add(new Pair<String,Double>(p.component1().toString(),0.0));
                        }
                    }
                    double pearson = Query_performance_prediction.evalAll(trainMap, trainSet)[1];
                    System.out.println(pearson);
                    if(maxPearson < pearson){
                        maxPearson = pearson;
                        maxRate = i;
                    }
                }


                allPredictions = new ArrayList<>();
                for (int ii = 1; ii <= 10; ii=ii+1) {
                    String input = "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"-5docs-"+ii/(float)10+"percentEntities-"+avg+"avg-F02.txt";
                    allPredictions.add(Query_performance_prediction.readPerfFromFils(input));

                }
                List <Pair<String, Double>> testPrediction = new ArrayList<>();
                List <Pair<String, Double>> testMap = new ArrayList<>();
//                for(String k:allPredictions.get((int)(maxRate) -1).keySet()){
//                    testPrediction.add(new Pair<String,Double>(k,allPredictions.get((int)(maxRate) -1).get(k)));
//                }
                for(int k = startTest; k <= endTest ; k++){
                        if (allPredictions.get((int)(maxRate) -1).containsKey(Integer.toString(k))){
                            testPrediction.add(new Pair<String,Double>(Integer.toString(k),allPredictions.get((int)(maxRate) -1).get(Integer.toString(k))));
//                            allPredictions.get((int)(maxRate) -1).remove(Integer.toString(k));
                        }
                    }
                for(Pair p : testPrediction){
                    if(map.containsKey(p.component1().toString())){
                        double value = map.get(p.component1().toString());
                        String key = p.component1().toString();
                        testMap.add(new Pair<String,Double>(key,value));
                    }
                    else{
                        testMap.add(new Pair<String,Double>(p.component1().toString(),0.0));
                    }

                }
                finalPearson[j] = Query_performance_prediction.evalAll(testMap, testPrediction)[1];
                System.out.println("finalPearson[j] :" + finalPearson[j] );
                bw.write(j+":"+finalPearson[j]+"\n" );
                bw.flush();
            }
            double avgPearson = 0;
            for (int i = 0; i < 5; i++) {
                avgPearson +=finalPearson[i];
            }
            avgPearson = avgPearson/5;
            System.out.println("avgpearson: "+avgPearson);
            bw.write("final:"+avgPearson+"\n" );
            bw.flush();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Query_performance_prediction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void test(String rs, String dataset, int documentNumber, int start,int end,float rate, boolean avg){
        try {
            String mapFile = "./crossfold/"+dataset+"/map/map-"+rs+"-"+dataset+".txt";
            String colFile = "./crossfold/"+dataset+"/result/"+rs+"-"+dataset+"collectionScore-F02.txt";
            HashMap<String, Double> map =  Query_performance_prediction.readPerfFromFile(mapFile,"map");

            F02 f02 = new F02(dataset,rs,documentNumber,start,end,rate,avg);
            f02.run();
            List <Pair<String, Double>> trainMap = new ArrayList<>();
            HashMap<String,Double> predictions = Query_performance_prediction.readPerfFromFils(f02.filename);
            HashMap<String,Double> collections = Query_performance_prediction.readPerfFromFils(colFile);
            List <Pair<String, Double>> trainSet = new ArrayList<>();


            for(String k:predictions.keySet()){
                double score = 0;
//                if( Double.isInfinite(Math.log(predictions.get(k))) || Double.isInfinite(Math.log(collections.get(k)))){
//                     score = predictions.get(k) - collections.get(k);
//                }
//                else
//                     score = (predictions.get(k) * Math.log(predictions.get(k)))- (collections.get(k) *Math.log(collections.get(k)));
                score = predictions.get(k);//-collections.get(k);
                System.out.println("score: "+score);
                trainSet.add(new Pair<String,Double>(k,score));
            }
            for(Pair p : trainSet){
                if(map.containsKey(p.component1().toString())){
                    double value = map.get(p.component1().toString());
                    String key = p.component1().toString();
                    trainMap.add(new Pair<String,Double>(key,value));
                }
                else{
                    trainMap.add(new Pair<String,Double>(p.component1().toString(),0.0));
                }
            }
            double pearson = Query_performance_prediction.evalAll(trainMap, trainSet)[1];
            double kandal  = Query_performance_prediction.evalAll(trainMap, trainSet)[2];
            double spearman = Query_performance_prediction.evalAll(trainMap, trainSet)[2];

            System.out.println("pearson is:"+pearson);
            System.out.println("kandal is:"+kandal);
            System.out.println("spearman is:"+spearman);



        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            Logger.getLogger(Query_performance_prediction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

    

