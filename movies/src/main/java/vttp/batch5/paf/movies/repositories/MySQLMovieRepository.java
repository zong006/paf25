package vttp.batch5.paf.movies.repositories;

import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonObject;

@Repository
public class MySQLMovieRepository {

  @Autowired
  private JdbcTemplate template;

  // TODO: Task 2.3
  // You can add any number of parameters and return any type from the method
  public void batchInsertMovies(List<JsonObject> movies) {
    String SQL_BATCH_UPDATE = """
       insert into imdb 
       (imdb_id, vote_average, vote_count, release_date, revenue, budget, runtime)
       values 
       (?, ?, ?, ?, ?, ?, ?);
       """;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
       template.batchUpdate(SQL_BATCH_UPDATE, movies, 25, 
                            (PreparedStatement ps , JsonObject job) -> {
                              ps.setString(1, job.getString("imdb_id"));
                              ps.setInt(2, job.getInt("vote_average"));
                              ps.setInt(3, job.getInt("vote_count"));
                              try {
                                ps.setDate(4, new Date(sdf.parse(job.getString("release_date")).getTime()));
                              } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                              }
                              ps.setDouble(5, job.getJsonNumber("revenue").doubleValue());
                              ps.setDouble(6, job.getJsonNumber("budget").doubleValue());
                              ps.setInt(7, job.getInt("runtime"));
                            }
                          );
  }

  public int getEntryCount(){
    String SQL_GET_COUNT = """
        SELECT count(*) as entries from paf25.imdb i ;
        """;

    SqlRowSet rs = template.queryForRowSet(SQL_GET_COUNT);
    int entries = 0;
    while(rs.next()){
      entries = rs.getInt("entries");
    }
    return entries;
  }
  
  // TODO: Task 3

  public Map<String, BigDecimal> getRevenueAndBudgetById(String id){
    String SQL_REV_BUDGET_QUERY = """
        select revenue, budget from imdb
        where imdb_id = ?;
        """;

    Map<String, BigDecimal> revBud = new HashMap<>();
    Map<String, Object> result = template.queryForMap(SQL_REV_BUDGET_QUERY, id);

    revBud.put("revenue", (BigDecimal)result.get("revenue"));
    revBud.put("budget", (BigDecimal)result.get("budget"));
    // System.out.println(">>> id: " + id);
    // System.out.println(revBud);
    return revBud;
  }
}
