package com.andreitraistaru.filestorage.dto;

import java.util.List;

public class FilesMatchingRegexpDTO {
    private String regexp;
    private List<String> filenames;

    public String getRegexp() {
        return regexp;
    }
    public List<String> getFilenames() {
        return filenames;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }
    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }
}
