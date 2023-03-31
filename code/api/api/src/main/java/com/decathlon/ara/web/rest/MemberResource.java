package com.decathlon.ara.web.rest;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.service.MemberService;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

public abstract class MemberResource<C, M extends Member, R extends MemberRelationship<C, M>> {

    protected static final String BASE_PATH = "/members";

    private MemberService<C, M, R> memberService;

    protected MemberResource(MemberService<C, M, R> memberService) {
        this.memberService = memberService;
    }

    protected abstract String getIdentifier(Map<String, String> pathVariables);

    @GetMapping
    public List<MemberDTO> getAll(@PathVariable Map<String, String> pathVariables) {
        return memberService.findAll(getIdentifier(pathVariables));
    }

    @GetMapping("/{memberName}")
    public MemberDTO get(@PathVariable Map<String, String> pathVariables, @PathVariable String memberName) throws NotFoundException {
        return memberService.findOne(getIdentifier(pathVariables), memberName);
    }

    @PostMapping
    public ResponseEntity<MemberDTO> addMember(@PathVariable Map<String, String> pathVariables, @Valid @RequestBody MemberDTO memberDto) throws BadRequestException {
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequestUri().scheme(null).host(null).port(null).path("/" + memberDto.getName()).build().toUri()).body(memberService.addMember(getIdentifier(pathVariables), memberDto));
    }

    @PatchMapping("/{memberName}")
    public MemberDTO updateMemberRole(@PathVariable Map<String, String> pathVariables, @PathVariable String memberName, @Valid @RequestBody MemberDTO memberDto) throws BadRequestException {
        return memberService.updateMemberRole(getIdentifier(pathVariables), memberName, memberDto.getRole());
    }

    @DeleteMapping("/{memberName}")
    public void deleteMember(@PathVariable Map<String, String> pathVariables, @PathVariable String memberName) throws BadRequestException {
        memberService.deleteMember(getIdentifier(pathVariables), memberName);

    }

}
