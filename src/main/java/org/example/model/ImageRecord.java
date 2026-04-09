package org.example.model;

public record ImageRecord(String fileName, boolean isAvailableInCloud) {
    @Override
    public String toString() {
        return fileName;
    }
}