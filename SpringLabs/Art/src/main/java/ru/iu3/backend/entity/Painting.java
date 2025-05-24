package ru.iu3.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "paintings")
@Access(AccessType.FIELD)
public class Painting {
    public Painting() { }
    public Painting(Long id) { this.id = id; }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public Long id;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "year")
    public String year;

    @ManyToOne
    @JoinColumn(name="artistid")
    public Artist artist;

    @ManyToOne
    @JoinColumn(name="museumid")
    public Museum museum;
}