package com.johansen.management_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johansen.management_app_backend.model.Member;
import com.johansen.management_app_backend.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @PostMapping
    public ResponseEntity<Member> createMember(
            @RequestPart("member") String memberJson,
            @RequestParam("picture") MultipartFile picture) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Member member = objectMapper.readValue(memberJson, Member.class);

            String pictureUrl = memberService.uploadPicture(picture);
            member.setPictureUrl(pictureUrl);

            Member createdMember = memberService.createMember(member);
            return new ResponseEntity<>(createdMember, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Test endpoint reached");
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        System.out.println("Received request to get all members");

        List<Member> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Optional<Member> member = memberService.getMemberById(id);
        return member.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
