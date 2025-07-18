package ru.iu3.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "artists")
@Access(AccessType.FIELD)
public class Artist {

    public Artist() { }
    public Artist(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    public Long id;

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @Column(name = "age", nullable = false)
    public String century;

    @ManyToOne()
    @JoinColumn(name = "countryid")
    public Country country;
}