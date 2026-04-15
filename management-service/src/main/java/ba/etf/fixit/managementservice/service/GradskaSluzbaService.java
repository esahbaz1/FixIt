package ba.etf.fixit.managementservice.service;
import ba.etf.fixit.managementservice.dto.*;
import ba.etf.fixit.managementservice.exception.*;
import ba.etf.fixit.managementservice.model.GradskaSluzba;
import ba.etf.fixit.managementservice.repository.GradskaSluzbaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GradskaSluzbaService {
    private final GradskaSluzbaRepository repo;
    public GradskaSluzbaService(GradskaSluzbaRepository repo){this.repo=repo;}

    public List<GradskaSluzbaResponseDTO> dohvatiSve(){
        return repo.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    public GradskaSluzbaResponseDTO dohvatiPoId(Long id){
        return mapToResponse(repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Gradska sluzba sa ID-em "+id+" nije pronadjena")));
    }
    public GradskaSluzbaResponseDTO kreiraj(GradskaSluzbaRequestDTO dto){
        if(repo.existsByNaziv(dto.getNaziv())) throw new DuplikatException("Sluzba '"+dto.getNaziv()+"' vec postoji");
        GradskaSluzba s = new GradskaSluzba(dto.getNaziv(),dto.getOpis(),dto.getKontaktEmail(),dto.getKontaktTelefon());
        return mapToResponse(repo.save(s));
    }
    public GradskaSluzbaResponseDTO azuriraj(Long id, GradskaSluzbaRequestDTO dto){
        GradskaSluzba s = repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Gradska sluzba sa ID-em "+id+" nije pronadjena"));
        s.setNaziv(dto.getNaziv()); s.setOpis(dto.getOpis());
        s.setKontaktEmail(dto.getKontaktEmail()); s.setKontaktTelefon(dto.getKontaktTelefon());
        return mapToResponse(repo.save(s));
    }
    public void obrisi(Long id){
        if(!repo.existsById(id)) throw new ResourceNotFoundException("Gradska sluzba sa ID-em "+id+" nije pronadjena");
        repo.deleteById(id);
    }
    public GradskaSluzbaResponseDTO mapToResponse(GradskaSluzba s){
        return new GradskaSluzbaResponseDTO(s.getId(),s.getNaziv(),s.getOpis(),s.getKontaktEmail(),s.getKontaktTelefon(),s.getAktivan());
    }
}
