package com.decathlon.ara.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.decathlon.ara.cache.CacheService;
import com.decathlon.ara.domain.Member;
import com.decathlon.ara.domain.MemberContainerRepository;
import com.decathlon.ara.domain.MemberRelationship;
import com.decathlon.ara.domain.enumeration.MemberRole;
import com.decathlon.ara.repository.MemberRelationshipRepository;
import com.decathlon.ara.repository.MemberRepository;
import com.decathlon.ara.service.dto.member.MemberDTO;
import com.decathlon.ara.service.exception.BadRequestException;
import com.decathlon.ara.service.exception.NotFoundException;
import com.decathlon.ara.util.TestUtil;

abstract class MemberServiceTest<C, M extends Member, R extends MemberRelationship<C, M>> {
    
    protected abstract MemberService<C, M, R> getMemberService();
    protected abstract MemberContainerRepository<C, ?> getMemberContainerRepository();
    protected abstract MemberRelationshipRepository<C, M, R> getMemberRelationshipRepository();
    protected abstract MemberRepository<M> getMemberRepository();
    protected abstract C contructContainer(String name);
    protected abstract M contructMember(String memberName);
    protected abstract R contructMemberRelationship(String memberName, MemberRole role);
    
    @Mock
    protected CacheService cacheService;
    
    @Test
    void findAllShouldReturnEmptyListWhenNoRelationshipExists() {
        Assertions.assertEquals(0, getMemberService().findAll("identifier").size());
    }
    
    @Test
    void findAllShouldReturnListContaningAllDatabaseRelationship() {
        List<R> databaseRelatioship = List.of(
                contructMemberRelationship("adminA", MemberRole.ADMIN),
                contructMemberRelationship("maintainerA", MemberRole.MAINTAINER),
                contructMemberRelationship("memberA", MemberRole.MEMBER),
                contructMemberRelationship("memberB", MemberRole.MEMBER));
        
        Mockito.when(getMemberRelationshipRepository().findAllByContainerIdentifier("identifier")).thenReturn(databaseRelatioship);
        List<MemberDTO> resultList = getMemberService().findAll("identifier");
        Assertions.assertEquals(databaseRelatioship.size(), resultList.size());
        for (int i=0; i< databaseRelatioship.size(); i++) {
            Assertions.assertEquals(databaseRelatioship.get(i).getMemberName(), resultList.get(i).getName());
            Assertions.assertEquals(databaseRelatioship.get(i).getRole(), resultList.get(i).getRole());
        }
    }
    
    @Test
    void findOneShouldThrowNotFoundExceptionWhenMemberRelationshipDoesntExist() throws NotFoundException {
        Assertions.assertThrows(NotFoundException.class, () -> getMemberService().findOne("identifier", "memberName"));
    }
    
    @Test
    void findOneShouldReturnMemberDTOWhenMemberRelationshipExist() throws NotFoundException {
        R relationship = contructMemberRelationship("memberName", MemberRole.ADMIN);
        Mockito.when(getMemberRelationshipRepository().findByContainerIdentifierAndMemberName("identifier", "memberName")).thenReturn(relationship);
        MemberDTO result = getMemberService().findOne("identifier", "memberName");
        Assertions.assertEquals(relationship.getMemberName(), result.getName());
        Assertions.assertEquals(relationship.getRole(), result.getRole());
    }
    
    @Test
    void addMemberShouldThrowBadRequestExceptionExceptionWhenRelationshipAlreadyExist() {
        Mockito.when(getMemberRelationshipRepository().findByContainerIdentifierAndMemberName("identifier", "memberName")).thenReturn(contructMemberRelationship("test", MemberRole.ADMIN));
        Assertions.assertThrows(BadRequestException.class, () -> getMemberService().addMember("identifier", new MemberDTO("memberName", null)));
        Mockito.verify(getMemberRelationshipRepository(), Mockito.times(0)).save(Mockito.any());
    }
    
    @Test
    void addMemberShouldThrowNotFoundExceptionWhenRelationshipNotExistAndContainerNotExist() {
        Assertions.assertThrows(NotFoundException.class, () -> getMemberService().addMember("identifier", new MemberDTO()));
        Mockito.verify(getMemberRelationshipRepository(), Mockito.times(0)).save(Mockito.any());
    }
    
    @Test
    void addMemberShouldThrowNotFoundExceptionWhenRelationshipNotExistAndContainerExistAndMemberNotExist() {
        Mockito.when(getMemberContainerRepository().findByContainerIdentifier("identifier")).thenReturn(contructContainer("name"));
        Assertions.assertThrows(NotFoundException.class, () -> getMemberService().addMember("identifier", new MemberDTO()));
        Mockito.verify(getMemberRelationshipRepository(), Mockito.times(0)).save(Mockito.any());
    }
    
    @Test
    void addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist() throws BadRequestException {
        addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist(null);
    }
    
    void addMemberShouldCreateMemberWhenRelationshipNotExistAndContainerExistAndMemberExist(AdditionalAssertion<C, M, R> additionalAssertion) throws BadRequestException {
        C container = contructContainer("name");
        Mockito.when(getMemberContainerRepository().findByContainerIdentifier("identifier")).thenReturn(container);
        M member = contructMember("memberName");
        Mockito.when(getMemberRepository().findByMemberName("memberName")).thenReturn(member);
        MemberDTO memberDTO = new MemberDTO("memberName", MemberRole.MEMBER);
        Class<R> RelationshipClass = TestUtil.getField(getMemberService(), "relationshipClass");
        MemberDTO result = getMemberService().addMember("identifier", memberDTO);
        Assertions.assertEquals(memberDTO, result);
        ArgumentCaptor<R> captor = ArgumentCaptor.forClass(RelationshipClass);
        Mockito.verify(getMemberRelationshipRepository()).save(captor.capture());
        R savedRelationship = captor.getValue();
        Assertions.assertEquals(savedRelationship.getMemberName(), member.getMemberName());
        Assertions.assertEquals(savedRelationship.getRole(), memberDTO.getRole());
        if (additionalAssertion != null) {
            additionalAssertion.assertFor(container, member, savedRelationship);
        }
    }
    
    @Test
    void updateMemberRoleShouldThrowNotFoundExceptionWhenRelationshipNotExist() {
        Assertions.assertThrows(NotFoundException.class, () -> getMemberService().updateMemberRole("identifier", "memberName", null));
        Mockito.verify(getMemberRelationshipRepository(), Mockito.times(0)).save(Mockito.any());
    }
    
    @Test
    void updateMemberRoleShouldUpdateRelationshipWhenRelationshipExists() throws BadRequestException {
        R relationship = contructMemberRelationship("memberName", MemberRole.ADMIN);
        Mockito.when(getMemberRelationshipRepository().findByContainerIdentifierAndMemberName("identifier", "memberName")).thenReturn(relationship);
        Class<R> relationshipClass = TestUtil.getField(getMemberService(), "relationshipClass");
        MemberDTO result = getMemberService().updateMemberRole("identifier", "memberName", MemberRole.MAINTAINER);
        Assertions.assertEquals("memberName", result.getName());
        Assertions.assertEquals(MemberRole.MAINTAINER, result.getRole());
        ArgumentCaptor<R> captor = ArgumentCaptor.forClass(relationshipClass);
        Mockito.verify(getMemberRelationshipRepository()).save(captor.capture());
        R updated = captor.getValue();
        Assertions.assertEquals(result.getName(), updated.getMemberName());
        Assertions.assertEquals(result.getRole(), updated.getRole());
    }
    
    @Test
    void deleteMemberShouldThrowNotFoundExceptionWhenRelationshipNotExist() {
        Assertions.assertThrows(NotFoundException.class, () -> getMemberService().deleteMember("identifier", "memberName"));
    }
    
    @Test
    void deleteMemberShouldDeleteRelationshipWhenRelationshipExist() throws BadRequestException {
        R relationship = contructMemberRelationship("memberName", MemberRole.ADMIN);
        Mockito.when(getMemberRelationshipRepository().findByContainerIdentifierAndMemberName("identifier", "memberName")).thenReturn(relationship);
        Class<R> RelationshipClass = TestUtil.getField(getMemberService(), "relationshipClass");
        getMemberService().deleteMember("identifier", "memberName");
        ArgumentCaptor<R> captor = ArgumentCaptor.forClass(RelationshipClass);
        Mockito.verify(getMemberRelationshipRepository()).delete(captor.capture());
        R deleted = captor.getValue();
        Assertions.assertEquals(relationship, deleted);
    }
        
    interface AdditionalAssertion<C, M extends Member, R extends MemberRelationship>{
        
        public void assertFor(C container, M member, R Relationship);
        
    }
}
