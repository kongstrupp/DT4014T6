import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.checkerframework.checker.units.qual.A;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;

/* class to demonstarte use of Drive files list API */
public class Client {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = new ArrayList<>();

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
        InputStream in = Client.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

    public void backUp(DriveUtilities driveUtilities, Drive service, String remoteFolderPath, String localFolderPath) throws IOException {

        // check if the folder have the same lenght in files
        ArrayList<File> googleDriveFolder = new ArrayList<>();
        java.io.File localFolder = new java.io.File(localFolderPath);
        java.io.File[] localFiles = localFolder.listFiles();

        FileList result = service.files().list()
                .setQ("mimeType = 'text/plain'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                googleDriveFolder.add(file);
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }


    }


    public static void main(String... args) throws IOException, GeneralSecurityException {

        SCOPES.add(DriveScopes.DRIVE_FILE);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        DriveUtilities driveUtilities = new DriveUtilities(service);
        String backUpFolderID = "1oVeAdW02f3yEqnksjTFFMjewKcdN51mM";


        ArrayList<File> driveFolder = new ArrayList<>();
        ArrayList<java.io.File> localFolder = new ArrayList<>();


        java.io.File folder = new java.io.File("C:\\DT4014T6\\src\\main\\java\\Files");
        java.io.File[] localFiles = folder.listFiles();

        for (java.io.File file : localFiles) {
            localFolder.add(file);
        }

        for (int i = 0; i < localFiles.length; i++) {
            driveUtilities.uploadToFolder(backUpFolderID, localFiles[i].getAbsolutePath());
        }

        FileList result = service.files().list()
                .setQ("mimeType = 'text/plain'")
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                driveFolder.add(file);
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }

        // Check if the same files exist
        localFolder.sort(Comparator.comparing(java.io.File::getName));
        driveFolder.sort(Comparator.comparing(File::getName));

        if (driveFolder.size() > localFolder.size()) {
            for (int i = 0; i < driveFolder.size(); i++) {
                if (i > (localFolder.size() + 1)) {
                    driveUtilities.deleteFile(driveFolder.get(i).getId());
                } else {
                    ByteArrayOutputStream byteArrayOutputStream = driveUtilities.downloadFile(driveFolder.get(i).getId());
                    byte[] localFileBytes = Files.readAllBytes(localFolder.get(i).toPath());
                    byte[] driveFileBytes = byteArrayOutputStream.toByteArray();

                    boolean check = Arrays.equals(localFileBytes, driveFileBytes);

                    if (!check) {
                        driveUtilities.deleteFile(driveFolder.get(i).getId());
                        driveUtilities.uploadToFolder(backUpFolderID, localFolder.get(i).getAbsolutePath());
                    }
                }
            }
        } else if (localFolder.size() > driveFolder.size()) {
            for (int i = 0; i < localFolder.size(); i++) {
                if (i > (driveFolder.size() + 1)) {
                    driveUtilities.uploadToFolder(backUpFolderID, localFolder.get(i).getAbsolutePath());
                } else {
                    ByteArrayOutputStream byteArrayOutputStream = driveUtilities.downloadFile(driveFolder.get(i).getId());
                    byte[] localFileBytes = Files.readAllBytes(localFolder.get(i).toPath());
                    byte[] driveFileBytes = byteArrayOutputStream.toByteArray();

                    boolean check = Arrays.equals(localFileBytes, driveFileBytes);

                    if (!check) {
                        driveUtilities.deleteFile(driveFolder.get(i).getId());
                        driveUtilities.uploadToFolder(backUpFolderID, localFolder.get(i).getAbsolutePath());
                    }
                }
            }
        } else if (localFolder.size() == driveFolder.size()) {
            for (int i = 0; i < localFolder.size(); i++) {
                ByteArrayOutputStream byteArrayOutputStream = driveUtilities.downloadFile(driveFolder.get(i).getId());
                byte[] localFileBytes = Files.readAllBytes(localFolder.get(i).toPath());
                byte[] driveFileBytes = byteArrayOutputStream.toByteArray();

                boolean check = Arrays.equals(localFileBytes, driveFileBytes);

                if (!check) {
                    driveUtilities.deleteFile(driveFolder.get(i).getId());
                    driveUtilities.uploadToFolder(backUpFolderID, localFolder.get(i).getAbsolutePath());
                }
            }
        }

    }
}



