package vttp.batch5.paf.movies.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.json.data.JsonDataSource;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;
import net.sf.jasperreports.pdf.SimplePdfReportConfiguration;
import vttp.batch5.paf.movies.repositories.MongoMovieRepository;
import vttp.batch5.paf.movies.repositories.MySQLMovieRepository;

@Service
public class MovieService {

  @Autowired
  private MongoMovieRepository mongoMovieRepository;

  @Autowired 
  private MySQLMovieRepository sqlMovieRepository;

  @Value("${report.name}")
  private String reportName;

  @Value("${report.batch}")
  private String reportBatch;

  // TODO: Task 2
  @Transactional
  public void loadIntoDb(List<JsonObject> movies){

    List<JsonObject> uniqueIds = selectUniquObjects(movies);

    if (sqlMovieRepository.getEntryCount()==0){
      System.out.println("loading into sql...");
      sqlMovieRepository.batchInsertMovies(uniqueIds);
    }
    else{
      System.out.println("sql already populated");
    }
    if(mongoMovieRepository.isEmpty()){
      
      System.out.println("loading into mongo...");

      List<Document> movieDocuments = uniqueIds.stream().map(
        x -> {
          Document d = new Document();
          d.put("_id", x.getString("imdb_id"));
          d.put("title", x.getString("title"));

          String[] directorArray = x.getString("director").split(",");
          List<String> directors = new LinkedList<>();
          for (String s : directorArray){
            directors.add(s.trim());
          }
          d.put("directors", directors); 

          d.put("overview", x.getString("overview"));
          d.put("tagline", x.getString("tagline"));

          String[] genreArray = x.getString("genres").split(",");
          d.put("genres", genreArray); 
          
          try {
            d.put("imdb_rating", x.getInt("imdb_rating"));
          } catch (Exception e) {
            d.put("imdb_rating", 0);
          }
          try {
            d.put("imdb_votes", x.getInt("imdb_votes"));
          } catch (Exception e) {
            d.put("imdb_votes", 0);
          }

          return d;
        }
      ).toList();
      
      List<Document> batch = new LinkedList<>();

      for (int i=0 ; i<movieDocuments.size() ; i++){
        batch.add(movieDocuments.get(i));
        if (batch.size()==25 || i==movieDocuments.size()-1){
          mongoMovieRepository.batchInsertMovies(batch);
          batch.clear();
        }
      }
    }
    else{
      System.out.println("mongo already populated");
    }
  }

  private List<JsonObject> selectUniquObjects(List<JsonObject> movies){
    Set<String> ids = new HashSet<>();
    List<JsonObject> uniqueList = new LinkedList<>();

    movies.forEach(x -> {
      if (!ids.contains(x.getString("imdb_id"))){
        ids.add(x.getString("imdb_id"));
        uniqueList.add(x);
      }
    });

    return uniqueList;
  }

  // TODO: Task 3
  // You may change the signature of this method by passing any number of parameters
  // and returning any type
  public JsonArray getProlificDirectors(int count) {
    List<Document> results = mongoMovieRepository.getDirectors(count);
    JsonArrayBuilder jab = Json.createArrayBuilder();

    results.forEach(director -> {

      JsonObjectBuilder job = Json.createObjectBuilder();
      job.add("director_name", director.getString("_id"));
      job.add("movies_count", director.getInteger("movies_count"));

      double totalRev = 0;
      double totalBudget = 0;
      
      List<String> ids = director.getList("ids", String.class);
      for (String id : ids){
        Map<String, BigDecimal> revBud = sqlMovieRepository.getRevenueAndBudgetById(id);
        totalRev += revBud.get("revenue").doubleValue();
        totalBudget += revBud.get("budget").doubleValue();
      }
      // System.out.println(">>>>service, director: " + director.getString("_id"));
      // System.out.println(">>>service, totalrev : " + totalRev);
      // System.out.println(">>>service, totalBudget : " + totalBudget);
      job.add("total_revenue", totalRev);
      job.add("total_budget", totalBudget);
      JsonObject directorInfo = job.build();
      jab.add(directorInfo);
    });

    return jab.build();
  }


  // TODO: Task 4
  // You may change the signature of this method by passing any number of parameters
  // and returning any type
  public void generatePDFReport(int count) throws JRException {
    JsonObject job = Json.createObjectBuilder().add("name", reportName).add("batch", reportBatch).build();
    InputStream is = new ByteArrayInputStream(job.toString().getBytes());
    JsonDataSource reportDS = new JsonDataSource(is);

    JsonArrayBuilder jab = Json.createArrayBuilder();
    JsonArray jsonArray = getProlificDirectors(count);
    for (int i=0 ; i<jsonArray.size() ; i++){
      JsonObject job2 = jsonArray.getJsonObject(i);
      JsonObject director = Json.createObjectBuilder().add("director", job2.getString("director_name"))
                                                      .add("count", job2.getInt("movies_count"))
                                                      .add("revenue", job2.getJsonNumber("total_revenue").doubleValue())
                                                      .add("budget", job2.getJsonNumber("total_budget").doubleValue())
                                                      .build();
      jab.add(director);
    }
    JsonArray directorsArray = jab.build();
    
    InputStream is2 = new ByteArrayInputStream(directorsArray.toString().getBytes());
    JsonDataSource directorDS = new JsonDataSource(is2);

    Map<String, Object> params = new HashMap<>();
    params.put("DIRECTOR_TABLE_DATASET", directorDS);

    JasperReport report = JasperCompileManager.compileReport("data/director_movies_report.jrxml");
    JasperPrint print = JasperFillManager.fillReport(report, params, reportDS);

    JRPdfExporter exporter = new JRPdfExporter();
    exporter.setExporterInput(new SimpleExporterInput(print));
    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput("src/main/resources/static/report.pdf"));

    SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
    reportConfig.setSizePageToContent(true);
    reportConfig.setForceLineBreakPolicy(false);

    SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
    exportConfig.setEncrypted(false);

    exporter.setConfiguration(reportConfig);
    exporter.setConfiguration(exportConfig);

    exporter.exportReport();

    HtmlExporter htmlExporter = new HtmlExporter();
    htmlExporter.setExporterInput(new SimpleExporterInput(print));
    htmlExporter.setExporterOutput(new SimpleHtmlExporterOutput("src/main/resources/static/report.html"));
    htmlExporter.exportReport();
  }

}
