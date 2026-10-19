package eu.xplace.macbackend;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeraetRepository extends JpaRepository<Geraet, Long> {

    Optional<Geraet> findByMacIgnoreCase(String mac);
}
