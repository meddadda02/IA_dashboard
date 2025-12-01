package org.example.ia_dashboard.service;

import org.example.ia_dashboard.Entity.File;
import org.example.ia_dashboard.Entity.User;
import org.example.ia_dashboard.Repository.FileRepository;
import org.example.ia_dashboard.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    public File store(MultipartFile file, String username) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        File fileEntity = File.builder()
                .name(fileName)
                .type(file.getContentType())
                .data(file.getBytes())
                .user(user)
                .build();

        return fileRepository.save(fileEntity);
    }

    public File getFile(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id " + id));
    }

    public List<File> getAllFilesByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return fileRepository.findByUserId(user.getId());
    }
}
