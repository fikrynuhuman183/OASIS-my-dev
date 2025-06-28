package com.oasis.ocrspring.dto;

import java.util.List;

public class CreatePatientDto {
    private String patientName;
    private String contactNo;
    private String dob;
    private String gender;
    private List<String> riskFactors;
    private String medicalHistory;
    private String familyHistory;
    private List<String> systemicDisease;

    // Constructors
    public CreatePatientDto() {}

    public CreatePatientDto(String patientName, String contactNo, String dob, String gender, 
                           List<String> riskFactors, String medicalHistory, String familyHistory, 
                           List<String> systemicDisease) {
        this.patientName = patientName;
        this.contactNo = contactNo;
        this.dob = dob;
        this.gender = gender;
        this.riskFactors = riskFactors;
        this.medicalHistory = medicalHistory;
        this.familyHistory = familyHistory;
        this.systemicDisease = systemicDisease;
    }

    // Getters and Setters
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }

    public List<String> getSystemicDisease() { return systemicDisease; }
    public void setSystemicDisease(List<String> systemicDisease) { this.systemicDisease = systemicDisease; }
}