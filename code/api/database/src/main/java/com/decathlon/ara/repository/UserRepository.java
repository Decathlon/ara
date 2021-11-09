package com.decathlon.ara.repository;

import org.springframework.stereotype.Repository;

import com.decathlon.ara.domain.User;

@Repository
public interface UserRepository extends MemberRepository<User> {

    @Override
    default User findByMemberName(String memberName) {
        return findById(memberName).orElse(null);
    }

    User findByNameAndIssuer(String name, String issuer);

}
