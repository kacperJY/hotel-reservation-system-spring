package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "currencies")
public class CurrencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currencyGenerator")
    @SequenceGenerator(name = "currencyGenerator", sequenceName = "currencySeq", allocationSize = 50)
    @Column(name = "currency_id")
    private Long currencyId;

    private String currencySymbol;
    @Column(unique = true, nullable = false) private String currencyCode;
    private BigDecimal averageRate;
    private LocalDate publicationDate;

    public CurrencyEntity() {
    }

    public CurrencyEntity(String currencySymbol, String currencyCode, BigDecimal averageRate, LocalDate publicationDate) {
        this.currencySymbol = currencySymbol;
        this.currencyCode = currencyCode;
        this.averageRate = averageRate;
        this.publicationDate = publicationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        CurrencyEntity that = (CurrencyEntity) object;
        return Objects.equals(currencyCode, that.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(currencyCode);
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencyName) {
        this.currencySymbol = currencyName;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAverageRate() {
        return averageRate;
    }

    public void setAverageRate(BigDecimal averageRate) {
        this.averageRate = averageRate;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
}
