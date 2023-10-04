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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class Application {

    static final String APPLICATION_NAME = "exqudens-java-google";

    public static void run(String... args) {
        try {
            String jsonPath = null;
            String tokensPath = "tmp";

            for (String arg : args) {
                log.info("arg: '" + arg + "'");
            }

            if (args.length > 0) {
                jsonPath = args[0];
            }

            if (args.length > 1) {
                tokensPath = args[1];
            }

            log.info("jsonPath: {}'",  jsonPath);
            log.info("tokensPath: '{}'", tokensPath);

            List<String> results = listFiles(jsonPath, tokensPath);
            if (results.isEmpty()) {
                System.out.println("<NO_FILES_FOUND>");
            } else {
                for (String result : results) {
                    System.out.println(result);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static List<String> listFiles(
            final String jsonPath,
            final String tokensPath
    ) throws Exception {
        Objects.requireNonNull(jsonPath);
        List<String> results = new ArrayList<>();

        // Read json file content.
        String json = Files.readString(Path.of(jsonPath));

        // Global instance of the JSON factory.
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // Build a new authorized API client service.
        NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        List<String> scopes = List.of(DriveScopes.DRIVE_METADATA_READONLY);
        Credential credential = getCredentials(json, jsonFactory, netHttpTransport, scopes, tokensPath);
        Drive service = new Drive.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                results.add(file.getName() + " (" + file.getId() + ")");
            }
        }
        return results;
    }

    private static Credential getCredentials(
            final String json,
            final JsonFactory jsonFactory,
            final NetHttpTransport netHttpTransport,
            final List<String> scopes,
            final String tokensDirectoryPath
    ) throws Exception {
        Credential credential;

        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(json));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return credential;
    }

}
