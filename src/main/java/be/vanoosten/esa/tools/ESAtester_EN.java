/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.vanoosten.esa.tools;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.queryparser.classic.ParseException;

import be.vanoosten.esa.EnwikiFactory;
import be.vanoosten.esa.WikiAnalyzer;
import be.vanoosten.esa.WikiFactory;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import core.SqlConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import static org.apache.lucene.util.Version.LUCENE_48;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mohaddeseh
 */
public class ESAtester_EN {

    MysqlDataSource dataSource ;
    Connection connection;
    Statement statement;
//    HashMap<String, Double> maxSimilarity;
    HashMap<String, Double> countMap;
    public ESAtester_EN(){
        countMap = new HashMap<>();
        dataSource = new MysqlDataSource();
        dataSource.setUser("root");
        dataSource.setPassword("137428sAm");
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("query_performance_prediction");
        dataSource.setUseUnicode(true);
        dataSource.setCharacterEncoding("UTF-8");

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws IOException {


        ESAtester_EN esa = new ESAtester_EN();



    }

    public ArrayList<String> outputESA(ArrayList<String> entityPair) throws FileNotFoundException, IOException {

//        maxSimilarity = new HashMap<>();
        
        
        
        String output = "";
        String count ="";
        ArrayList<String> entityPairResult = new ArrayList<>();
        for(String line : entityPair) {
            String entity1 = line.split(" ")[0];
            String entity2 = line.split(" ")[1];
            count = line.split(" ")[2];

            try {
                double esa = CalcSimilarity(entity1, entity2);
                countMap.put(entity2, Double.parseDouble(count));
//                if(!maxSimilarity.containsKey(entity2))
//                    maxSimilarity.put(entity2, esa);
//                else if(maxSimilarity.get(entity2)<esa)
//                    maxSimilarity.put(entity1, esa);
                
                output = entity1 + " " + entity2 + " " + esa +" "+count ;
                entityPairResult.add(output);
              
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

        }
//        for (String str : maxSimilarity.keySet()) {
//            if(countMap.get(str)!=null)
//            {   output = "entity1" + " " + str + " " + maxSimilarity.get(str) +" "+countMap.get(str) ;
//                entityPairResult.add(output);
//            }
//        }
        return entityPairResult;

    }

    public static double calcESA(String t1, String t2) throws Exception {
        WikiFactory factory = new EnwikiFactory();
        CharArraySet stopWords = factory.getStopWords();

        String loc_index = "C:\\Development\\esa\\enwiki";
        WikiAnalyzer wa = new WikiAnalyzer(LUCENE_48, stopWords);
        Vectorizer vec = new Vectorizer(new File(loc_index), wa);

        SemanticSimilarityTool sst = new SemanticSimilarityTool(vec);

        double sim = sst.findSemanticSimilarity(t1, t2);
        factory.close();
        return sim;
    }

    private void writeOutputToFile(String content, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(content);
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void fileWriterContinue(String content, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.append(content);
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();

            } catch (IOException ex) {
                Logger.getLogger(ESAtester_EN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private double CalcSimilarity(String entity1, String entity2) {
        double esa=0;
        try {
                String query= "SELECT similarity FROM `similarityScore` WHERE (entity1 = \""+entity1+"\" and entity2 = \""+entity2+
                        "\" ) or (entity2 = \""+entity1+"\" and entity1 = \""+entity2+"\" )";
                ResultSet resultset = SqlConnection.select(query);
                
                if(resultset.next()){
                    esa= Double.parseDouble( resultset.getString(1));
                }
                else{
                    esa = calcESA(entity1, entity2);
                    try{
                            PreparedStatement updateSales = connection.prepareStatement(
                                "insert into similarityScore (entity1,entity2,similarity) values(?,?,?)");
                            updateSales.setString(1, entity1); 
                            updateSales.setString(2, entity2);
                            updateSales.setString(3, Double.toString(esa));
                            int row = updateSales.executeUpdate();
                            if (row > 0) {
                                System.out.println("A contact was inserted");

                            } 

                        }
                        catch(Exception ex){
                            System.out.println("repeated");
                            statement.close();
                            connection.close();
                            connection = dataSource.getConnection();
                            statement = connection.createStatement();
                        }
                }
                
              
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        return esa;
    }
}
