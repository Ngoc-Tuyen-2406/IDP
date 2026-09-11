package com.idp.idpapi.ai.entity;

import com.idp.idpapi.contract.entity.ContractVersion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detection_regions")
public class DetectionRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private ContractVersion version;

    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "label", length = 50)
    private String label;

    @Column(name = "x_min")
    private Double xMin;

    @Column(name = "y_min")
    private Double yMin;

    @Column(name = "x_max")
    private Double xMax;

    @Column(name = "y_max")
    private Double yMax;

    @Column(name = "confidence")
    private Double confidence;

    public Integer getRegionId() {
        return regionId;
    }

    public void setRegionId(Integer regionId) {
        this.regionId = regionId;
    }

    public ContractVersion getVersion() {
        return version;
    }

    public void setVersion(ContractVersion version) {
        this.version = version;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getXMin() {
        return xMin;
    }

    public void setXMin(Double xMin) {
        this.xMin = xMin;
    }

    public Double getYMin() {
        return yMin;
    }

    public void setYMin(Double yMin) {
        this.yMin = yMin;
    }

    public Double getXMax() {
        return xMax;
    }

    public void setXMax(Double xMax) {
        this.xMax = xMax;
    }

    public Double getYMax() {
        return yMax;
    }

    public void setYMax(Double yMax) {
        this.yMax = yMax;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
