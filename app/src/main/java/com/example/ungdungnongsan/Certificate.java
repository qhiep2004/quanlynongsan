package com.example.ungdungnongsan;

import java.io.Serializable;

public class Certificate implements Serializable {
    private String name;
    private String organization;
    private String issueDate;
    private String expiryDate;
    private String imageURL;

    // Default constructor for Firebase
    public Certificate() {
    }

    public Certificate(String name, String organization, String issueDate, String expiryDate, String imageURL) {
        this.name = name;
        this.organization = organization;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.imageURL = imageURL;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
