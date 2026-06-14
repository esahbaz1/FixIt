package ba.etf.fixit.notificationservice.model;

public enum TipNotifikacije {
    STATUS_PROMJENA,   // Promjena statusa prijave (za podnosioca)
    NOVI_KOMENTAR,     // Novi komentar na prijavi
    DODJELA_SLUZBI,    // Prijava dodijeljena gradskoj sluzbi (za rukovodioca sluzbe)
    DODJELA_RADNIKU,   // Prijava dodijeljena konkretnom radniku
    RIJESENO,          // Problem uspjesno rijesen (za podnosioca)
    NOVA_PRIJAVA       // Nova prijava u sistemu (za admin/rukovodioca)
}
