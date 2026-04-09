package org.example;

import org.example.cloud.S3Service;
import org.example.core.Encryptor;
import org.example.core.Fragmenter;
import org.example.manager.StatusManager;
import org.example.model.ImageRecord;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private S3Service s3Service;
    private final Fragmenter fragmenter = new Fragmenter();
    private final Encryptor encryptor = new Encryptor("imagecloud_secure_2026");
    private final StatusManager statusManager = new StatusManager();

    private final DefaultListModel<ImageRecord> listModel = new DefaultListModel<>();
    private final JList<ImageRecord> imageList = new JList<>(listModel);
    private final JLabel statusLabel = new JLabel("🛡️ System Ready");
    private final JProgressBar progressBar = new JProgressBar();

    private final String BASE_PATH = "data/";
    private final String INPUT_DIR = BASE_PATH + "input";
    private final String OUTPUT_DIR = BASE_PATH + "output";
    private final String BUCKET = "image-fragmentation-bucket-123";

    public Main() {
        setupAWS();
        setupUI();
        refreshList();
    }

    // 🔐 SECURE AWS SETUP
    private void setupAWS() {
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");

        if (accessKey == null || secretKey == null) {
            statusLabel.setText("⚠️ AWS Credentials Missing");
            JOptionPane.showMessageDialog(this,
                    "Set environment variables:\nAWS_ACCESS_KEY & AWS_SECRET_KEY");
            return;
        }

        try {
            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_SOUTH_1)
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();

            this.s3Service = new S3Service(s3Client, BUCKET);
            statusLabel.setText("✅ AWS Connected");

        } catch (Exception e) {
            statusLabel.setText("⚠️ AWS Connection Failed");
            e.printStackTrace();
        }
    }

    // 🎨 UI SETUP
    private void setupUI() {
        setTitle("🛡️ Cloud Fragmenter Pro");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        imageList.setCellRenderer(new StatusCellRenderer());
        add(new JScrollPane(imageList), BorderLayout.CENTER);

        JButton addBtn = new JButton("Add Image");
        JButton pushBtn = new JButton("Secure & Upload");
        JButton pullBtn = new JButton("Restore Image");

        JPanel panel = new JPanel();
        panel.add(addBtn);
        panel.add(pushBtn);
        panel.add(pullBtn);

        add(panel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> selectFile());
        pushBtn.addActionListener(e -> handlePush());
        pullBtn.addActionListener(e -> handlePull());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 📁 FILE SELECT
    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File src = chooser.getSelectedFile();
                File dest = new File(INPUT_DIR, src.getName());
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                refreshList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 🔄 REFRESH FILE LIST
    private void refreshList() {
        listModel.clear();

        new Thread(() -> {
            try {
                List<String> cloudKeys = (s3Service != null) ? s3Service.listMetadataKeys() : new ArrayList<>();
                List<ImageRecord> records = statusManager.getSyncStatus(INPUT_DIR, cloudKeys);

                SwingUtilities.invokeLater(() -> {
                    for (ImageRecord r : records) listModel.addElement(r);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 🚀 UPLOAD
    private void handlePush() {
        ImageRecord selected = imageList.getSelectedValue();
        if (selected == null || s3Service == null) return;

        new Thread(() -> {
            try {
                File file = new File(INPUT_DIR, selected.fileName());
                byte[] data = Files.readAllBytes(file.toPath());

                List<byte[]> parts = fragmenter.split(data, 4);

                for (int i = 0; i < parts.size(); i++) {
                    byte[] encrypted = encryptor.encrypt(parts.get(i));
                    s3Service.upload("fragments/" + selected.fileName() + "/part" + i, encrypted);
                }

                statusLabel.setText("✅ Uploaded Successfully");
                refreshList();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 📥 DOWNLOAD
    private void handlePull() {
        ImageRecord selected = imageList.getSelectedValue();
        if (selected == null || s3Service == null) return;

        new Thread(() -> {
            try {
                List<byte[]> parts = new ArrayList<>();

                for (int i = 0; i < 4; i++) {
                    byte[] enc = s3Service.download("fragments/" + selected.fileName() + "/part" + i);
                    parts.add(encryptor.decrypt(enc));
                }

                byte[] merged = fragmenter.merge(parts);

                File out = new File(OUTPUT_DIR, "RESTORED_" + selected.fileName());
                Files.write(out.toPath(), merged);

                JOptionPane.showMessageDialog(this, "Restored: " + out.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 🎨 LIST RENDER
    class StatusCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean focus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, focus);
            ImageRecord record = (ImageRecord) value;

            if (record.isAvailableInCloud()) {
                label.setText("🔒 SECURED: " + record.fileName());
                label.setForeground(Color.GREEN);
            } else {
                label.setText("📁 LOCAL: " + record.fileName());
                label.setForeground(Color.ORANGE);
            }

            return label;
        }
    }

    public static void main(String[] args) {
        new File("data/input").mkdirs();
        new File("data/output").mkdirs();
        SwingUtilities.invokeLater(Main::new);
    }
}