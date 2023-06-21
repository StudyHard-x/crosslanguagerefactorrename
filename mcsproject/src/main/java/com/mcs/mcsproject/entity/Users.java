package com.mcs.mcsproject.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import java.io.Serializable;

@Data
@Entity
public class Users implements Serializable {
    private static final long serialVersionUID = -73006096239882008L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer age;
    private String number;

}

