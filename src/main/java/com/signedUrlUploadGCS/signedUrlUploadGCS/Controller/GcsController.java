package com.signedUrlUploadGCS.signedUrlUploadGCS.Controller;

import com.google.cloud.storage.StorageException;
import com.signedUrlUploadGCS.signedUrlUploadGCS.Service.GcsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.logging.Logger;

@RestController
@Slf4j
public class GcsController {

    @Autowired
    private GcsService gcsService;


    @PutMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("objectName") String objectName) {
        try {
            String contentType = file.getContentType();
            URL signedUrl = gcsService.generateV4PutObjectSignedUrl(objectName,contentType);
            log.info("Signed Url for upload: {}", signedUrl);

            HttpURLConnection connection = (HttpURLConnection) signedUrl.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
//            connection.setRequestProperty("Content-Type", contentType);


            try (OutputStream os = connection.getOutputStream()) {
                os.write(file.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return ResponseEntity.ok("File uploaded successfully.");
            } else {
                return ResponseEntity.status(responseCode).body("Failed to upload file.");
            }
        } catch (StorageException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        }
    }
}
