package com.ipfs.controllers;

import com.ipfs.services.IpfsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
public class IpfsController {

    private final IpfsService ipfsService;

    public IpfsController(IpfsService ipfsService) {
        this.ipfsService = ipfsService;
    }

    @PostMapping(value = "uploadFileOnIpfs")
    public ResponseEntity<Object> uploadFileOnIpfs(@RequestParam(name = "file") MultipartFile multipartFile) {
        try {
            String ipfsUrl = ipfsService.uploadSingleFileOnIpfs(multipartFile);
            if (ipfsUrl != null) {
                return new ResponseEntity<>(ipfsUrl, HttpStatus.OK);
            }
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Something went wrong ", e);
        }
        return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "uploadMultipleFileOnIpfs")
    public ResponseEntity<Object> uploadMultipleFileOnIpfs(@RequestParam(name = "files") List<MultipartFile> multipartFile) {
        try {
            String ipfsDictUrl = ipfsService.multipleFileUpload(multipartFile);
            if (ipfsDictUrl != null) {
                return new ResponseEntity<>(ipfsDictUrl, HttpStatus.OK);
            }
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Something went wrong ", e);
        }
        return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "uploadFileFromFolderInIPFS")
    public ResponseEntity<Object> uploadFileFromFolderInIPFS() {
        try {

            String ipfsDicUrl = ipfsService.uploadFileFromFolder();
            if (ipfsDicUrl != null) {
                return new ResponseEntity<>(ipfsDicUrl, HttpStatus.OK);
            }
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Something went wrong ", e);
        }
        return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
    }


}
