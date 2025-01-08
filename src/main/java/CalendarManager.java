import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class CalendarManager{
    /**
     * Your calendar.
    */
    private List<Event> items = null;
    /**
     * Application name.
     */
    private static String APPLICATION_NAME = "My Application";
    /**
     * Global instance of the JSON factory.
     */
    private static JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Constructor that customizes the application name.
     * 
     * @param appName customized application name.
     */
    public CalendarManager(String appName){
        APPLICATION_NAME = appName;
    }

    /**
     * Constructor that customizes the application name and the JsonFactory used.
     * 
     * @param appName customized application name.
     * @param factory customized JsonFactory.
     */
    public CalendarManager(String appName,JsonFactory factory){
        APPLICATION_NAME = appName;
        JSON_FACTORY = factory;
    }

    /**
     * Constructor that customizes the application name, the JsonFactory used
     * and the directory to store authorization tokens for this application.
     * 
     * @param appName customized application name.
     * @param factory customized JsonFactory.
     * @param token_dir directory to store authorization tokens for this application
     */
    public CalendarManager(String appName,JsonFactory factory,String token_dir){
        APPLICATION_NAME = appName;
        JSON_FACTORY = factory;
        TOKENS_DIRECTORY_PATH = token_dir;
    }

    /**
     * Constructor that customizes the application name, the JsonFactory used
     * and the directory to store authorization tokens for this application.
     * 
     * @param appName customized application name.
     * @param token_dir directory to store authorization tokens for this application
     */
    public CalendarManager(String appName,String token_dir){
        APPLICATION_NAME = appName;
        TOKENS_DIRECTORY_PATH = token_dir;
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
        Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
    throws IOException {
        // Load client secrets.
        InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    /** 
     * Updates the local calendar.
     * 
     * @param start start of the events you are looking for
     * @param end end of the events you are looking for
     * @throws IOException If the credentials.json file cannot be found.
     * @throws GeneralSecurityException If the HTTP connection was not secure.
     */
    private void getCalendar(DateTime start, DateTime end) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service =
            new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Events events = service.events().list("primary")
            .setTimeMin(start)
            .setTimeMax(end)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute();
        items = events.getItems();
        HTTP_TRANSPORT.shutdown();
    }

    /**
     * Returns the size of the saved calendar.
     */
    private int getCalendarLength(){
        if (items==null){
            System.out.println("There is no calendar saved.");
            return 0;
        }
        else{
            return items.size();
        }
    }

    /**
     * Returns the calendar as an array.
     * Returns null if the calendar was not initialised.
     */
    private Event[] getCalendarAsArray(){
        if (items==null){
            System.out.println("There is no calendar saved.");
            return null;
        }
        else{
            return (Event[]) items.toArray();
        }
    }

    /**
     * Returns the calendar as a list.
     * May modify the calendar if the result is modified.
     */
    private List<Event> getCalendarAsList(){
        return items;
    }

    /**
     * Returns a json representing the event ev as presented by google Calendar.
     * 
     * @param ev event you want the json representation of.
     */
    private static String EventToString(Event ev){
        return ev.toString();
    }

    /**
     *  Shows all the recorded event's summary, start time and end time in multiple lines 
     * each in a line (separated by "\n"). The first line gives context.
     */
    private String ShowEvent(){
        if (items ==null){
            return "Calendar not initialised.";
        }
        else{
            String msg="";
            if (items.isEmpty()) {
                msg = "No upcoming events found.";
            } else {
                msg = "Upcoming events:";
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                    start = event.getStart().getDate();
                    }
                    DateTime end = event.getEnd().getDateTime();
                    if (end == null) {
                    end = event.getStart().getDate();
                    }
                    msg+="\n"+ event.getSummary() + " starts at " + start + " ends at "+end;
                }
            }
            return msg;
        }
    }

    /**
     *  Shows all the recorded event as jsons each in a line (separated by "\n").
     * The first line gives context.
     */
    private String toString(){
        if (items ==null){
            return "Calendar not initialised";
        }
        else{
            String msg="";
            if (items.isEmpty()) {
                msg = "No upcoming events found.";
            } else {
                msg = "Upcoming events:";
                for (Event event : items) {
                    msg+="\n" + event.toString();
                }
            }
            return msg;
        }
    }

}