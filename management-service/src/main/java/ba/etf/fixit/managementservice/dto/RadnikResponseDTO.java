package ba.etf.fixit.managementservice.dto;
public class RadnikResponseDTO {
    private Long id; private Long korisnikId; private Long gradskaSluzbaId;
    private String nazivSluzbe; private String pozicija; private String kompetencije; private Boolean aktivan;
    public RadnikResponseDTO(){}
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public Long getKorisnikId(){return korisnikId;} public void setKorisnikId(Long v){this.korisnikId=v;}
    public Long getGradskaSluzbaId(){return gradskaSluzbaId;} public void setGradskaSluzbaId(Long v){this.gradskaSluzbaId=v;}
    public String getNazivSluzbe(){return nazivSluzbe;} public void setNazivSluzbe(String v){this.nazivSluzbe=v;}
    public String getPozicija(){return pozicija;} public void setPozicija(String v){this.pozicija=v;}
    public String getKompetencije(){return kompetencije;} public void setKompetencije(String v){this.kompetencije=v;}
    public Boolean getAktivan(){return aktivan;} public void setAktivan(Boolean v){this.aktivan=v;}
}
