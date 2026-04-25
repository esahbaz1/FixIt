package ba.etf.fixit.managementservice.service;
import ba.etf.fixit.managementservice.dto.*;
import ba.etf.fixit.managementservice.exception.*;
import ba.etf.fixit.managementservice.model.*;
import ba.etf.fixit.managementservice.repository.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Service
@Transactional
public class RadnikService {
    private final RadnikRepository radnikRepo;
    private final GradskaSluzbaRepository sluzbaRepo;
    public RadnikService(RadnikRepository radnikRepo, GradskaSluzbaRepository sluzbaRepo){
        this.radnikRepo=radnikRepo; this.sluzbaRepo=sluzbaRepo;
    }

    public List<RadnikResponseDTO> dohvatiSve(){
        return radnikRepo.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    public RadnikResponseDTO dohvatiPoId(Long id){
        return mapToResponse(nadji(id));
    }
    public List<RadnikResponseDTO> dohvatiPoSluzbi(Long sluzbaId){
        return radnikRepo.findByGradskaSluzbaId(sluzbaId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    public RadnikResponseDTO kreiraj(RadnikRequestDTO dto){
        GradskaSluzba sluzba = sluzbaRepo.findById(dto.getGradskaSluzbaId())
                .orElseThrow(()->new ResourceNotFoundException("Gradska sluzba "+dto.getGradskaSluzbaId()+" nije pronadjena"));
        Radnik r = new Radnik(null, dto.getKorisnikId(), sluzba, dto.getPozicija(), dto.getKompetencije(), true);
        return mapToResponse(radnikRepo.save(r));
    }
    public void obrisi(Long id){
        if(!radnikRepo.existsById(id)) throw new ResourceNotFoundException("Radnik sa ID-em "+id+" nije pronadjen");
        radnikRepo.deleteById(id);
    }
    private Radnik nadji(Long id){
        return radnikRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Radnik sa ID-em "+id+" nije pronadjen"));
    }
    public RadnikResponseDTO mapToResponse(Radnik r){
        RadnikResponseDTO dto = new RadnikResponseDTO();
        dto.setId(r.getId()); dto.setKorisnikId(r.getKorisnikId());
        dto.setGradskaSluzbaId(r.getGradskaSluzba().getId());
        dto.setNazivSluzbe(r.getGradskaSluzba().getNaziv());
        dto.setPozicija(r.getPozicija()); dto.setKompetencije(r.getKompetencije());
        dto.setAktivan(r.getAktivan());
        return dto;
    }
    public List<RadnikResponseDTO> dohvatiPoSluzbiPaged(Long sluzbaId, int page, int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    Page<Radnik> result = radnikRepo.findByGradskaSluzbaId(sluzbaId, pageable);

    return result.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}


public List<RadnikResponseDTO> aktivniPoPoziciji(Long sluzbaId, String pozicija) {
    return radnikRepo.findAktivniPoPoziciji(sluzbaId, pozicija)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
@Transactional
public RadnikResponseDTO premjestiRadnika(Long radnikId, Long novaSluzbaId) {

    Radnik r = radnikRepo.findById(radnikId)
            .orElseThrow(() -> new ResourceNotFoundException("Radnik ne postoji"));

    GradskaSluzba nova = sluzbaRepo.findById(novaSluzbaId)
            .orElseThrow(() -> new ResourceNotFoundException("Sluzba ne postoji"));

    r.setGradskaSluzba(nova);

    return mapToResponse(radnikRepo.save(r));
}
}
