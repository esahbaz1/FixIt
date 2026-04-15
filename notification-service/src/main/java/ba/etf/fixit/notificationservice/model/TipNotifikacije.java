package ba.etf.fixit.notificationservice.model;

public enum TipNotifikacije {
    STATUS_PROMJENA,   // Promjena statusa prijave
    NOVI_KOMENTAR,     // Novi komentar na prijavi
    DODJELA_SLUZBI,    // Prijava dodijeljena gradskoj sluzbi
    RIJESENO,          // Problem uspjesno rijesen
    NOVA_PRIJAVA       // Nova prijava u sistemu (za admin/rukovodioca)
}
