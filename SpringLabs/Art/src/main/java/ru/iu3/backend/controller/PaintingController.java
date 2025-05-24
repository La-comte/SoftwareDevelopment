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
import ru.iu3.backend.entity.Museum;
import ru.iu3.backend.entity.Painting;
import ru.iu3.backend.repository.ArtistRepository;
import ru.iu3.backend.repository.MuseumRepository;
import ru.iu3.backend.repository.PaintingRepository;
import ru.iu3.backend.tools.DataValidationException;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class PaintingController {

    @Autowired
    PaintingRepository paintingRepository;

    @Autowired
    ArtistRepository artistRepository;

    @Autowired
    MuseumRepository museumRepository;


    @GetMapping("/paintings")
    public Page getAllPaintings(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return paintingRepository.findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")));
    }

    @GetMapping("/paintings/{id}")
    public ResponseEntity getPainting(@PathVariable(value = "id") Long paintingId)
            throws DataValidationException
    {
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(()-> new DataValidationException("Картина с таким индексом не найдена"));
        return ResponseEntity.ok(painting);
    }

    @PostMapping("/paintings")
    public ResponseEntity<Object> createPainting(@RequestBody Painting painting) throws DataValidationException {
        try {
            painting.artist = (Artist) artistRepository.findByName(painting.artist.name).orElseThrow(() -> new DataValidationException("Художник с таким индексом не найден"));
            painting.museum = (Museum) museumRepository.findByName(painting.museum.name).orElseThrow(() -> new DataValidationException("Музей с таким индексом не найден"));
            painting.id = null;
            Painting nc = paintingRepository.save(painting);
            return new ResponseEntity<Object>(nc, HttpStatus.OK);
        }
        catch(Exception ex) {
            if (ex.getMessage().contains("paintings.name_UNIQUE"))
                throw new DataValidationException("Эта картина уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PutMapping("/paintings/{id}")
    public ResponseEntity<Painting> updatePainting(@PathVariable(value = "id") Long paintingId, @Valid @RequestBody Painting paintingDetails)  throws DataValidationException{
        try {
            Painting painting = paintingRepository.findById(paintingId).orElseThrow(() -> new DataValidationException("Картина с таким индексом не найдена"));
            painting.name = paintingDetails.name;
            painting.artist = (Artist) artistRepository.findByName(paintingDetails.artist.name).orElseThrow(() -> new DataValidationException("Художник с таким именем не найден"));
            painting.museum = (Museum) museumRepository.findByName(paintingDetails.museum.name).orElseThrow(() -> new DataValidationException("Музей с таким именем не найден"));
            painting.year = paintingDetails.year;
            paintingRepository.save(painting);
            return ResponseEntity.ok(painting);
        }
        catch (Exception ex) {
            if (ex.getMessage().contains("paintings.name_UNIQUE"))
                throw new DataValidationException("Эта картина уже есть в базе");
            else
                throw new DataValidationException("Неизвестная ошибка");
        }
    }

    @PostMapping("/deletepaintings")
    public ResponseEntity<Object> deletePaintings(@Valid @RequestBody List<Painting> paintings) {
        paintingRepository.deleteAll(paintings);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}