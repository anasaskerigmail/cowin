package com.cowin.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query("SELECT distinct u.pinCode from User u WHERE u.active = true")
    List<String> getDistinctPinCode();

    @Query("SELECT u from User u WHERE u.active = true and u.pinCode like %?1%")
    List<User> getUsersByPinCode(String pincode);

}
