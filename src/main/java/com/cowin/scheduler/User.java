package com.cowin.scheduler;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = User.TABLE_NAME)
public class User {

    static final String TABLE_NAME = "User";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String emailId;

    private String pinCode;

    private Boolean active;

    private String preference;

}
