package twitter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * TweetWallFX - Devoxx 2014
 * {@literal @}johanvos {@literal @}SvenNB {@literal @}SeanMiPhillips {@literal @}jdub1581 {@literal @}JPeredaDnr
 * 
 * Place your oauth credentials in a properties file:
 
# Step 1. Sign in in https://dev.twitter.com
# Step 2. Create an app in https://apps.twitter.com
# 2.1 Name: DevoxxTweetWall
# 2.2 Description: JavaFX based application for displaying 3D rotating tweets at Devoxx 2014
# 2.3 Website: (add your website or the link of your repository, for instance)
# 3. When the application is created, these two keys are generated:
# Step 3.1 Assing API Key to oauth.consumerKey
# Step 3.2 Assing Api secret to oauth.consumerSecret
# Step 4. Click on create an Access token. Two new keys are generated:
# Step 4.1 Assign Access token to oauth.accessToken
# Step 4.2 Assign Acces token secret to oauth.accessTokenSecret
 * 
 * Don't share this credentials with anybody, don't commit the properties file to the repo !!
 * @author jpereda
 */
public class TwitterOAuth {
    
    private String error="";
    private Configuration conf=null;
    
    private static TwitterOAuth instance=null;
        
    public static TwitterOAuth getInstance() { 
        if(instance==null){
            instance=new TwitterOAuth();
        } 
        return instance;
    }
    
    private TwitterOAuth(){
        Properties props = new Properties();
        
        try {
            /* MyRealOAuth.properties -> this file is not commited to the repo. 
               Ask for it or provide your own keys. */
            props.load(TwitterOAuth.class.getResourceAsStream("MyOAuth.properties"));
        } catch (FileNotFoundException ex) {
            System.out.println("Error finding properties file: "+ ex);
        } catch (IOException ex) {
            System.out.println("Error loading properties file: "+ ex);
        }
        
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(false);
        builder.setOAuthConsumerKey(props.getProperty("oauth.consumerKey"));
        builder.setOAuthConsumerSecret(props.getProperty("oauth.consumerSecret"));
        builder.setOAuthAccessToken(props.getProperty("oauth.accessToken"));
        builder.setOAuthAccessTokenSecret(props.getProperty("oauth.accessTokenSecret"));
        conf = builder.build();
        
        // check Configuration
        if(conf.getOAuthConsumerKey()!=null && !conf.getOAuthConsumerKey().isEmpty() && 
           conf.getOAuthConsumerSecret()!=null && !conf.getOAuthConsumerSecret().isEmpty() &&
           conf.getOAuthAccessToken()!=null && !conf.getOAuthAccessToken().isEmpty() && 
           conf.getOAuthAccessTokenSecret()!=null && !conf.getOAuthAccessTokenSecret().isEmpty()){
            Twitter twitter = new TwitterFactory(conf).getInstance();
            try {
                User user = twitter.verifyCredentials();
                System.out.println("User "+user.getName()+" validated");
                error="";
            } catch (TwitterException ex) {
                error="Error: "+ex.getErrorMessage();
                //  statusCode=400, message=Bad Authentication data -> wrong token
                //  statusCode=401, message=Could not authenticate you ->wrong consumerkey
                System.out.println("Error credentials: "+ex.getStatusCode()+" "+ex.getErrorMessage());
                conf=null;
            }
        } else {
            error="Error: Missing credentials";
        }
    }
    
    public Configuration readOAuth() { return conf; }
    
    public String getError(){ return error; }
    
}
