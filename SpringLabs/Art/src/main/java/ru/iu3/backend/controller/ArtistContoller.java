package ru.iu3.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.iu3.backend.entity.Artist;
import ru.iu3.backend.entity.Country;
import ru.iu3.backend.repository.ArtistRepository;
import ru.iu3.backend.repository.CountryRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class ArtistContoller {
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    CountryRepository countryRepository;
    @GetMapping("/artists")
    public List getAllArtists() {
        return artistRepository.findAll();
    }
    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist) throws Exception {
        try {
            Optional<Country>
                    cc = countryRepository.findById(artist.country.id);
            if (cc.isPresent()) {
                artist.country = cc.get();
            }
            Artist nc = artistRepository.save(artist);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        } finally {

        }
    }


}
