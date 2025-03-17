package vttp.batch5.paf.movies.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.JsonArray;
import net.sf.jasperreports.engine.JRException;
import vttp.batch5.paf.movies.services.MovieService;

@RestController
@RequestMapping("/api")
public class MainController {

  @Autowired
  private MovieService movieService;

  

  // TODO: Task 3
  @GetMapping("/summary")
  public ResponseEntity<String> getDirectorDetails(@RequestParam int count){
    
    JsonArray directorsInfo = movieService.getProlificDirectors(count);
    return ResponseEntity.ok().body(directorsInfo.toString());
  }

  
  // TODO: Task 4
  @GetMapping("/summary/pdf")
  public void generatePDF(@RequestParam int count) throws JRException{
    movieService.generatePDFReport(count);
  }

}
