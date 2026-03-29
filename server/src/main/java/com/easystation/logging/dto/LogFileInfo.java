package com.easystation.logging.dto;

import java.io.Serializable;

/**
 * 日志文件信息 DTO
 */
public class LogFileInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String fileName;
    private long fileSize;
    private String fileSizeHuman;
    private String lastModified;
    private boolean isArchived;
    private String filePath;
    
    public LogFileInfo() {
    }
    
    public LogFileInfo(String fileName, long fileSize, String lastModified, boolean isArchived, String filePath) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileSizeHuman = formatFileSize(fileSize);
        this.lastModified = lastModified;
        this.isArchived = isArchived;
        this.filePath = filePath;
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    // Getters and Setters
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
        this.fileSizeHuman = formatFileSize(fileSize);
    }
    
    public String getFileSizeHuman() {
        return fileSizeHuman;
    }
    
    public void setFileSizeHuman(String fileSizeHuman) {
        this.fileSizeHuman = fileSizeHuman;
    }
    
    public String getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean isArchived() {
        return isArchived;
    }
    
    public void setArchived(boolean archived) {
        isArchived = archived;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
