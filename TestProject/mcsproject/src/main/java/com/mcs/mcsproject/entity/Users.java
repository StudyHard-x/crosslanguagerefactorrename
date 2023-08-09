package com.mcs.mcsproject.entity;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class Users implements Serializable {
    private static final long serialVersionUID = -73006096239882008L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String userName;
    private Integer userAge;
    private String userNumber;

}

