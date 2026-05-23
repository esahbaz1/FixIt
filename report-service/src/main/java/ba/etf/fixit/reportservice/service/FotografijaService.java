package ba.etf.fixit.reportservice.service;

import ba.etf.fixit.reportservice.dto.FotografijaResponseDTO;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.Fotografija;
import ba.etf.fixit.reportservice.model.Prijava;
import ba.etf.fixit.reportservice.repository.FotografijaRepository;
import ba.etf.fixit.reportservice.repository.PrijavaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FotografijaService {

    private static final int MAX_FOTOGRAFIJA = 5;

    @Value("${fixit.upload-dir:uploads/fotografije}")
    private String uploadDir;

    private final FotografijaRepository fotografijaRepo;
    private final PrijavaRepository prijavaRepo;

    public FotografijaService(FotografijaRepository fotografijaRepo, PrijavaRepository prijavaRepo) {
        this.fotografijaRepo = fotografijaRepo;
        this.prijavaRepo = prijavaRepo;
    }

    public List<FotografijaResponseDTO> dodajFotografije(Long prijavaId, MultipartFile[] fajlovi) throws IOException {
        Prijava prijava = prijavaRepo.findById(prijavaId)
                .orElseThrow(() -> new ResourceNotFoundException("Prijava " + prijavaId + " nije pronadjena"));

        long trenutniBroj = fotografijaRepo.countByPrijavaId(prijavaId);
        if (trenutniBroj + fajlovi.length > MAX_FOTOGRAFIJA) {
            throw new IllegalArgumentException(
                    "Prijava vec ima " + trenutniBroj + " fotografija. Maksimum je " + MAX_FOTOGRAFIJA + ".");
        }

        Path dirPath = Paths.get(uploadDir, String.valueOf(prijavaId));
        Files.createDirectories(dirPath);

        List<FotografijaResponseDTO> rezultat = new ArrayList<>();
        for (MultipartFile fajl : fajlovi) {
            String originalnIme = fajl.getOriginalFilename();
            String ekstenzija = (originalnIme != null && originalnIme.contains("."))
                    ? originalnIme.substring(originalnIme.lastIndexOf("."))
                    : ".jpg";
            String imeFajla = UUID.randomUUID().toString() + ekstenzija;
            Path putanja = dirPath.resolve(imeFajla);
            Files.copy(fajl.getInputStream(), putanja);

            Fotografija foto = new Fotografija();
            foto.setPrijava(prijava);
            foto.setPutanja("/" + uploadDir + "/" + prijavaId + "/" + imeFajla);
            rezultat.add(mapToResponse(fotografijaRepo.save(foto)));
        }
        return rezultat;
    }

    @Transactional(readOnly = true)
    public List<FotografijaResponseDTO> dohvatiFotografije(Long prijavaId) {
        if (!prijavaRepo.existsById(prijavaId)) {
            throw new ResourceNotFoundException("Prijava " + prijavaId + " nije pronadjena");
        }
        return fotografijaRepo.findByPrijavaId(prijavaId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private FotografijaResponseDTO mapToResponse(Fotografija f) {
        return new FotografijaResponseDTO(f.getId(), f.getPrijava().getId(), f.getPutanja(), f.getDatumUnosa());
    }
}
