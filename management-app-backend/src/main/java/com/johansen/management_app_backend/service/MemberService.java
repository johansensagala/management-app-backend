package com.johansen.management_app_backend.service;

import com.johansen.management_app_backend.model.Member;
import com.johansen.management_app_backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    private final String uploadDir;
    private final String baseUrl = "http://localhost:8080";

    @Autowired
    public MemberService(ServletContext servletContext) {
        this.uploadDir = servletContext.getRealPath("/uploads/");
        System.out.println("Upload directory: " + uploadDir);
    }

    public String uploadPicture(MultipartFile picture) throws IOException {
        File directory = new File(uploadDir);

        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Created upload directory: " + uploadDir);
        }

        String fileName = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
        File file = new File(directory, fileName);

        picture.transferTo(file);
        System.out.println("File uploaded: " + file.getAbsolutePath());

        return "/uploads/" + fileName;
    }

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll().stream()
                .peek(member -> member.setPictureUrl(baseUrl + member.getPictureUrl()))
                .collect(Collectors.toList());
    }

    public Optional<Member> getMemberById(Long id) {
        Optional<Member> memberOpt = memberRepository.findById(id);
        memberOpt.ifPresent(member -> member.setPictureUrl(baseUrl + member.getPictureUrl()));
        return memberOpt;
    }
}
