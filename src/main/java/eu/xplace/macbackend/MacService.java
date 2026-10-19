package eu.xplace.macbackend;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class MacService {

    private static final Pattern TRENNZEICHEN = Pattern.compile("[-:]");
    private static final Pattern MAC_OHNE_TRENNZEICHEN = Pattern.compile("[0-9a-f]{12}");

    private final GeraetRepository repository;

    public MacService(GeraetRepository repository) {
        this.repository = repository;
    }

    /**
     * Sucht ein Gerät unabhängig von der Schreibweise der MAC
     * (aa-bb-cc-dd-ee-ff, aa:bb:cc:dd:ee:ff, aabbccddeeff; Groß-/Kleinschreibung egal).
     */
    public Optional<Geraet> sucheNachMac(String mac) {
        return repository.findByNormalisierterMac(normalisieren(mac));
    }

    /**
     * Entfernt Trennzeichen und wandelt in Kleinbuchstaben um (aabbccddeeff).
     *
     * @throws IllegalArgumentException wenn die MAC fehlt oder das Ergebnis keine 12 Hex-Zeichen sind
     */
    public String normalisieren(String mac) {
        if (mac == null) {
            throw new IllegalArgumentException("MAC-Adresse fehlt");
        }
        String normalisiert = TRENNZEICHEN.matcher(mac.trim()).replaceAll("").toLowerCase(Locale.ROOT);
        if (!MAC_OHNE_TRENNZEICHEN.matcher(normalisiert).matches()) {
            throw new IllegalArgumentException("Ungültige MAC-Adresse: " + mac);
        }
        return normalisiert;
    }
}
