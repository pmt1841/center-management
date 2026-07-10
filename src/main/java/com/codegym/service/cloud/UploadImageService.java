package com.codegym.service.cloud;

import okhttp3.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

public class UploadImageService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String SUPABASE_URL = AppConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = AppConfig.SUPABASE_KEY;

    public static String uploadFile(MultipartFile file, String bucketName) {
        String imageUrl = "";

        if (file != null && !file.isEmpty()) {
            try {
                // Lấy tên file gốc
                String fileName = Paths.get(Objects.requireNonNull(file.getOriginalFilename())).getFileName().toString();
                String uniqueFileName = UUID.randomUUID() + "-" + fileName;

                // Lấy contentType (image/png, image/jpeg, ...)
                String contentType = file.getContentType();

                // Gọi hàm upload
                boolean uploadSuccess = uploadToSupabase(file.getInputStream(), uniqueFileName, contentType, bucketName);

                if (uploadSuccess) {
                    imageUrl = SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + uniqueFileName;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return imageUrl;

    }

    private static boolean uploadToSupabase(InputStream fileStream, String fileName, String contentType, String bucketName) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096]; // Đọc mỗi lần 4KB

            while ((nRead = fileStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileBytes = buffer.toByteArray();

            String url = SUPABASE_URL + "/storage/v1/object/" + bucketName + "/" + fileName;
            RequestBody requestBody = RequestBody.create(fileBytes, MediaType.parse(contentType));

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                    .addHeader("Content-Type", contentType)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    assert response.body() != null;
                    System.err.println("Supabase Error: " + response.code() + " - " + response.body().string());
                }
                return response.isSuccessful();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
