package eu.xplace.macbackend;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GeraetRepository extends JpaRepository<Geraet, Long> {

    /** Vergleicht die gespeicherte MAC ohne Trennzeichen und in Kleinbuchstaben. */
    @Query("SELECT g FROM Geraet g WHERE LOWER(REPLACE(REPLACE(g.mac, '-', ''), ':', '')) = :mac")
    Optional<Geraet> findByNormalisierterMac(@Param("mac") String normalisierteMac);
}
