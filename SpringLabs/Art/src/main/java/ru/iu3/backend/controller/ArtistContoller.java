package ru.iu3.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.iu3.backend.entity.Artist;
import ru.iu3.backend.entity.Country;
import ru.iu3.backend.entity.Painting;
import ru.iu3.backend.repository.ArtistRepository;
import ru.iu3.backend.repository.CountryRepository;
import ru.iu3.backend.tools.DataValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class ArtistContoller {
    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    CountryRepository countryRepository;

    @GetMapping("/artists")
    public Page<Artist> getAllArtists(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return artistRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity getArtist(@PathVariable(value = "id") Long artistId)
            throws DataValidationException
    {
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(()-> new DataValidationException("Художник с таким индексом не найден"));
        return ResponseEntity.ok(artist);
    }

    @GetMapping("/artists/{id}/paintings")
    public ResponseEntity<List<Painting>> getArtistPaintings(@PathVariable(value="id") Long artistId) {
        Optional<Artist> cc = artistRepository.findById(artistId);
        if (cc.isPresent()) {
            return (ResponseEntity<List<Painting>>) ResponseEntity.ok();
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping("/artists")
    public ResponseEntity<Object> createArtist(@RequestBody Artist artist) throws DataValidationException {
        try {
            artist.country = (Country) countryRepository.findByName(artist.country.name).orElseThrow(() -> new DataValidationException("Страна с таким индексом не найдена"));
            artist.id = null;
            Artist nc = artistRepository.save(artist);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        }
        catch(Exception ex) {
            if (ex.getMessage().contains("artists.name_UNIQUE"))
                throw new DataValidationException("Этот художник уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PutMapping("/artists/{id}")
    public ResponseEntity<Artist> updateArtist(@PathVariable(value = "id") Long artistId, @Valid @RequestBody Artist artistDetails)  throws DataValidationException{
        try {
            Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new DataValidationException("Художник с таким индексом не найден"));
            artist.name = artistDetails.name;
            artist.country = (Country) countryRepository.findByName(artistDetails.country.name).orElseThrow(() -> new DataValidationException("Страна с таким именем не найдена"));
            artist.century = artistDetails.century;
            artistRepository.save(artist);
            return ResponseEntity.ok(artist);
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("artist.name_UNIQUE"))
                throw new DataValidationException("Этот художник уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PostMapping("/deleteartists")
    public ResponseEntity<Object> deleteArtists(@Valid @RequestBody List<Artist> artists) {
        artistRepository.deleteAll(artists);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
