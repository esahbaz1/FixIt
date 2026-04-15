package ba.etf.fixit.managementservice.dto;
public class GradskaSluzbaResponseDTO {
    private Long id; private String naziv; private String opis;
    private String kontaktEmail; private String kontaktTelefon; private Boolean aktivan;
    public GradskaSluzbaResponseDTO(){}
    public GradskaSluzbaResponseDTO(Long id,String naziv,String opis,String email,String tel,Boolean aktivan){
        this.id=id;this.naziv=naziv;this.opis=opis;this.kontaktEmail=email;this.kontaktTelefon=tel;this.aktivan=aktivan;
    }
    public Long getId(){return id;} public void setId(Long v){this.id=v;}
    public String getNaziv(){return naziv;} public void setNaziv(String v){this.naziv=v;}
    public String getOpis(){return opis;} public void setOpis(String v){this.opis=v;}
    public String getKontaktEmail(){return kontaktEmail;} public void setKontaktEmail(String v){this.kontaktEmail=v;}
    public String getKontaktTelefon(){return kontaktTelefon;} public void setKontaktTelefon(String v){this.kontaktTelefon=v;}
    public Boolean getAktivan(){return aktivan;} public void setAktivan(Boolean v){this.aktivan=v;}
}
