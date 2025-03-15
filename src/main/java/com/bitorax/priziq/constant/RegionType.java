package com.bitorax.priziq.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RegionType {
    AFGHANISTAN("AF", "Afghanistan", "AFG", 4, "Asia/Kabul"),
    ARGENTINA("AR", "Argentina", "ARG", 32, "America/Argentina/Buenos_Aires"),
    AUSTRALIA("AU", "Australia", "AUS", 36, "Australia/Sydney"),
    AUSTRIA("AT", "Austria", "AUT", 40, "Europe/Vienna"),
    BELGIUM("BE", "Belgium", "BEL", 56, "Europe/Brussels"),
    BRAZIL("BR", "Brazil", "BRA", 76, "America/Sao_Paulo"),
    CANADA("CA", "Canada", "CAN", 124, "America/Toronto"),
    CHINA("CN", "China", "CHN", 156, "Asia/Shanghai"),
    DENMARK("DK", "Denmark", "DNK", 208, "Europe/Copenhagen"),
    EGYPT("EG", "Egypt", "EGY", 818, "Africa/Cairo"),
    FINLAND("FI", "Finland", "FIN", 246, "Europe/Helsinki"),
    FRANCE("FR", "France", "FRA", 250, "Europe/Paris"),
    GERMANY("DE", "Germany", "DEU", 276, "Europe/Berlin"),
    GREECE("GR", "Greece", "GRC", 300, "Europe/Athens"),
    HONG_KONG("HK", "Hong Kong", "HKG", 344, "Asia/Hong_Kong"),
    INDIA("IN", "India", "IND", 356, "Asia/Kolkata"),
    INDONESIA("ID", "Indonesia", "IDN", 360, "Asia/Jakarta"),
    IRAN("IR", "Iran", "IRN", 364, "Asia/Tehran"),
    IRAQ("IQ", "Iraq", "IRQ", 368, "Asia/Baghdad"),
    IRELAND("IE", "Ireland", "IRL", 372, "Europe/Dublin"),
    ISRAEL("IL", "Israel", "ISR", 376, "Asia/Jerusalem"),
    ITALY("IT", "Italy", "ITA", 380, "Europe/Rome"),
    JAPAN("JP", "Japan", "JPN", 392, "Asia/Tokyo"),
    KENYA("KE", "Kenya", "KEN", 404, "Africa/Nairobi"),
    MALAYSIA("MY", "Malaysia", "MYS", 458, "Asia/Kuala_Lumpur"),
    MEXICO("MX", "Mexico", "MEX", 484, "America/Mexico_City"),
    NETHERLANDS("NL", "Netherlands", "NLD", 528, "Europe/Amsterdam"),
    NEW_ZEALAND("NZ", "New Zealand", "NZL", 554, "Pacific/Auckland"),
    NIGERIA("NG", "Nigeria", "NGA", 566, "Africa/Lagos"),
    NORWAY("NO", "Norway", "NOR", 578, "Europe/Oslo"),
    PAKISTAN("PK", "Pakistan", "PAK", 586, "Asia/Karachi"),
    PHILIPPINES("PH", "Philippines", "PHL", 608, "Asia/Manila"),
    POLAND("PL", "Poland", "POL", 616, "Europe/Warsaw"),
    PORTUGAL("PT", "Portugal", "PRT", 620, "Europe/Lisbon"),
    QATAR("QA", "Qatar", "QAT", 634, "Asia/Qatar"),
    RUSSIA("RU", "Russia", "RUS", 643, "Europe/Moscow"),
    SAUDI_ARABIA("SA", "Saudi Arabia", "SAU", 682, "Asia/Riyadh"),
    SINGAPORE("SG", "Singapore", "SGP", 702, "Asia/Singapore"),
    SOUTH_AFRICA("ZA", "South Africa", "ZAF", 710, "Africa/Johannesburg"),
    SOUTH_KOREA("KR", "South Korea", "KOR", 410, "Asia/Seoul"),
    SPAIN("ES", "Spain", "ESP", 724, "Europe/Madrid"),
    SWEDEN("SE", "Sweden", "SWE", 752, "Europe/Stockholm"),
    SWITZERLAND("CH", "Switzerland", "CHE", 756, "Europe/Zurich"),
    TAIWAN("TW", "Taiwan", "TWN", 158, "Asia/Taipei"),
    THAILAND("TH", "Thailand", "THA", 764, "Asia/Bangkok"),
    TURKEY("TR", "Turkey", "TUR", 792, "Europe/Istanbul"),
    UKRAINE("UA", "Ukraine", "UKR", 804, "Europe/Kiev"),
    UNITED_ARAB_EMIRATES("AE", "United Arab Emirates", "ARE", 784, "Asia/Dubai"),
    UNITED_KINGDOM("GB", "United Kingdom", "GBR", 826, "Europe/London"),
    UNITED_STATES("US", "United States", "USA", 840, "America/New_York"),
    VIETNAM("VN", "Vietnam", "VNM", 704, "Asia/Ho_Chi_Minh"),
    ZIMBABWE("ZW", "Zimbabwe", "ZWE", 716, "Africa/Harare");

    String alpha2Code;
    String countryName;
    String alpha3Code;
    int numericCode;
    String primaryTimeZone;
}