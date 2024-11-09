package com.example.cliniccare.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {
    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(fileName, file.getInputStream(), file.getContentType());

        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucket.getName(), fileName);
    }

    public String updateImage(MultipartFile file, String oldImageUrl) throws IOException {
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            String oldFileName = oldImageUrl.substring(oldImageUrl.lastIndexOf("/") + 1, oldImageUrl.indexOf("?"));
            Bucket bucket = StorageClient.getInstance().bucket();
            bucket.get(oldFileName).delete();
        }

        return uploadImage(file);
    }
}