package ba.etf.fixit.reportservice.service;
import ba.etf.fixit.reportservice.dto.*;
import ba.etf.fixit.reportservice.exception.ResourceNotFoundException;
import ba.etf.fixit.reportservice.model.*;
import ba.etf.fixit.reportservice.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PrijavaService {
    private final PrijavaRepository prijavaRepo;
    private final KategorijaRepository kategorijaRepo;
    private final StatusiRepository statusiRepo;
    private final TipPromjeneRepository tipPromjeneRepo;

    public PrijavaService(
            PrijavaRepository prijavaRepo,
            KategorijaRepository kategorijaRepo,
            StatusiRepository statusiRepo,
            TipPromjeneRepository tipPromjeneRepo) {
        this.prijavaRepo = prijavaRepo;
        this.kategorijaRepo = kategorijaRepo;
        this.statusiRepo = statusiRepo;
        this.tipPromjeneRepo = tipPromjeneRepo;
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
        Prijava p = new Prijava();
        p.setNaslov(dto.getNaslov());
        p.setOpis(dto.getOpis());
        p.setLatitude(dto.getLatitude());
        p.setLongitude(dto.getLongitude());
        p.setAdresa(dto.getAdresa());
        p.setKategorija(kat);
        p.setKorisnikId(dto.getKorisnikId());
        p.setStatus(status);
        if(dto.getPrioritet()!=null) p.setPrioritet(dto.getPrioritet());
        if(dto.getDatumRoka()!=null) p.setDatumRoka(dto.getDatumRoka());
        return mapToResponse(prijavaRepo.save(p));
    }

    public PrijavaResponseDTO promijeniStatus(Long id, String noviStatusNaziv, Long korisnikId){
        Prijava p = nadji(id);
        Statusi stariStatus = p.getStatus();
        Statusi novi = statusiRepo.findByNaziv(noviStatusNaziv)
                .orElseThrow(()->new ResourceNotFoundException("Status '"+noviStatusNaziv+"' nije pronadjen"));
        p.setStatus(novi);
        if (stariStatus != null && stariStatus.getNaziv().equals(noviStatusNaziv)) {
         throw new IllegalArgumentException("Status je već postavljen na isti");
}
        if("Rijeseno".equals(noviStatusNaziv)) p.setDatumZavrsetka(LocalDateTime.now());

        String stariNaziv = stariStatus != null ? stariStatus.getNaziv() : null;
        TipPromjene tip = tipPromjeneRepo.findByStatus1AndStatus2(stariNaziv, noviStatusNaziv)
                .orElseGet(() -> tipPromjeneRepo.save(new TipPromjene(null, stariNaziv, noviStatusNaziv)));


    HistorijaPrijave h = new HistorijaPrijave();
    h.setPrijava(p);
    h.setTipPromjene(tip);
    h.setKorisnikId(korisnikId);

    p.getHistorija().add(h);
        return mapToResponse(prijavaRepo.save(p));
    }

    public void arhiviraj(Long id){
        Prijava p = nadji(id); p.setArhiviran(true); prijavaRepo.save(p);
    }

    public PrijavaResponseDTO partialUpdate(Long id, Map<String, Object> fields) {

    Prijava p = prijavaRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prijava nije pronađena"));

    if (fields.containsKey("naslov")) {
        p.setNaslov((String) fields.get("naslov"));
    }

    if (fields.containsKey("opis")) {
        p.setOpis((String) fields.get("opis"));
    }

    if (fields.containsKey("adresa")) {
        p.setAdresa((String) fields.get("adresa"));
    }

    if (fields.containsKey("prioritet")) {
        p.setPrioritet(PrioritetPrijave.valueOf((String) fields.get("prioritet")));
    }

    return mapToResponse(prijavaRepo.save(p));
}

    private Prijava nadji(Long id){
        return prijavaRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Prijava sa ID-em "+id+" nije pronadjena"));
    }
     
public List<PrijavaResponseDTO> dohvatiSvePaged(int page, int size, String sortBy) {

    PageRequest pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sortBy));

    return prijavaRepo.findByArhiviranFalse(pageable)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
public List<PrijavaResponseDTO> hitneSaPrekoracenimRokom() {
    return prijavaRepo.findHitneSaPrekoracenimRokom(LocalDateTime.now())
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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
