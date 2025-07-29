package com.example.workmanager.model;

import jakarta.persistence.*;


@Entity
@Table(name = "userrole")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Board board;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
