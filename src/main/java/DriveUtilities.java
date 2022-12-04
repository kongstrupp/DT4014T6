import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class DriveUtilities {
    private Drive driveService;

    public DriveUtilities(Drive driveService) {
        this.driveService = driveService;
    }

    public String createFolder(String folderName) throws IOException {
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.
        // File's metadata.
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
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

    public File uploadToFolder(String realFolderId, String pathName) throws IOException {
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.
        // Build a new authorized API client service.
        // File's metadata.

        String fileName = pathName.substring(pathName.lastIndexOf("\\")+1);

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(realFolderId));



        if (fileExist(fileName)){
            System.out.println("File already exist: " + fileName);
            return null;
        }

        java.io.File filePath = new java.io.File(pathName);
        FileContent mediaContent = new FileContent("text/plain", filePath);


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

    public ByteArrayOutputStream downloadFile(String realFileId) throws IOException {
        /* Load pre-authorized user credentials from the environment.
           TODO(developer) - See https://developers.google.com/identity for
          guides on implementing OAuth2 for your application.*/

        try {
            OutputStream outputStream = new ByteArrayOutputStream();


            driveService.files().get(realFileId)
                    .executeMediaAndDownloadTo(outputStream);

/*
            driveService.files().export(realFileId,"text/plain")
                    .executeMediaAndDownloadTo(outputStream);
*/
            return (ByteArrayOutputStream) outputStream;
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to move file: " + e.getDetails());
            throw e;
        }
    }


    public boolean fileExist(String nameOfFile) throws IOException {
        List<File> files = new ArrayList<File>();

        String pageToken = null;
        do {
            FileList result = driveService.files().list()
                    .setQ("name = " + "'" + nameOfFile + "'")
                    .setSpaces("drive")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
               if (Objects.equals(file.getName(), nameOfFile)){
                    return true;
               }
            }

            files.addAll(result.getFiles());

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return false;
    }


    public void deleteFile(String fileId) {
        try {
            driveService.files().delete(fileId).execute();
        } catch (IOException e) {
            System.out.println("An error occurred: " + e);
        }
    }



}






