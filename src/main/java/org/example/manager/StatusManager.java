package org.example.manager;

import org.example.model.ImageRecord;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StatusManager {
    public List<ImageRecord> getSyncStatus(String localDir, List<String> metadataKeys) {
        List<ImageRecord> statusList = new ArrayList<>();
        File folder = new File(localDir);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    boolean exists = metadataKeys.contains("metadata/" + f.getName() + ".json");
                    statusList.add(new ImageRecord(f.getName(), exists));
                }
            }
        }
        return statusList;
    }
}