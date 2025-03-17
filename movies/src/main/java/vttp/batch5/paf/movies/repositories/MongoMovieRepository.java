package vttp.batch5.paf.movies.repositories;

import java.util.List;
import org.bson.Document;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.stereotype.Repository;

@Repository
public class MongoMovieRepository {

    @Autowired
    private MongoTemplate template;

 // TODO: Task 2.3
 // You can add any number of parameters and return any type from the method
 // You can throw any checked exceptions from the method
 // Write the native Mongo query you implement in the method in the comments
 //
 //    native MongoDB query here
 
 /*
    db.getCollection("imdb").insertMany(
        [
            { movie 1},
            { movie 2}, 
            ...
        ]
    )

 */
 public void batchInsertMovies(List<Document> movies) {
    template.insert(movies, "imdb");
 }

 public boolean isEmpty(){
    List<Document> results = template.findAll(Document.class, "imdb");
    return results.size()==0;
 }

 // TODO: Task 2.4
 // You can add any number of parameters and return any type from the method
 // You can throw any checked exceptions from the method
 // Write the native Mongo query you implement in the method in the comments
 //
 //    native MongoDB query here
 //
 public void logError() {

 }

 // TODO: Task 3
 // Write the native Mongo query you implement in the method in the comments
 //
 //    native MongoDB query here
 /*
    db.getCollection("imdb").aggregate([
        {
            $unwind: "$directors"
        },
        {
            $group : {
                _id : "$directors",
                "movies_count" : {$sum:1},
                "ids" : {
                    $push: "$_id"
                }
            }
        },
        {
            $sort : {"movies_count" : -1}
        },
        {
            $skip : 1
        },
        {
            $limit : << size from service layer>>
        }
    ])
 */
    public List<Document> getDirectors(int count){
        AggregationOperation unwindOperation = Aggregation.unwind("directors");
        GroupOperation groupOperation = Aggregation.group("directors").push("_id").as("ids").count().as("movies_count");
        SortOperation sortOperation = Aggregation.sort(Sort.by(Direction.DESC, "movies_count"));
        SkipOperation skipOperation = Aggregation.skip(1);
        LimitOperation limitOperation = Aggregation.limit(count);

        Aggregation pipeline = Aggregation.newAggregation(unwindOperation, groupOperation, sortOperation, skipOperation, limitOperation);
        List<Document> results = template.aggregate(pipeline, "imdb", Document.class).getMappedResults();

        return results;
    }

}
