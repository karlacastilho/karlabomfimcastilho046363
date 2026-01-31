package com.karlacastilho.apiseletivo.storage;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class AlbumCoverStorageService {

    private final MinioClient minio;
    private final String bucket;
    private final String publicUrl;

    public AlbumCoverStorageService(MinioClient minio,
                                    @Value("${minio.bucket}") String bucket,
                                    @Value("${minio.publicUrl}") String publicUrl) {
        this.minio = minio;
        this.bucket = bucket;
        this.publicUrl = publicUrl;
    }

    public String uploadCover(Long albumId, MultipartFile file) {
        validateImage(file);

        ensureBucketExists();

        String ext = guessExtension(file.getOriginalFilename());
        String objectKey = "albums/" + albumId + "/cover-" + UUID.randomUUID() + ext;

        try (InputStream in = file.getInputStream()) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar arquivo para o MinIO", e);
        }
    }

    public String presignedGetUrl(String objectKey, Duration expiry) {
        try {
            int seconds = (int) expiry.getSeconds();
            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(seconds)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar URL pré-assinada", e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao verificar/criar bucket no MinIO", e);
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo é obrigatório");
        }
        String ct = file.getContentType();
        if (ct == null || (!ct.equals(MediaType.IMAGE_JPEG_VALUE) && !ct.equals(MediaType.IMAGE_PNG_VALUE))) {
            throw new IllegalArgumentException("Apenas PNG ou JPEG são aceitos");
        }
        // opcional: limite (ex: 5MB)
        long max = 5 * 1024 * 1024;
        if (file.getSize() > max) {
            throw new IllegalArgumentException("Arquivo muito grande (máx 5MB)");
        }
    }

    private String guessExtension(String filename) {
        if (filename == null) return "";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".jpg")) return ".jpg";
        if (lower.endsWith(".jpeg")) return ".jpeg";
        return "";
    }

    public String uploadAlbumImage(Long albumId, MultipartFile file) {
        validateImage(file);
        ensureBucketExists();

        String ext = guessExtension(file.getOriginalFilename());
        String objectKey = "albums/" + albumId + "/images/" + UUID.randomUUID() + ext;

        try (InputStream in = file.getInputStream()) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar arquivo para o MinIO", e);
        }
    }
}