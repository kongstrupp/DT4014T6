import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/* Class to demonstrate Drive's upload to folder use-case. */
public class UploadToFolder {

    private Drive driveService;


    public UploadToFolder(Drive driveService) {
        this.driveService = driveService;
    }


    /**
     * Upload a file to the specified folder.
     *
     * @param realFolderId Id of the folder.
     * @return Inserted file metadata if successful, {@code null} otherwise.
     * @throws IOException if service account credentials file not found.
     */
    public File uploadToFolder(String realFolderId) throws IOException {
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.

        // Build a new authorized API client service.
        // File's metadata.
        File fileMetadata = new File();
        fileMetadata.setName("kuk.txt");
        fileMetadata.setParents(Collections.singletonList(realFolderId));
        java.io.File filePath = new java.io.File("C:\\DT4014T6\\src\\main\\java\\kuk.txt");
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        try {
            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, parents")
                    .execute();
            System.out.println("File ID: " + file.getId());
            return file;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            throw e;
        }
    }
}