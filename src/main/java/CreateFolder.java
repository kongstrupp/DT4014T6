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

/* Class to demonstrate use of Drive's create folder API */
public class CreateFolder {

  private Drive driveService;


  public CreateFolder(Drive driveService) {
    this.driveService = driveService;
  }


  /**
   * Create new folder.
   *
   * @return Inserted folder id if successful, {@code null} otherwise.
   * @throws IOException if service account credentials file not found.
   */
  public String createFolder() throws IOException {
    // Load pre-authorized user credentials from the environment.
    // TODO(developer) - See https://developers.google.com/identity for
    // guides on implementing OAuth2 for your application.
    // File's metadata.
    File fileMetadata = new File();
    fileMetadata.setName("Test");
    fileMetadata.setMimeType("application/vnd.google-apps.folder");
    try {
      File file = driveService.files().create(fileMetadata)
          .setFields("id")
          .execute();
      System.out.println("Folder ID: " + file.getId());
      return file.getId();
    } catch (GoogleJsonResponseException e) {
      // TODO(developer) - handle error appropriately
      System.err.println("Unable to create folder: " + e.getDetails());
      throw e;
    }
  }

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