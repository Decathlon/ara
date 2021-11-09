package com.decathlon.ara.service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.core.GenericTypeResolver;
import org.springframework.transaction.annotation.Transactional;

import com.decathlon.ara.Messages;
import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.MemberRelationshipRepository;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;

@Transactional
public abstract class MemberService<C, M extends Member, R extends MemberRelationship<C, M>> {

    private static final Pattern CAMEL_CASE_WORD_FORMAT_PATTERN = Pattern.compile("([A-Za-z])([A-Z]+)");

    private MemberContainerRepository<C, ?> memberContainerRepository;
    private MemberRelationshipRepository<C, M, R> memberRelationshipRepository;
    private MemberRepository<M> memberRepository;

    private Class<C> containerClass;
    private Class<M> memberClass;
    private Class<R> relationshipClass;

    @SuppressWarnings("unchecked")
    protected MemberService(MemberContainerRepository<C, ?> memberContainerRepository, MemberRelationshipRepository<C, M, R> memberRelationshipRepository, MemberRepository<M> memberRepository) {
        this.memberContainerRepository = memberContainerRepository;
        this.memberRelationshipRepository = memberRelationshipRepository;
        this.memberRepository = memberRepository;
        Class<?>[] typeArgumentResolved = GenericTypeResolver.resolveTypeArguments(this.getClass(), MemberService.class);
        Objects.requireNonNull(typeArgumentResolved);
        containerClass = (Class<C>) typeArgumentResolved[0];
        memberClass = (Class<M>) typeArgumentResolved[1];
        relationshipClass = (Class<R>) typeArgumentResolved[2];
    }

    @Transactional(readOnly = true)
    public List<MemberDTO> findAll(String identifier) {
        return memberRelationshipRepository.findAllByContainerIdentifier(identifier).stream().map(memberRelationship -> new MemberDTO(memberRelationship.getMemberName(), memberRelationship.getRole())).toList();
    }

    @Transactional(readOnly = true)
    public MemberDTO findOne(String identifier, String memberName) throws NotFoundException {
        R memberRelationship = memberRelationshipRepository.findByContainerIdentifierAndMemberName(identifier, memberName);
        if (memberRelationship == null) {
            throw new NotFoundException(notFoundMessage(relationshipClass), makeIdentifier(relationshipClass));
        }
        return new MemberDTO(memberRelationship.getMemberName(), memberRelationship.getRole());
    }

    public MemberDTO addMember(String identifier, MemberDTO memberDto) throws BadRequestException {
        R memberRelationship = memberRelationshipRepository.findByContainerIdentifierAndMemberName(identifier, memberDto.getName());
        if (memberRelationship == null) {
            C container = memberContainerRepository.findByContainerIdentifier(identifier);
            if (container == null) {
                throw new NotFoundException(notFoundMessage(containerClass), makeIdentifier(containerClass));
            }
            M member = memberRepository.findByMemberName(memberDto.getName());
            if (member == null) {
                throw new NotFoundException(notFoundMessage(memberClass), makeIdentifier(memberClass));
            }
            memberRelationship = constructMember(container, member);
        } else {
            throw new BadRequestException(String.format(Messages.ALREADY_EXIST, toStringForMessage(relationshipClass)), makeIdentifier(relationshipClass), "already_exists");
        }
        memberRelationship.setRole(memberDto.getRole());
        memberRelationshipRepository.save(memberRelationship);
        afterAddMember(memberRelationship.getContainer(), memberRelationship.getMember());
        return memberDto;
    }

    public MemberDTO updateMemberRole(String identifier, String memberName, MemberRole role) throws BadRequestException {
        R memberRelationship = memberRelationshipRepository.findByContainerIdentifierAndMemberName(identifier, memberName);
        if (memberRelationship == null) {
            throw new NotFoundException(notFoundMessage(relationshipClass), makeIdentifier(relationshipClass));
        }
        memberRelationship.setRole(role);
        memberRelationshipRepository.save(memberRelationship);
        afterUpdateRole(memberRelationship.getContainer(), memberRelationship.getMember());
        return new MemberDTO(memberName, role);
    }

    public void deleteMember(String identifier, String memberName) throws BadRequestException {
        R memberRelationship = memberRelationshipRepository.findByContainerIdentifierAndMemberName(identifier, memberName);
        if (memberRelationship == null) {
            throw new NotFoundException(notFoundMessage(relationshipClass), makeIdentifier(relationshipClass));
        }
        memberRelationshipRepository.delete(memberRelationship);
        afterDeleteMember(memberRelationship.getContainer(), memberRelationship.getMember());
    }

    protected abstract R constructMember(C container, M member);

    protected abstract void afterAddMember(C container, M member);

    protected abstract void afterUpdateRole(C container, M member);

    protected abstract void afterDeleteMember(C container, M member);

    private String notFoundMessage(Class<?> entityClass) {
        return String.format(Messages.NOT_FOUND, toStringForMessage(entityClass));
    }

    private String makeIdentifier(Class<?> entityClass) {
        return CAMEL_CASE_WORD_FORMAT_PATTERN.matcher(entityClass.getSimpleName()).replaceAll("$1-$2").toLowerCase();
    }

    private String toStringForMessage(Class<?> entityClass) {
        return CAMEL_CASE_WORD_FORMAT_PATTERN.matcher(entityClass.getSimpleName()).replaceAll("$1 $2").toLowerCase();
    }

}
