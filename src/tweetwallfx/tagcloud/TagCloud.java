/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.List;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import twitter.TwitterOAuth;
import twitter4j.conf.Configuration;

/**
 *
 * @author sven
 */
public class TagCloud extends Application {

    private Configuration conf;
//    private CLogOut log;
    private final String hashtag = "#netbeans";// #google";
    private TagTweets tweetsTask;
    
    @Override
    public void start(Stage primaryStage) {
        
//        try {
            BorderPane borderPane = new BorderPane();
            Scene scene = new Scene(borderPane, 800, 600);
            StopList.add(hashtag);
            
            /* TWITTER */
//        log=CLogOut.getInstance();
//        log.getMessages().addListener((ov,s,s1)->System.out.println(s1));
            
            final Service service=new Service<Void>(){
                @Override protected Task<Void> createTask() {   
                    Task<Void> task = new Task<Void>(){
                        @Override protected Void call() throws Exception {
                            final List<String> rawParameters = getParameters().getRaw();
                            conf = TwitterOAuth.getInstance(rawParameters.toArray(new String[rawParameters.size()])).readOAuth();
                            return null;
                        }
                    };
                    return task;
                }
            };
            
            service.setOnSucceeded(e->{
                if(!hashtag.isEmpty() && conf!=null){
                    tweetsTask= new TagTweets(conf, hashtag, borderPane);
                    tweetsTask.start();
                }
            });
            
            primaryStage.setTitle("The JavaFX Tweetwall for NetBeans Day!");
//            primaryStage.setTitle("The JavaFX Tweetwall for Devoxx!");
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setFullScreen(true);
            service.start();
//        } catch (IOException ex) {
//            Logger.getLogger(TagCloud.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    @Override
    public void stop() {
        System.out.println("closing...");
        if(tweetsTask!=null){
            tweetsTask.stop();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
