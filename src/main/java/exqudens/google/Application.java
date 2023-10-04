package exqudens.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class Application {

    static final String APPLICATION_NAME = "exqudens-java-google";

    public static void run(String... args) {
        try {
            System.out.println("-".repeat(10));
            for (String arg : args) {
                System.out.println(arg);
            }
            System.out.println("-".repeat(10));
            /*
            // Global instance of the JSON factory.
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            // Build a new authorized API client service.
            NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            List<String> scopes = List.of(DriveScopes.DRIVE_METADATA_READONLY);
            Credential credential = getCredentials(jsonFactory, netHttpTransport, scopes);
            Drive service = new Drive.Builder(netHttpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Print the names and IDs for up to 10 files.
            FileList result = service.files().list()
                    .setPageSize(10)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
            }
            */
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static Credential getCredentials(
            final JsonFactory jsonFactory,
            final NetHttpTransport netHttpTransport,
            final List<String> scopes
    ) throws Exception {
        Credential credential;
        String json = "";

        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(json));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return credential;
    }

}
