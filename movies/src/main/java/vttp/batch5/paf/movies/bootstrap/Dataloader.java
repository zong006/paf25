package vttp.batch5.paf.movies.bootstrap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.batch5.paf.movies.services.MovieService;

@Component
public class Dataloader {

  //TODO: Task 2

  @Autowired
  private MovieService movieService;

  public void loadIntoDb(String filePath) throws IOException, ParseException{
    JsonArray jsonArray = openZipFile(filePath);
    List<JsonObject> movies = jsonArray.stream().map(x -> x.asJsonObject()).toList();
    movieService.loadIntoDb(movies); 
  }


  public JsonArray openZipFile(String filePath) throws IOException, ParseException{
    Path p = Paths.get(filePath);
    InputStream is = new FileInputStream(p.toFile());
    ZipInputStream zis = new ZipInputStream(is);
    zis.getNextEntry();
    
    InputStreamReader isr = new InputStreamReader(zis);
    BufferedReader br = new BufferedReader(isr);
    
    String line = "";
    JsonArrayBuilder jab = Json.createArrayBuilder();

    while ((line=br.readLine())!=null ){
      
      InputStream inputStream = new ByteArrayInputStream(line.getBytes());
      JsonReader jr = Json.createReader(inputStream);
      JsonObject job = jr.readObject();

      String releaseDate = job.getString("release_date");

      if (isAfter2018(releaseDate)){
        jab.add(job);
      }
    }
    is.close();
    JsonArray jsonArray = jab.build();
  
    return jsonArray;
  }

  private boolean isAfter2018(String dateString) throws ParseException{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date date = sdf.parse(dateString);

    Calendar relDate = Calendar.getInstance();
    relDate.setTime(date);

    return relDate.get(Calendar.YEAR) >= 2018;
    
  }
}
