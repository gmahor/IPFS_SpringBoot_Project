package com.ipfs.services;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class IpfsService {

    public static final String FILE_PARAMETER_NAME = "file";

    @Value(value = "${multipleFileUploadIpfsUrl}")
    public String MULTIPLE_FILE_UPLOAD_URL;

    @Value("${singleFileUploadIpfsUrl}")
    public String SINGLE_FILE_UPLOAD_URL;

    @Value("${getIpfsFile}")
    public String GET_IPFS_FILE;

    private final RestTemplate restTemplate;


    public IpfsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadSingleFileOnIpfs(MultipartFile multipartFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            ContentDisposition contentDisposition = ContentDisposition
                    .builder("form-data")
                    .name(FILE_PARAMETER_NAME)
                    .filename(Objects.requireNonNull(multipartFile.getOriginalFilename()))
                    .build();
            fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(multipartFile.getBytes(), fileMap);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add(FILE_PARAMETER_NAME, fileEntity);
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    SINGLE_FILE_UPLOAD_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            if (response.getStatusCodeValue() == 200) {
                return this.getFileHashFromResp(response.getBody());
            }
        } catch (HttpClientErrorException | IOException e) {
            log.error("Error while uploading file on ipfs - ", e);
        }
        return null;
    }


    public String multipleFileUpload(List<MultipartFile> multipartFiles) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        multipartFiles.forEach(
                file -> {
                    try {
                        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
                        ContentDisposition contentDisposition = ContentDisposition
                                .builder("form-data")
                                .name(FILE_PARAMETER_NAME)
                                .filename(Objects.requireNonNull(file.getOriginalFilename()))
                                .build();
                        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
                        HttpEntity<byte[]> fileEntity = new HttpEntity<>(file.getBytes(), fileMap);
                        body.add(FILE_PARAMETER_NAME, fileEntity);
                    } catch (Exception e) {
                        log.error("Error while upload file on ipfs :", e);
                    }
                }
        );
        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    MULTIPLE_FILE_UPLOAD_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            if (response.getStatusCodeValue() == 200 && response.getBody() != null) {
                return this.getDictHashFromResp(response.getBody());
            }
        } catch (HttpClientErrorException e) {
            log.error("Error : ", e);
        }
        return null;
    }


    public File convertMultipartFile(MultipartFile file) {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            return convFile;
        } catch (Exception e) {
            log.error("Error while converting multipart file to file - ", e);
        }
        return null;
    }


    private String getDictHashFromResp(String resp) {
        if (resp != null) {
            String[] items = resp.split("\n");
            String asJSONArrayString = Arrays.toString(items);
            JSONArray jsonArray = new JSONArray(asJSONArrayString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(jsonArray.length() - 1);
                if (jsonObject.get("Name").equals("")) {
                    String hash = jsonObject.get("Hash").toString();
                    log.info(GET_IPFS_FILE + hash);
                    return GET_IPFS_FILE + hash;
                }
            }
        }
        return null;
    }

    private String getFileHashFromResp(String resp) {
        if (resp != null) {
            JSONObject jsonObject = new JSONObject(resp);
            String fileHash = jsonObject.get("Hash").toString();
            log.info(GET_IPFS_FILE + fileHash);
            return GET_IPFS_FILE + fileHash;
        }
        return null;
    }

    public String uploadFileFromFolder() {
        final File folder = new File("E:/IpfsProject/fileDict");
        return this.uploadFileOnIpfs(folder);
    }

    private String uploadFileOnIpfs(File folder) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            for (final File file : Objects.requireNonNull(folder.listFiles())) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
                ContentDisposition contentDisposition = ContentDisposition
                        .builder("form-data")
                        .name(FILE_PARAMETER_NAME)
                        .filename(Objects.requireNonNull(file.getName()))
                        .build();
                fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
                HttpEntity<byte[]> fileEntity = new HttpEntity<>(bytes, fileMap);
                body.add(FILE_PARAMETER_NAME, fileEntity);
            }
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    MULTIPLE_FILE_UPLOAD_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            if (response.getStatusCodeValue() == 200 && response.getBody() != null) {
                return this.getDictHashFromResp(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error while uploading files on ipfs - ", e);
        }
        return null;
    }
}
