package ba.etf.fixit.reportservice.service;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrijavaService {
    private final PrijavaRepository prijavaRepo;
    private final KategorijaRepository kategorijaRepo;
    private final StatusiRepository statusiRepo;

    public PrijavaService(PrijavaRepository prijavaRepo, KategorijaRepository kategorijaRepo, StatusiRepository statusiRepo){
        this.prijavaRepo=prijavaRepo; this.kategorijaRepo=kategorijaRepo; this.statusiRepo=statusiRepo;
    }

    public List<PrijavaResponseDTO> dohvatiSve(){
        return prijavaRepo.findByArhiviranFalse().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PrijavaResponseDTO dohvatiPoId(Long id){
        return mapToResponse(nadji(id));
    }

    public PrijavaResponseDTO kreiraj(PrijavaRequestDTO dto){
        Kategorija kat = kategorijaRepo.findById(dto.getKategorijaId())
                .orElseThrow(()->new ResourceNotFoundException("Kategorija "+dto.getKategorijaId()+" nije pronadjena"));
        Statusi status = statusiRepo.findByNaziv("Novo")
                .orElseThrow(()->new ResourceNotFoundException("Status Novo nije pronadjen"));
        Prijava p = new Prijava(dto.getNaslov(),dto.getOpis(),dto.getLatitude(),dto.getLongitude(),dto.getAdresa(),kat,dto.getKorisnikId(),status);
        if(dto.getPrioritet()!=null) p.setPrioritet(dto.getPrioritet());
        if(dto.getDatumRoka()!=null) p.setDatumRoka(dto.getDatumRoka());
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO promijeniStatus(Long id, String noviStatusNaziv, Long korisnikId){
        Prijava p = nadji(id);
        Statusi novi = statusiRepo.findByNaziv(noviStatusNaziv)
                .orElseThrow(()->new ResourceNotFoundException("Status '"+noviStatusNaziv+"' nije pronadjen"));
        p.setStatus(novi);
        if("Rijeseno".equals(noviStatusNaziv)) p.setDatumZavrsetka(LocalDateTime.now());
        return mapToResponse(prijavaRepo.save(p));
    }

    public void arhiviraj(Long id){
        Prijava p = nadji(id); p.setArhiviran(true); prijavaRepo.save(p);
    }

    private Prijava nadji(Long id){
        return prijavaRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Prijava sa ID-em "+id+" nije pronadjena"));
    }

    public PrijavaResponseDTO mapToResponse(Prijava p){
        PrijavaResponseDTO dto = new PrijavaResponseDTO();
        dto.setId(p.getId()); dto.setNaslov(p.getNaslov()); dto.setOpis(p.getOpis());
        dto.setLatitude(p.getLatitude()); dto.setLongitude(p.getLongitude()); dto.setAdresa(p.getAdresa());
        dto.setStatusNaziv(p.getStatus()!=null?p.getStatus().getNaziv():null);
        dto.setPrioritet(p.getPrioritet());
        dto.setKategorijaId(p.getKategorija().getId()); dto.setNazivKategorije(p.getKategorija().getNaziv());
        dto.setKorisnikId(p.getKorisnikId()); dto.setDatumPodnosenja(p.getDatumPodnosenja());
        dto.setDatumRoka(p.getDatumRoka()); dto.setDatumZavrsetka(p.getDatumZavrsetka());
        dto.setArhiviran(p.getArhiviran());
        return dto;
    }
}
