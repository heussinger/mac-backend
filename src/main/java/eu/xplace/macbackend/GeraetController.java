package eu.xplace.macbackend;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geraete")
public class GeraetController {

    private final GeraetRepository repository;

    public GeraetController(GeraetRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Geraet> alle() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Geraet> nachId(@PathVariable Long id) {
        return ResponseEntity.of(repository.findById(id));
    }

    @GetMapping("/suche")
    public ResponseEntity<Geraet> nachMac(@RequestParam String mac) {
        return ResponseEntity.of(repository.findByMacIgnoreCase(mac));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Geraet anlegen(@RequestBody Geraet geraet) {
        geraet.setId(null);
        return repository.save(geraet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Geraet> aendern(@PathVariable Long id, @RequestBody Geraet geraet) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        geraet.setId(id);
        return ResponseEntity.ok(repository.save(geraet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> loeschen(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void konflikt() {
        // z.B. MAC bereits vorhanden
    }
}
