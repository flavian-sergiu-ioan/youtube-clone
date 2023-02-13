package com.microservices.youtubeclone.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service implements FileService {

    public static final String YOUTUBE_CLONE_APP_BUCKET = "youtube-clone-app-bucket";
    private final AmazonS3Client amazonS3Client;

    @Override
    public String uploadFile(final MultipartFile multipartFile) {
        var filenameExtension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        var key = UUID.randomUUID() + filenameExtension;
        var metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        try {
            amazonS3Client.putObject(YOUTUBE_CLONE_APP_BUCKET, key, multipartFile.getInputStream(), metadata);
        } catch (IOException ioException) {
            throw new ResponseStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "An exception occurred while uploading a file", ioException);
        }
        amazonS3Client.setObjectAcl(YOUTUBE_CLONE_APP_BUCKET, key, CannedAccessControlList.PublicRead);

        return amazonS3Client.getResourceUrl(YOUTUBE_CLONE_APP_BUCKET, key);
    }
}
