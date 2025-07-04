package ru.iu3.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Access(AccessType.FIELD)
public class User {
    public User() { }
    public User(Long id) { this.id = id; }

    public void addMuseum(Museum m) {
        this.museums.add(m);
        m.users.add(this);
    }
    public void removeMuseum(Museum m) {
        this.museums.remove(m);
        m.users.remove(this);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public Long id;

    @Column(name = "login", nullable = false, unique = true)
    public String login;

    @JsonIgnore
    @Column(name = "password")
    public String password;

    @Transient
    public String np;

    @Column(name = "email", nullable = false, unique = true)
    public String email;

    @JsonIgnore
    @Column(name = "salt")
    public String salt;

    @Column(name = "token")
    public String token;

    @Column(name = "activity")
    public LocalDateTime activity;

    @ManyToMany(mappedBy = "users")
    public Set<Museum> museums = new HashSet<>();
}