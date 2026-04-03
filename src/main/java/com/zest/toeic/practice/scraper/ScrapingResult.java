package com.zest.toeic.practice.scraper;

public class ScrapingResult {
    private int totalFound;
    private int imported;
    private int duplicates;
    private int invalid;
    private int errors;
    private String error;

    public void incrementDuplicates() { duplicates++; }
    public void incrementInvalid() { invalid++; }
    public void incrementErrors() { errors++; }

    // Getters and setters
    public int getTotalFound() { return totalFound; }
    public void setTotalFound(int totalFound) { this.totalFound = totalFound; }
    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }
    public int getDuplicates() { return duplicates; }
    public void setDuplicates(int duplicates) { this.duplicates = duplicates; }
    public int getInvalid() { return invalid; }
    public void setInvalid(int invalid) { this.invalid = invalid; }
    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
