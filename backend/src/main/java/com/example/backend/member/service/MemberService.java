package com.example.backend.member.service;

import com.example.backend.member.dto.*;
import com.example.backend.member.entity.Member;
import com.example.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtEncoder jwtEncoder;

    public void add(MemberForm memberForm) {
        validate(memberForm);

        Member member = new Member();
        member.setEmail(memberForm.getEmail().trim());
        member.setPassword(memberForm.getPassword().trim());
        member.setInfo(memberForm.getInfo());
        member.setNickName(memberForm.getNickName().trim());

        memberRepository.save(member);
    }

    private void validate(MemberForm memberForm) {
        String email = memberForm.getEmail().trim();
        String password = memberForm.getPassword().trim();
        String nickName = memberForm.getNickName().trim();

        // 이메일 검증
        if (email.isBlank()) {
            throw new RuntimeException("이메일을 입력해야 합니다.");
        }
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new RuntimeException("이메일 형식에 맞지 않습니다.");
        }
        if (memberRepository.findById(email).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 검증
        if (password.isBlank()) {
            throw new RuntimeException("비밀번호를 입력해야 합니다.");
        }
        String pwRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$";
        if (!Pattern.matches(pwRegex, password)) {
            throw new RuntimeException("비밀번호는 8자 이상이며, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        // 닉네임 검증
        if (nickName.isBlank()) {
            throw new RuntimeException("닉네임을 입력해야 합니다.");
        }
        String nickRegex = "^[가-힣a-zA-Z0-9]{2,20}$";
        if (!Pattern.matches(nickRegex, nickName)) {
            throw new RuntimeException("닉네임은 2~20자이며, 한글, 영문, 숫자만 사용할 수 있습니다.");
        }
        if (memberRepository.findByNickName(nickName).isPresent()) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
    }

    public List<MemberListInfo> list() {
        return memberRepository.findAllBy();
    }

    public MemberDto get(String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일의 회원이 존재하지 않습니다."));

        return MemberDto.builder()
                .email(member.getEmail())
                .nickName(member.getNickName())
                .info(member.getInfo())
                .insertedAt(member.getInsertedAt())
                .build();
    }

    public void delete(MemberForm memberForm) {
        Member member = memberRepository.findById(memberForm.getEmail())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!member.getPassword().equals(memberForm.getPassword())) {
            throw new RuntimeException("암호가 일치하지 않습니다.");
        }

        memberRepository.delete(member);
    }

    public void update(MemberForm memberForm) {
        Member member = memberRepository.findById(memberForm.getEmail())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!member.getPassword().equals(memberForm.getPassword())) {
            throw new RuntimeException("암호가 일치하지 않습니다.");
        }

        member.setNickName(memberForm.getNickName().trim());
        member.setInfo(memberForm.getInfo());

        memberRepository.save(member);
    }

    public void changePassword(ChangePasswordForm form) {
        Member member = memberRepository.findById(form.getEmail())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        if (!member.getPassword().equals(form.getOldPassword())) {
            throw new RuntimeException("이전 비밀번호가 일치하지 않습니다.");
        }

        member.setPassword(form.getNewPassword().trim());
        memberRepository.save(member);
    }

    public String getToken(MemberLoginForm loginForm) {
        Member member = memberRepository.findById(loginForm.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!member.getPassword().equals(loginForm.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 365)) // 1년
                .subject(member.getEmail())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
