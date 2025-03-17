package vttp.batch5.paf.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import vttp.batch5.paf.movies.bootstrap.Dataloader;

@SpringBootApplication
public class MoviesApplication implements CommandLineRunner{

	@Autowired
	private Dataloader dataloader;
	public static void main(String[] args){
		
		SpringApplication.run(MoviesApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		ApplicationArguments cliOpts = new DefaultApplicationArguments(args);
		// cliOpts.getOptionNames().forEach(x -> System.out.println(x));
		String filePath = "../data/movies_post_2010.zip";
		if (cliOpts.containsOption("file")){
			
			filePath = cliOpts.getOptionValues("file").get(0);
			System.out.println(filePath);
		}
		
		dataloader.loadIntoDb(filePath);
		
		
		System.out.println("============== testing ===============");
	}

}
