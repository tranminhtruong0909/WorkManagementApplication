package com.example.workmanager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "userrole")
public class UserRole {

    // Getters & Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Board board;

    @Enumerated(EnumType.STRING)
    private Role role;

}
